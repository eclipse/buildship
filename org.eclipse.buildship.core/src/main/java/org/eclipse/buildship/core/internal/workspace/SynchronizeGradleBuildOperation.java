/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.configuration.ConfigurationManager;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.buildship.core.internal.configuration.ProjectConfiguration;

/**
 * Synchronizes the given Gradle build with the Eclipse workspace.
 */
public final class SynchronizeGradleBuildOperation {

    private final Set<EclipseProject> allProjects;
    private final InternalGradleBuild gradleBuild;
    private final NewProjectHandler newProjectHandler;
    private final ProjectConfigurators configurators;

    private List<SynchronizationProblem> failures;


    public SynchronizeGradleBuildOperation(Set<EclipseProject> allProjects, InternalGradleBuild gradleBuild, NewProjectHandler newProjectHandler, ProjectConfigurators configurators) {
        this.allProjects = allProjects;
        this.gradleBuild = gradleBuild;
        this.newProjectHandler = newProjectHandler;
        this.configurators = configurators;
    }

    public List<SynchronizationProblem> run(IProgressMonitor monitor) throws CoreException {
        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setTaskName(String.format("Synchronizing Gradle build at %s", this.gradleBuild.getBuildConfig().getRootProjectDirectory()));

        this.failures = new ArrayList<>();
        synchronizeProjectsWithWorkspace(progress);
        return this.failures;
    }

