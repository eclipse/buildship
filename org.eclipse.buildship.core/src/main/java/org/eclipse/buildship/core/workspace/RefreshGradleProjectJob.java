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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepositoryProvider;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.util.predicate.Predicates;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.gradle.tooling.ProgressListener;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Forces the reload of the given Gradle (multi-)project and refreshes all affected workspace projects accordingly.
 */
public final class RefreshGradleProjectJob extends ToolingApiWorkspaceJob {

    private final FixedRequestAttributes rootRequestAttributes;

    public RefreshGradleProjectJob(FixedRequestAttributes rootRequestAttributes) {
        super("Reload root project at " + Preconditions.checkNotNull(rootRequestAttributes).getProjectDir().getAbsolutePath(), false);
        this.rootRequestAttributes = rootRequestAttributes;
    }

    @Override
    protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) {
        monitor.beginTask("Refresh Gradle project and Eclipse workspace", 100);

        // use the same rule as the ProjectImportJob to do the initialization
        IJobManager manager = Job.getJobManager();
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        manager.beginRule(workspaceRoot, monitor);
        try {
            OmniEclipseGradleBuild result = forceReloadEclipseGradleBuild(this.rootRequestAttributes, new SubProgressMonitor(monitor, 50));
            synchronizeGradleProjectsWithWorkspace(result, new SubProgressMonitor(monitor, 50));
        } finally {
            manager.endRule(workspaceRoot);
        }

        // monitor is closed by caller in super class
    }

    private OmniEclipseGradleBuild forceReloadEclipseGradleBuild(FixedRequestAttributes requestAttributes, IProgressMonitor monitor) {
        monitor.beginTask(String.format("Force reload of Gradle build located at %s", requestAttributes.getProjectDir().getAbsolutePath()), IProgressMonitor.UNKNOWN);
        try {
            ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
            ImmutableList<ProgressListener> listeners = ImmutableList.<ProgressListener>of(new DelegatingProgressListener(monitor));
            TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), streams.getInput(), listeners,
                    ImmutableList.<org.gradle.tooling.events.ProgressListener>of(), getToken());
            ModelRepositoryProvider repository = CorePlugin.modelRepositoryProvider();
            return repository.getModelRepository(requestAttributes).fetchEclipseGradleBuild(transientAttributes, FetchStrategy.FORCE_RELOAD);
        } finally {
            monitor.done();
        }
    }

    private void synchronizeGradleProjectsWithWorkspace(OmniEclipseGradleBuild gradleBuild, IProgressMonitor monitor) {
        // collect added and removed projects
        List<OmniEclipseProject> allGradleProjects = gradleBuild.getRootEclipseProject().getAll();
        List<IProject> oldWorkspaceProjects = collectWorkspaceProjectsRemovedFromGradleBuild(allGradleProjects);

        monitor.beginTask("Synchronize Gradle projects with workspace", oldWorkspaceProjects.size() + allGradleProjects.size());
        try {
            // remove the workspace projects that do not have a corresponding Gradle project anymore
            for (IProject oldProject : oldWorkspaceProjects) {
                makeWorkspaceProjectGradleUnaware(oldProject, new SubProgressMonitor(monitor, 1));
            }
            // synchronize the Gradle projects with their corresponding workspace projects
            for (OmniEclipseProject gradleProject : allGradleProjects) {
                synchronizeGradleProjectWithWorkspaceProject(gradleProject, gradleBuild, new SubProgressMonitor(monitor, 1));
            }
        } finally {
            monitor.done();
        }
    }

    private List<IProject> collectWorkspaceProjectsRemovedFromGradleBuild(List<OmniEclipseProject> gradleProjects) {
        // in the workspace, find all projects with a Gradle nature that belong to the same Gradle build (based on the root project directory) but
        // which do not match the location of one of the Gradle projects of that build
        final Set<File> gradleProjectDirectories = FluentIterable.from(gradleProjects).transform(new Function<OmniEclipseProject, File>() {

            @Override
            public File apply(OmniEclipseProject gradleProject) {
                return gradleProject.getProjectDirectory();
            }
        }).toSet();

        ImmutableList<IProject> allWorkspaceProjects = CorePlugin.workspaceOperations().getAllProjects();
        return FluentIterable.from(allWorkspaceProjects).filter(Predicates.accessibleGradleProject()).filter(new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                ProjectConfiguration projectConfiguration = CorePlugin.projectConfigurationManager().readProjectConfiguration(project);
                return projectConfiguration.getRequestAttributes().getProjectDir().equals(RefreshGradleProjectJob.this.rootRequestAttributes.getProjectDir()) &&
                        (project.getLocation() != null && !gradleProjectDirectories.contains(project.getLocation().toFile()));
            }
        }).toList();
    }

    private void makeWorkspaceProjectGradleUnaware(IProject project, IProgressMonitor monitor) {
        monitor.beginTask(String.format("Remove project %s", project.getName()), 1);
        try {
            CorePlugin.workspaceGradleOperations().makeProjectGradleUnaware(project, new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
    }

    private void synchronizeGradleProjectWithWorkspaceProject(OmniEclipseProject gradleProject, OmniEclipseGradleBuild gradleBuild, IProgressMonitor monitor) {
        monitor.beginTask(String.format("Update project %s", gradleProject.getName()), 1);
        try {
            CorePlugin.workspaceGradleOperations().updateProjectInWorkspace(gradleProject, gradleBuild, this.rootRequestAttributes, ImmutableList.<String>of(), new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
    }

    @Override
    public boolean belongsTo(Object family) {
        // associate with a family so we can cancel all builds of
        // this type at once through the Eclipse progress manager
        return RefreshGradleProjectJob.class.getName().equals(family);
    }

}
