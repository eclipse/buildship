/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 472223
 */

package org.eclipse.buildship.core.projectimport;

import java.io.File;
import java.util.List;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.gradle.Specs;
import org.eclipse.buildship.core.projectimport.internal.DefaultProjectCreatedEvent;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;
import org.eclipse.buildship.core.util.workspace.WorkspaceUtils;
import org.eclipse.buildship.core.workspace.WorkspaceOperations;
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
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.OmniGradleProjectStructure;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;
import com.gradleware.tooling.toolingmodel.util.Maybe;

/**
 * Imports a Gradle project into Eclipse using the project import coordinates given by a
 * {@code ProjectImportConfiguration} instance.
 */
public final class ProjectImportJob extends ToolingApiWorkspaceJob {

    private FixedRequestAttributes fixedAttributes;
    private final ImmutableList<String> workingSets;
    private final AsyncHandler initializer;
    private ProjectImportConfiguration configuration;

    public ProjectImportJob(ProjectImportConfiguration configuration, AsyncHandler initializer) {
        super("Importing Gradle project");
        this.configuration = configuration;

        // extract the required data from the mutable configuration object
        this.workingSets = configuration.getApplyWorkingSets().getValue() ? ImmutableList.copyOf(configuration.getWorkingSets().getValue()) : ImmutableList.<String>of();
        this.initializer = Preconditions.checkNotNull(initializer);

        // explicitly show a dialog with the progress while the import is in process
        setUser(true);
    }

    @Override
    public void runToolingApiJobInWorkspace(IProgressMonitor monitor) {
        monitor.beginTask("Import Gradle project", 100);

        this.initializer.run(new SubProgressMonitor(monitor, 10));

        // all Java operations use the workspace root as a scheduling rule
        // see org.eclipse.jdt.internal.core.JavaModelOperation#getSchedulingRule()
        // if this rule ends during the import then other projects jobs see an
        // inconsistent workspace state, consequently we keep the rule for the whole import
        IJobManager manager = Job.getJobManager();
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        manager.beginRule(workspaceRoot, monitor);
        try {
            // in case the project dir is in the workspace folder the root
            // folder name has to be change to the "rootProject.name", which is
            // defined in the settings.gradle file. See Bug 472223
            if (WorkspaceUtils.isInWorkspaceFolder(this.configuration.getProjectDir().getValue())) {
                changeProjectRootFolderName(monitor);
            }
            // create the fixedAttributes after the project dir might have
            // changed
            this.fixedAttributes = this.configuration.toFixedAttributes();
            OmniEclipseGradleBuild eclipseGradleBuild = fetchEclipseGradleBuild(new SubProgressMonitor(monitor, 50));
            OmniEclipseProject rootProject = eclipseGradleBuild.getRootEclipseProject();
            List<OmniEclipseProject> allProjects = rootProject.getAll();
            for (OmniEclipseProject project : allProjects) {
                importProject(project, eclipseGradleBuild, new SubProgressMonitor(monitor, 50 / allProjects.size()));
            }
        } finally {
            manager.endRule(workspaceRoot);
        }

        // monitor is closed by caller in super class
    }

    private void changeProjectRootFolderName(IProgressMonitor monitor) {
        File projectDir = this.fixedAttributes.getProjectDir();
            OmniGradleBuildStructure gradleBuildStructure = fetchGradleBuildStructure(monitor);
            OmniGradleProjectStructure rootProject = gradleBuildStructure.getRootProject();
            if (!(projectDir.getName().equals(rootProject.getName()))) {
                File newProjectDir = new File(projectDir.getParentFile(), rootProject.getName());
                projectDir.renameTo(newProjectDir);
            // set newProjectDir in the config
            this.configuration.setProjectDir(newProjectDir);
            }
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

    private OmniGradleBuildStructure fetchGradleBuildStructure(IProgressMonitor monitor) {
        monitor.beginTask("Load Gradle Project Structure", IProgressMonitor.UNKNOWN);
        try {
            ProcessStreams stream = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
            TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, stream.getOutput(),
                    stream.getError(), null, ImmutableList.<ProgressListener> of(),
                    ImmutableList.<org.gradle.tooling.events.ProgressListener> of(), getToken());
            ModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(this.fixedAttributes);
            return repository.fetchGradleBuildStructure(transientAttributes, FetchStrategy.FORCE_RELOAD);
        } finally {
            monitor.done();
        }
    }

