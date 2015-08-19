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

import com.gradleware.tooling.toolingmodel.repository.ModelRepositoryProvider;
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
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;

/**
 * Finds the root projects for the selection and issues a classpath update on each related workspace
 * project.
 */
public final class RefreshGradleProjectsJob extends ToolingApiWorkspaceJob {

    private final List<IProject> projects;

    public RefreshGradleProjectsJob(List<IProject> projects) {
        super("Refresh Gradle projects", true);
        this.projects = ImmutableList.copyOf(projects);
    }

    @Override
    protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) throws Exception {
        monitor.beginTask("Refresh selected Gradle projects in workspace", 2);
        try {
            // find the root projects related to the selection and reload their model
            Set<OmniEclipseGradleBuild> eclipseGradleBuilds = reloadEclipseGradleBuilds(new SubProgressMonitor(monitor, 1));
            List<OmniEclipseProject> gradleProjectsToUpdate = collectAllGradleProjectsFromAllBuilds(eclipseGradleBuilds);
            updateWorkspaceProjects(gradleProjectsToUpdate, new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
    }

    private Set<OmniEclipseGradleBuild> reloadEclipseGradleBuilds(IProgressMonitor monitor) {
        monitor.beginTask("Reload selected Gradle projects from Gradle", IProgressMonitor.UNKNOWN);
        try {
            return forceReloadEclipseGradleBuilds(monitor);
        } finally {
            monitor.done();
        }
    }

    private Set<OmniEclipseGradleBuild> forceReloadEclipseGradleBuilds(final IProgressMonitor monitor) {
        // todo (etst) can happen in parallel
        Set<FixedRequestAttributes> rootProjectConfigurations = getUniqueRootProjectConfigurations(this.projects);
        return FluentIterable.from(rootProjectConfigurations).transform(new Function<FixedRequestAttributes, OmniEclipseGradleBuild>() {

            @Override
            public OmniEclipseGradleBuild apply(final FixedRequestAttributes requestAttributes) {
                ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
                ImmutableList<ProgressListener> listeners = ImmutableList.<ProgressListener>of(new DelegatingProgressListener(monitor));
                TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), streams.getInput(), listeners,
                        ImmutableList.<org.gradle.tooling.events.ProgressListener>of(), getToken());
                ModelRepositoryProvider repository = CorePlugin.modelRepositoryProvider();
                return repository.getModelRepository(requestAttributes).fetchEclipseGradleBuild(transientAttributes, FetchStrategy.FORCE_RELOAD);
            }
        }).toSet();
    }

    private void updateWorkspaceProjects(List<OmniEclipseProject> gradleProjects, IProgressMonitor monitor) {
        monitor.beginTask("Update selected Gradle projects in workspace", gradleProjects.size());
        try {
            for (OmniEclipseProject gradleProject : gradleProjects) {
                updateWorkspaceProject(gradleProject);
                monitor.worked(1);
            }
        } finally {
            monitor.done();
        }
    }


    private void updateWorkspaceProject(OmniEclipseProject gradleProject) {
        // todo (etst) do not abort if one of the projects throws an exception but continue
        // todo (donat) the update mechanism should be extended to non-java projects too
        Optional<IProject> workspaceProject = CorePlugin.workspaceOperations().findProjectByLocation(gradleProject.getProjectDirectory());
        if (workspaceProject.isPresent()) {
            try {
                // todo (etst) check for _open_ projects with _java_ and _gradle_ nature
                if (workspaceProject.get().hasNature(JavaCore.NATURE_ID)) {
                    IJavaProject javaProject = JavaCore.create(workspaceProject.get());
                    GradleClasspathContainer.requestUpdateOf(javaProject);
                }
            } catch (CoreException e) {
                throw new GradlePluginsRuntimeException(e);
            }
        }
    }

    private static Set<FixedRequestAttributes> getUniqueRootProjectConfigurations(List<IProject> projects) {
        // todo (etst) filter for _open_ projects with _java_ and _gradle_ nature
        return FluentIterable.from(projects).transform(new Function<IProject, FixedRequestAttributes>() {

            @Override
            public FixedRequestAttributes apply(IProject project) {
                return CorePlugin.projectConfigurationManager().readProjectConfiguration(project).getRequestAttributes();
            }
        }).toSet();
    }

    private static List<OmniEclipseProject> collectAllGradleProjectsFromAllBuilds(Collection<OmniEclipseGradleBuild> eclipseGradleBuilds) {
        ImmutableList.Builder<OmniEclipseProject> result = new ImmutableList.Builder<OmniEclipseProject>();
        for (OmniEclipseGradleBuild build : eclipseGradleBuilds) {
            result.addAll(build.getRootEclipseProject().getAll());
        }
        return result.build();
    }

}
