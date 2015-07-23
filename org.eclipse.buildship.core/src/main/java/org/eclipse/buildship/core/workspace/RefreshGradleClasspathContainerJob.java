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

package org.eclipse.buildship.core.workspace;

import java.util.List;
import java.util.Set;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;
import com.gradleware.tooling.toolingmodel.OmniGradleProjectStructure;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;

/**
 * Finds the root projects for the selection and requests a classpath update for each related
 * workspace project.
 */
public final class RefreshGradleClasspathContainerJob extends ToolingApiWorkspaceJob {

    private final List<IProject> selectedProjects;

    public RefreshGradleClasspathContainerJob(List<IProject> selectedProjects) {
        super("Refresh classpath", true);
        this.selectedProjects = Preconditions.checkNotNull(selectedProjects);
    }

    @Override
    protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) throws Exception {
        // find the root projects related to the selection and reload their model
        Set<OmniGradleProjectStructure> rootGradleProjects = collectGradleRootProjects(this.selectedProjects);
        Set<IProject> eclipseRootProjects = collectEclipseRootProjects(rootGradleProjects);
        reloadEclipseModel(eclipseRootProjects);

        // request the classpath update for the related java projects
        List<IJavaProject> eclipseJavaProjectsToUpdate = collectJavaProjectsBelongingToGradleStructure(rootGradleProjects);
        requestClasspathUpdates(eclipseJavaProjectsToUpdate);
    }

    private Set<OmniGradleProjectStructure> collectGradleRootProjects(List<IProject> selectedProjects) {
        return FluentIterable.from(selectedProjects).filter(new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                return project.isOpen() && GradleProjectNature.INSTANCE.isPresentOn(project);
            }
        }).transform(new Function<IProject, OmniGradleProjectStructure>() {

            @Override
            public OmniGradleProjectStructure apply(IProject project) {
                FixedRequestAttributes requestAttributes = CorePlugin.projectConfigurationManager().readProjectConfiguration(project).getRequestAttributes();
                ProcessStreams stream = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();

                OmniGradleBuildStructure structure = CorePlugin.modelRepositoryProvider().getModelRepository(requestAttributes)
                        .fetchGradleBuildStructure(new TransientRequestAttributes(false, stream.getOutput(), stream.getError(), stream.getInput(),
                                ImmutableList.<ProgressListener>of(), ImmutableList.<org.gradle.tooling.events.ProgressListener>of(),
                                GradleConnector.newCancellationTokenSource().token()), FetchStrategy.LOAD_IF_NOT_CACHED);
                return structure.getRootProject();
            }
        }).toSet();
    }

    private Set<IProject> collectEclipseRootProjects(Set<OmniGradleProjectStructure> rootProjects) {
        return FluentIterable.from(rootProjects).transform(new Function<OmniGradleProjectStructure, IProject>() {

            @Override
            public IProject apply(OmniGradleProjectStructure structure) {
                return CorePlugin.workspaceOperations().findProjectByName(structure.getName()).orNull();
            }
        }).toSet();
    }

    private List<IJavaProject> collectJavaProjectsBelongingToGradleStructure(Set<OmniGradleProjectStructure> projects) {
        final ImmutableSet<String> allProjectNames = getAllProjectNames(projects);

        return FluentIterable.from(CorePlugin.workspaceOperations().getAllProjects()).filter(new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                try {
                    return project.isAccessible() && allProjectNames.contains(project.getName()) && project.hasNature(JavaCore.NATURE_ID);
                } catch (CoreException e) {
                    throw new GradlePluginsRuntimeException(e);
                }
            }
        }).transform(new Function<IProject, IJavaProject>() {

            @Override
            public IJavaProject apply(IProject project) {
                return JavaCore.create(project);
            }
        }).toList();
    }

    private ImmutableSet<String> getAllProjectNames(Set<OmniGradleProjectStructure> rootProjects) {
        ImmutableSet.Builder<String> relatedProjectNames = ImmutableSet.builder();
        for (OmniGradleProjectStructure rootProject : rootProjects) {
            relatedProjectNames.addAll(getProjectNamesRecursively(rootProject));
        }

        return relatedProjectNames.build();
    }

    private List<String> getProjectNamesRecursively(OmniGradleProjectStructure projectStructure) {
        ImmutableList.Builder<String> projectNames = ImmutableList.builder();
        projectNames.add(projectStructure.getName());
        for (OmniGradleProjectStructure childStructure : projectStructure.getChildren()) {
            projectNames.addAll(getProjectNamesRecursively(childStructure));
        }
        return projectNames.build();
    }

    private void reloadEclipseModel(Set<IProject> rootEclipseProjects) {
        for (IProject project : rootEclipseProjects) {
            ProjectConfiguration configuration = CorePlugin.projectConfigurationManager().readProjectConfiguration(project);
            reloadEclipseGradleBuildModel(configuration.getRequestAttributes());
        }
    }

    private void reloadEclipseGradleBuildModel(FixedRequestAttributes fixedRequestAttributes) {
        ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        List<ProgressListener> noProgressListeners = ImmutableList.of();
        List<org.gradle.tooling.events.ProgressListener> noTypedProgressListeners = ImmutableList.of();
        CancellationToken cancellationToken = GradleConnector.newCancellationTokenSource().token();
        TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), null, noProgressListeners,
                noTypedProgressListeners, cancellationToken);
        ModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(fixedRequestAttributes);
        repository.fetchEclipseGradleBuild(transientAttributes, FetchStrategy.FORCE_RELOAD);
    }

    private void requestClasspathUpdates(List<IJavaProject> javaProjects) {
        for (IJavaProject javaProject : javaProjects) {
            GradleClasspathContainer.requestUpdateOf(javaProject);
        }
    }
}
