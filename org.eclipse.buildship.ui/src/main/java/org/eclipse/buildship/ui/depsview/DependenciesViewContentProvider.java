/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.depsview;

import java.util.List;

import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.console.ProcessStreamsProvider;
import org.eclipse.buildship.core.gradle.Specs;
import org.eclipse.buildship.core.model.LoadEclipseGradleBuildsJob;
import org.eclipse.buildship.core.workspace.WorkspaceOperations;
import org.eclipse.buildship.ui.domain.DependencyNode;
import org.eclipse.buildship.ui.domain.ProjectNode;
import org.eclipse.buildship.ui.taskview.TaskViewContent;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.TestProgressListener;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.gradleware.tooling.toolingclient.Consumer;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniExternalDependency;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.Path;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.ModelRepositoryProvider;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

/**
 * Content provider for the {@link DependenciesView}.
 */
public final class DependenciesViewContentProvider implements ITreeContentProvider {

    private static final Object[] NO_CHILDREN = new Object[0];

    private final DependenciesView dependenciesView;
    private final ModelRepositoryProvider modelRepositoryProvider;
    private final ProcessStreamsProvider processStreamsProvider;
    private final WorkspaceOperations workspaceOperations;

    DependenciesViewContentProvider(DependenciesView dependenciesView, ModelRepositoryProvider modelRepositoryProvider, ProcessStreamsProvider processStreamsProvider,
            WorkspaceOperations workspaceOperations) {
        this.dependenciesView = dependenciesView;
        this.modelRepositoryProvider = Preconditions.checkNotNull(modelRepositoryProvider);
        this.processStreamsProvider = Preconditions.checkNotNull(processStreamsProvider);
        this.workspaceOperations = Preconditions.checkNotNull(workspaceOperations);
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // handle the case where the new input is null
        // (this happens when the ContentViewer gets disposed)
        if (newInput == null) {
            return;
        }

        // the only way to set the input is
        // through TaskView#setInput(TaskViewContent)
        TaskViewContent content = TaskViewContent.class.cast(newInput);
        LoadEclipseGradleBuildsJob loadEclipseGradleBuildsJob = new LoadEclipseGradleBuildsJob(this.modelRepositoryProvider, this.processStreamsProvider,
                content.getModelFetchStrategy(), content.getRootProjectConfigurations(), new LoadEclipseGradleBuildPostProcess(this.dependenciesView));
        loadEclipseGradleBuildsJob.schedule();
    }

    @Override
    public Object[] getElements(Object input) {
        ImmutableList.Builder<Object> result = ImmutableList.builder();
        if (input instanceof TaskViewContent) {
            TaskViewContent content = (TaskViewContent) input;
            for (ProjectConfiguration projectConfiguration : content.getRootProjectConfigurations()) {
                result.addAll(createTopLevelProjectNodes(projectConfiguration));
            }
        }
        return result.build().toArray();
    }

    private List<ProjectNode> createTopLevelProjectNodes(ProjectConfiguration projectConfiguration) {
        OmniEclipseGradleBuild gradleBuild = fetchCachedEclipseGradleBuild(projectConfiguration.getRequestAttributes());
        if (gradleBuild == null) {
            // no Gradle projects are cached yet, meaning the async job
            // to load the projects is still running, thus nothing to show
            return ImmutableList.of();
        } else {
            // flatten the tree of Gradle projects to a list, similar
            // to how Eclipse projects look in the Eclipse Project explorer
            List<ProjectNode> allProjectNodes = Lists.newArrayList();
            collectProjectNodesRecursively(gradleBuild.getRootEclipseProject(), gradleBuild.getRootProject(), null, allProjectNodes);
            return allProjectNodes;
        }
    }

    private OmniEclipseGradleBuild fetchCachedEclipseGradleBuild(FixedRequestAttributes fixedRequestAttributes) {
        List<ProgressListener> noProgressListeners = ImmutableList.of();
        List<TestProgressListener> noTestProgressListeners = ImmutableList.of();
        CancellationToken cancellationToken = GradleConnector.newCancellationTokenSource().token();
        TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, null, null, null, noProgressListeners, noTestProgressListeners, cancellationToken);
        ModelRepository repository = this.modelRepositoryProvider.getModelRepository(fixedRequestAttributes);
        return repository.fetchEclipseGradleBuild(transientAttributes, FetchStrategy.FROM_CACHE_ONLY);
    }

    private void collectProjectNodesRecursively(OmniEclipseProject eclipseProject, OmniGradleProject gradleRootProject, ProjectNode parentProjectNode,
            List<ProjectNode> allProjectNodes) {
        // find the Gradle project corresponding to the Eclipse project
        // (there will always be exactly one match)
        Path gradleProjectPath = eclipseProject.getPath();
        OmniGradleProject gradleProject = gradleRootProject.tryFind(Specs.gradleProjectMatchesProjectPath(gradleProjectPath)).get();

        // find the native Eclipse project in the Eclipse workspace
        // (search by the name defined on the OmniEclipseProject since this is
        // the name we use to create a native Eclipse project)
        Optional<IProject> workspaceProject = DependenciesViewContentProvider.this.workspaceOperations.findProjectByName(eclipseProject.getName());

        // create a new node for the given Eclipse project and then recurse into the children
        ProjectNode projectNode = new ProjectNode(parentProjectNode, eclipseProject, gradleProject, workspaceProject);
        allProjectNodes.add(projectNode);
        for (OmniEclipseProject childProject : eclipseProject.getChildren()) {
            collectProjectNodesRecursively(childProject, gradleRootProject, projectNode, allProjectNodes);
        }
    }

    @Override
    public boolean hasChildren(Object element) {
        return element instanceof ProjectNode;
    }

    @Override
    public Object[] getChildren(Object parent) {
        return parent instanceof ProjectNode ? childrenOf((ProjectNode) parent) : NO_CHILDREN;
    }

    private Object[] childrenOf(ProjectNode projectNode) {
        ImmutableList.Builder<DependencyNode> result = ImmutableList.builder();
        for (OmniExternalDependency dependency : projectNode.getEclipseProject().getExternalDependencies()) {
            result.add(new DependencyNode(projectNode, dependency));
        }
        return FluentIterable.from(result.build()).toArray(DependencyNode.class);
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof ProjectNode) {
            return ((ProjectNode) element).getParentProjectNode();
        } else if (element instanceof DependencyNode) {
            return ((DependencyNode) element).getParentProjectNode();
        } else {
            return null;
        }
    }

    @Override
    public void dispose() {
    }

    /**
     * {@code Consumer} that, when it gets invoked, refreshes the task view.
     */
    private static final class LoadEclipseGradleBuildPostProcess implements Consumer<Optional<OmniEclipseGradleBuild>> {

        private final DependenciesView dependenciesView;

        private LoadEclipseGradleBuildPostProcess(DependenciesView dependenciesView) {
            this.dependenciesView = Preconditions.checkNotNull(dependenciesView);
        }

        @Override
        public void accept(Optional<OmniEclipseGradleBuild> eclipseGradleBuild) {
            // refresh the content of the task view to display the results
            // (refresh regardless of whether the mode was loaded successfully or not)
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    // todo (etst) only refresh the node that corresponds to the loaded Gradle build
                    LoadEclipseGradleBuildPostProcess.this.dependenciesView.refresh();
                }
            });
        }
    }
}