    private void importProject(OmniEclipseProject project, OmniEclipseGradleBuild eclipseGradleBuild, IProgressMonitor monitor) {
        monitor.beginTask("Import project " + project.getName(), 3);
        try {
            // check if an Eclipse project already exists at the location of the Gradle project to import
            WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations();
            File projectDirectory = project.getProjectDirectory();
            Optional<IProjectDescription> projectDescription = workspaceOperations.findProjectInFolder(projectDirectory, new SubProgressMonitor(monitor, 1));

            // collect all the sub folders to hide under the project
            List<File> filteredSubFolders = ImmutableList.<File> builder().
                    addAll(collectChildProjectLocations(project)).
                    add(getBuildDirectory(eclipseGradleBuild, project)).
                    add(getDotGradleDirectory(project)).
                    build();
            ImmutableList<String> gradleNature = ImmutableList.of(GradleProjectNature.ID);

            IProject workspaceProject;
            if (projectDescription.isPresent()) {
                // include the existing Eclipse project in the workspace
                workspaceProject = workspaceOperations.includeProject(projectDescription.get(), filteredSubFolders, gradleNature, new SubProgressMonitor(monitor, 2));
            } else {
                // create a new Eclipse project in the workspace for the current Gradle project
                workspaceProject = workspaceOperations.createProject(project.getName(), project.getProjectDirectory(), filteredSubFolders, gradleNature, new SubProgressMonitor(monitor, 1));

                // if the current Gradle project is a Java project, configure the Java nature, the classpath, and the source paths
                if (isJavaProject(project)) {
                    IPath jrePath = JavaRuntime.getDefaultJREContainerEntry().getPath();
                    workspaceOperations.createJavaProject(workspaceProject, jrePath, new SubProgressMonitor(monitor, 1));
                } else {
                    monitor.worked(1);
                }
            }

            // persist the Gradle-specific configuration in the Eclipse project's .settings folder
            ProjectConfiguration projectConfiguration = ProjectConfiguration.from(this.fixedAttributes, project);
            CorePlugin.projectConfigurationManager().saveProjectConfiguration(projectConfiguration, workspaceProject);

            // notify the listeners that a new IProject has been created
            ProjectCreatedEvent event = new DefaultProjectCreatedEvent(workspaceProject, this.workingSets);
            CorePlugin.listenerRegistry().dispatch(event);
        } finally {
            monitor.done();
        }
    }

    private List<File> collectChildProjectLocations(OmniEclipseProject project) {
        return FluentIterable.from(project.getChildren()).transform(new Function<OmniEclipseProject, File>() {

            @Override
            public File apply(OmniEclipseProject project) {
                return project.getProjectDirectory();
            }
        }).toList();
    }

    private File getBuildDirectory(OmniEclipseGradleBuild eclipseGradleBuild, OmniEclipseProject project) {
        Optional<OmniGradleProject> gradleProject = eclipseGradleBuild.getRootProject().tryFind(Specs.gradleProjectMatchesProjectPath(project.getPath()));
        Maybe<File> buildScript = gradleProject.get().getBuildDirectory();
        if (buildScript.isPresent() && buildScript.get() != null) {
            return buildScript.get();
        } else {
            return new File(project.getProjectDirectory(), "build");
        }
    }

    private File getDotGradleDirectory(OmniEclipseProject project) {
        return new File(project.getProjectDirectory(), ".gradle");
    }

    private boolean isJavaProject(OmniEclipseProject modelProject) {
        return !modelProject.getSourceDirectories().isEmpty();
    }

}