    private void synchronizeProjectsWithWorkspace(SubMonitor progress) throws CoreException {
        // collect Gradle projects and Eclipse workspace projects to sync
        List<IProject> decoupledWorkspaceProjects = getOpenWorkspaceProjectsRemovedFromGradleBuild();
        progress.setWorkRemaining(decoupledWorkspaceProjects.size() + this.allProjects.size() + 1);

        this.failures.addAll(this.configurators.initConfigurators(progress.newChild(1)));

        // uncouple the open workspace projects that do not have a corresponding Gradle project anymore
        for (IProject project : decoupledWorkspaceProjects) {
            uncoupleWorkspaceProjectFromGradle(project, progress.newChild(1));
        }

        // synchronize the Gradle projects with their corresponding workspace projects
        for (final EclipseProject gradleProject : this.allProjects) {
            ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    synchronizeGradleProjectWithWorkspaceProject(gradleProject, SubMonitor.convert(monitor));
                }
            }, progress.newChild(1));
        }
    }

    private List<IProject> getOpenWorkspaceProjectsRemovedFromGradleBuild() {
        // in the workspace, find all projects with a Gradle nature that belong to the same Gradle build (based on the root project directory) but
        // which do not match the location of one of the Gradle projects of that build
        final Set<File> gradleProjectDirectories = FluentIterable.from(this.allProjects).transform(new Function<EclipseProject, File>() {

            @Override
            public File apply(EclipseProject gradleProject) {
                return gradleProject.getProjectDirectory();
            }
        }).toSet();

        ImmutableList<IProject> allWorkspaceProjects = CorePlugin.workspaceOperations().getAllProjects();

        return FluentIterable.from(allWorkspaceProjects).filter(GradleProjectNature.isPresentOn()).filter(new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                    ProjectConfiguration projectConfiguration = CorePlugin.configurationManager().tryLoadProjectConfiguration(project);
                    if (projectConfiguration != null) {
                        BuildConfiguration buildConfiguration = projectConfiguration.getBuildConfiguration();
                        return buildConfiguration.getRootProjectDirectory().equals(SynchronizeGradleBuildOperation.this.gradleBuild.getBuildConfig().getRootProjectDirectory())
                                && (project.getLocation() == null || !gradleProjectDirectories.contains(project.getLocation().toFile()));
                    } else {
                        return false;
                    }
            }
        }).toList();
    }

    private void synchronizeGradleProjectWithWorkspaceProject(EclipseProject project, SubMonitor progress) throws CoreException {
        progress.setWorkRemaining(1);
        progress.subTask(String.format("Synchronize Gradle project %s with workspace project", project.getName()));
        // check if a project already exists in the workspace at the location of the Gradle project to import
        Optional<IProject> workspaceProject = CorePlugin.workspaceOperations().findProjectByLocation(project.getProjectDirectory());
        SubMonitor childProgress = progress.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS);
        if (workspaceProject.isPresent()) {
            synchronizeWorkspaceProject(project, workspaceProject.get(), childProgress);
        } else {
            if (project.getProjectDirectory().exists() && this.newProjectHandler.shouldImportNewProjects()) {
                synchronizeNonWorkspaceProject(project, childProgress);
            }
        }
    }

    private void synchronizeWorkspaceProject(EclipseProject project, IProject workspaceProject, SubMonitor progress) throws CoreException {
        if (workspaceProject.isAccessible()) {
            synchronizeOpenWorkspaceProject(project, workspaceProject, true, progress);
        } else {
            synchronizeClosedWorkspaceProject(progress);
        }
    }

    private void synchronizeOpenWorkspaceProject(EclipseProject project, IProject workspaceProject, boolean refreshNeeded, SubMonitor progress) throws CoreException {
        progress.setWorkRemaining(10);

        //currently lots of our synchronization logic assumes that the whole resource tree is readable.
        if (refreshNeeded) {
            CorePlugin.workspaceOperations().refreshProject(workspaceProject, progress.newChild(1));
        } else {
            progress.worked(1);
        }

        // save the project configuration; has to be called after workspace project is in sync with the file system
        // otherwise the Eclipse preferences API will throw BackingStoreException
        ConfigurationManager configManager = CorePlugin.configurationManager();
        ProjectConfiguration projectConfig = configManager.createProjectConfiguration(this.gradleBuild.getBuildConfig(), project.getProjectDirectory());
        configManager.saveProjectConfiguration(projectConfig);

        workspaceProject = ProjectNameUpdater.updateProjectName(workspaceProject, project, this.allProjects, progress.newChild(1));

        CorePlugin.workspaceOperations().addNature(workspaceProject, GradleProjectNature.ID, progress.newChild(1));

        this.failures.addAll(this.configurators.configureConfigurators(workspaceProject, progress.newChild(1)));
    }

    private void synchronizeClosedWorkspaceProject(SubMonitor childProgress) {
        // do not modify closed projects
    }

    private void synchronizeNonWorkspaceProject(EclipseProject project, SubMonitor progress) throws CoreException {
        progress.setWorkRemaining(2);
        IProject workspaceProject;

        // check if an Eclipse project already exists at the location of the Gradle project to import
        Optional<IProjectDescription> projectDescription = CorePlugin.workspaceOperations().findProjectDescriptor(project.getProjectDirectory(), progress.newChild(1));
        if (projectDescription.isPresent()) {
            workspaceProject = addExistingEclipseProjectToWorkspace(project, projectDescription.get(), progress.newChild(1));
        } else {
            workspaceProject = addNewEclipseProjectToWorkspace(project, progress.newChild(1));
        }

        this.newProjectHandler.afterProjectImported(workspaceProject);
    }

    private IProject addExistingEclipseProjectToWorkspace(EclipseProject project, IProjectDescription projectDescription, SubMonitor progress) throws CoreException {
        progress.setWorkRemaining(3);
        ProjectNameUpdater.ensureProjectNameIsFree(project, this.allProjects, progress.newChild(1));
        IProject workspaceProject = CorePlugin.workspaceOperations().includeProject(projectDescription, ImmutableList.<String>of(), progress.newChild(1));
        synchronizeOpenWorkspaceProject(project, workspaceProject, false, progress.newChild(1));
        return workspaceProject;
    }

    private IProject addNewEclipseProjectToWorkspace(EclipseProject project, SubMonitor progress) throws CoreException {
        progress.setWorkRemaining(3);
        ProjectNameUpdater.ensureProjectNameIsFree(project, this.allProjects, progress.newChild(1));
        IProject workspaceProject = CorePlugin.workspaceOperations().createProject(project.getName(), project.getProjectDirectory(), ImmutableList.<String>of(), progress.newChild(1));
        synchronizeOpenWorkspaceProject(project, workspaceProject, false, progress.newChild(1));
        return workspaceProject;
    }

    private void uncoupleWorkspaceProjectFromGradle(IProject workspaceProject, SubMonitor monitor) {
        monitor.setWorkRemaining(4);
        monitor.subTask(String.format("Uncouple workspace project %s from Gradle", workspaceProject.getName()));
        CorePlugin.workspaceOperations().refreshProject(workspaceProject, monitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));
        this.failures.addAll(this.configurators.unconfigureConfigurators(workspaceProject, monitor.newChild(1)));
        CorePlugin.workspaceOperations().removeNature(workspaceProject, GradleProjectNature.ID, monitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));
        CorePlugin.configurationManager().deleteProjectConfiguration(workspaceProject);
    }
}
