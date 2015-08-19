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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.gradle.tooling.ProgressListener;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;

/**
 * Finds the root projects for the selection and issues a classpath update on each related workspace
 * project.
 */
public final class RefreshGradleProjectsJob extends ToolingApiWorkspaceJob {

    private final List<IProject> projects;

    public RefreshGradleProjectsJob(List<IProject> projects) {
        super("Refresh classpath", true);
        this.projects = ImmutableList.copyOf(projects);
    }

    @Override
    protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) throws Exception {
        monitor.beginTask("Refresh selected Gradle projects", 2);
        try {
            // find the root projects related to the selection and reload their model
            Set<OmniEclipseGradleBuild> gradleBuilds = reloadRootEclipseModels(new SubProgressMonitor(monitor, 1));
            updateAllProjects(collectAllGradleProjectsFromAllBuilds(gradleBuilds), new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
    }

    private Set<OmniEclipseGradleBuild> reloadRootEclipseModels(IProgressMonitor monitor) {
        monitor.beginTask("Reload Eclipse Gradle projects", IProgressMonitor.UNKNOWN);
        try {
            return reloadEclipseModels();
        } finally {
            monitor.done();
        }
    }

    private void updateAllProjects(List<OmniEclipseProject> gradleProjects, IProgressMonitor monitor) {
        monitor.beginTask("Update projects", gradleProjects.size());
        try {
            for (OmniEclipseProject gradleProject : gradleProjects) {
                update(gradleProject);
                monitor.worked(1);
            }
        } finally {
            monitor.done();
        }
    }

    private Set<OmniEclipseGradleBuild> reloadEclipseModels() {
        Set<FixedRequestAttributes> attributesFromConfiguration = readRequestAttributesFromProjectConfiguration(this.projects);
        return FluentIterable.from(attributesFromConfiguration).transform(new Function<FixedRequestAttributes, OmniEclipseGradleBuild>() {

            @Override
            public OmniEclipseGradleBuild apply(final FixedRequestAttributes requestAttributes) {
                ProcessStreams stream = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
                return CorePlugin.modelRepositoryProvider().getModelRepository(requestAttributes)
                        .fetchEclipseGradleBuild(new TransientRequestAttributes(false, stream.getOutput(), stream.getError(), stream.getInput(),
                                ImmutableList.<ProgressListener>of(), ImmutableList.<org.gradle.tooling.events.ProgressListener>of(), getToken()), FetchStrategy.FORCE_RELOAD);
            }
        }).toSet();
    }

    private void update(OmniEclipseProject modelProject) {
        Optional<IProject> workspaceProject = CorePlugin.workspaceOperations().findProjectByLocation(modelProject.getProjectDirectory());
        if (workspaceProject.isPresent()) {
            try {
                if (workspaceProject.get().hasNature(JavaCore.NATURE_ID)) {
                    IJavaProject workspacejavaProject = JavaCore.create(workspaceProject.get());
                    GradleClasspathContainer.requestUpdateOf(workspacejavaProject);
                }
                // TODO (donat) the update mechanism should be extended to non-java projects too
            } catch (CoreException e) {
                throw new GradlePluginsRuntimeException(e);
            }
        }
    }

    private static Set<FixedRequestAttributes> readRequestAttributesFromProjectConfiguration(List<IProject> projects) {
        return FluentIterable.from(projects).transform(new Function<IProject, FixedRequestAttributes>() {

            @Override
            public FixedRequestAttributes apply(IProject project) {
                return CorePlugin.projectConfigurationManager().readProjectConfiguration(project).getRequestAttributes();
            }
        }).toSet();
    }

    private static List<OmniEclipseProject> collectAllGradleProjectsFromAllBuilds(Collection<OmniEclipseGradleBuild> builds) {
        ImmutableList.Builder<OmniEclipseProject> result = new ImmutableList.Builder<OmniEclipseProject>();

        for (OmniEclipseGradleBuild build : builds) {
            result.addAll(collectAllGradleProjectsRecursively(build.getRootEclipseProject()));
        }
        return result.build();
    }

    private static List<OmniEclipseProject> collectAllGradleProjectsRecursively(OmniEclipseProject project) {
        ImmutableList.Builder<OmniEclipseProject> result = new ImmutableList.Builder<OmniEclipseProject>();
        result.add(project);
        for (OmniEclipseProject child : project.getChildren()) {
            result.addAll(collectAllGradleProjectsRecursively(child));
        }
        return result.build();
    }
}
