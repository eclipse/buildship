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

package org.eclipse.buildship.core.projectimport;

import java.io.File;
import java.util.List;

import org.gradle.tooling.ProgressListener;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;
import com.gradleware.tooling.toolingmodel.util.Pair;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.launching.JavaRuntime;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;
import org.eclipse.buildship.core.workspace.ClasspathDefinition;
import org.eclipse.buildship.core.workspace.WorkspaceOperations;

/**
 * Imports a Gradle project into Eclipse using the project import coordinates given by a
 * {@code ProjectImportConfiguration} instance.
 */
public final class ProjectImportJob extends ToolingApiWorkspaceJob {

    private final FixedRequestAttributes fixedAttributes;

    public ProjectImportJob(ProjectImportConfiguration configuration) {
        super("Importing Gradle project");

        this.fixedAttributes = configuration.toFixedAttributes();

        // explicitly show a dialog with the progress while the import is in process
        setUser(true);
    }

    @Override
    public void runToolingApiJobInWorkspace(IProgressMonitor monitor) {
        monitor.beginTask("Import Gradle project", 100);

        // all Java operations use the workspace root as a scheduling rule
        // see org.eclipse.jdt.internal.core.JavaModelOperation#getSchedulingRule()
        // if this rule ends during the import then other projects jobs see an
        // inconsistent workspace state, consequently we keep the rule for the whole import
        IJobManager manager = Job.getJobManager();
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        manager.beginRule(workspaceRoot, monitor);
        try {
            OmniEclipseGradleBuild eclipseGradleBuild = fetchEclipseGradleBuild(new SubProgressMonitor(monitor, 50));
            OmniEclipseProject rootProject = eclipseGradleBuild.getRootEclipseProject();
            List<OmniEclipseProject> allProjects = rootProject.getAll();
            for (OmniEclipseProject project : allProjects) {
                importProject(project, new SubProgressMonitor(monitor, 50 / allProjects.size()));
            }
        } finally {
            manager.endRule(workspaceRoot);
        }

        // monitor is closed by caller in super class
    }

    private OmniEclipseGradleBuild fetchEclipseGradleBuild(IProgressMonitor monitor) {
        monitor.beginTask("Load Eclipse Gradle project", IProgressMonitor.UNKNOWN);
        try {
            ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
            List<ProgressListener> listeners = ImmutableList.<ProgressListener> of(new DelegatingProgressListener(monitor));
            TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), null, listeners,
                    ImmutableList.<org.gradle.tooling.events.ProgressListener> of(), getToken());
            ModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(this.fixedAttributes);
            return repository.fetchEclipseGradleBuild(transientAttributes, FetchStrategy.FORCE_RELOAD);
        } finally {
            monitor.done();
        }
    }

    private void importProject(OmniEclipseProject project, IProgressMonitor monitor) {
        monitor.beginTask("Import project " + project.getName(), 4);
        try {
            // check if an Eclipse project already exists at the location of the Gradle project to import
            WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations();
            File projectDirectory = project.getProjectDirectory();
            Optional<IProjectDescription> projectDescription = workspaceOperations.findProjectInFolder(projectDirectory, new SubProgressMonitor(monitor, 1));

            List<File> childProjectLocations = collectChildProjectLocations(project);
            ImmutableList<String> gradleNature = ImmutableList.of(GradleProjectNature.ID);

            IProject workspaceProject;
            if (projectDescription.isPresent()) {
                // include the existing Eclipse project in the workspace
                workspaceProject = workspaceOperations.includeProject(projectDescription.get(), childProjectLocations, gradleNature, new SubProgressMonitor(monitor, 2));
            } else {
                // create a new Eclipse project in the workspace for the current Gradle project
                workspaceProject = workspaceOperations.createProject(project.getName(), project.getProjectDirectory(), childProjectLocations, gradleNature, new SubProgressMonitor(
                        monitor, 1));

                // if the current Gradle project is a Java project, configure the Java nature,
                // the classpath, and the source paths
                if (isJavaProject(project)) {
                    IPath jre = JavaRuntime.getDefaultJREContainerEntry().getPath();
                    ClasspathDefinition classpath = new ClasspathDefinition(ImmutableList.<Pair<IPath, IPath>>of(), ImmutableList.<IPath>of(), ImmutableList.<String>of(), jre);
                    workspaceOperations.createJavaProject(workspaceProject, classpath, new SubProgressMonitor(monitor, 1));
                } else {
                    monitor.worked(1);
                }
            }

            // persist the Gradle-specific configuration in the Eclipse project's .settings folder
            ProjectConfiguration projectConfiguration = ProjectConfiguration.from(this.fixedAttributes, project);
            CorePlugin.projectConfigurationManager().saveProjectConfiguration(projectConfiguration, workspaceProject);

            // refresh the project content
            workspaceOperations.refresh(workspaceProject, new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
    }

    private boolean isJavaProject(OmniEclipseProject modelProject) {
        return !modelProject.getSourceDirectories().isEmpty();
    }

    private List<File> collectChildProjectLocations(OmniEclipseProject project) {
        return FluentIterable.from(project.getChildren()).transform(new Function<OmniEclipseProject, File>() {

            @Override
            public File apply(OmniEclipseProject project) {
                return project.getProjectDirectory();
            }
        }).toList();
    }

}
