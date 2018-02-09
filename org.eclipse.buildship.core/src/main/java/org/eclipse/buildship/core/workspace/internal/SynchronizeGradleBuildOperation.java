/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 473348
 */

package org.eclipse.buildship.core.workspace.internal;

import java.io.File;
import java.util.List;
import java.util.Set;

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
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.configuration.ConfigurationManager;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.omnimodel.OmniEclipseProject;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Synchronizes the given Gradle build with the Eclipse workspace. The algorithm is as follows:
 * <p/>
 * <ol>
 * <li>Uncouple all open workspace projects for which there is no corresponding Gradle project in the Gradle build anymore
 * <ol>
 * <li>the Gradle nature is removed</li>
 * <li>the derived resource markers are removed</li>
 * <li>the Gradle settings file is removed</li>
 * </ol>
 * </li>
 * <li>Synchronize all Gradle projects of the Gradle build with the Eclipse workspace project counterparts:
 * <ul>
 * <li>
 * If there is a project in the workspace at the location of the Gradle project, the synchronization is as follows:
 * <ol>
 * <li>If the workspace project is closed, the project is left unchanged</li>
 * <li>If the workspace project is open:
 * <ul>
 * <li>the project name is updated</li>
 * <li>the Gradle settings file is written</li>
 * <li>the linked resources are set</li>
 * <li>the derived resources are marked</li>
 * <li>the project natures and build commands are set</li>
 * <li>if the Gradle project is a Java project
 * <ul>
 * <li>the Java nature is added </li>
 * <li>the source compatibility settings are updated</li>
 * <li>the set of source folders is updated</li>
 * <li>the Gradle classpath container is updated</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ol>
 * </li>
 * <li>
 * If there is an Eclipse project at the location of the Gradle project, i.e. there is a .project file in that folder, then
 * the {@link NewProjectHandler} decides whether to import it and whether to keep or overwrite that existing .project file.
 * The imported project is then synchronized as specified above.
 * </li>
 * <li>If there is no project in the workspace, nor an Eclipse project at the location of the Gradle build, then
 * the {@link NewProjectHandler} decides whether to import it.
 * The imported project is then synchronized as specified above.
 * </li>
 * </ul>
 * </li>
 * </ol>
 *
 * <p/>
 * This operation changes resources. It will acquire the workspace scheduling rule to ensure an atomic operation.
 *
 */
final class SynchronizeGradleBuildOperation implements IWorkspaceRunnable {

    private final Set<OmniEclipseProject> allProjects;
    private final BuildConfiguration buildConfig;
    private final NewProjectHandler newProjectHandler;

    SynchronizeGradleBuildOperation(Set<OmniEclipseProject> allProjects, BuildConfiguration buildConfig, NewProjectHandler newProjectHandler) {
        this.allProjects = allProjects;
        this.buildConfig = buildConfig;
        this.newProjectHandler = newProjectHandler;
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setTaskName(String.format("Synchronizing Gradle build at %s", this.buildConfig.getRootProjectDirectory()));
        synchronizeProjectsWithWorkspace(progress);
    }

    private void synchronizeProjectsWithWorkspace(SubMonitor progress) throws CoreException {
        // collect Gradle projects and Eclipse workspace projects to sync
        List<IProject> decoupledWorkspaceProjects = getOpenWorkspaceProjectsRemovedFromGradleBuild();
        progress.setWorkRemaining(decoupledWorkspaceProjects.size() + this.allProjects.size());


        // uncouple the open workspace projects that do not have a corresponding Gradle project anymore
        for (IProject project : decoupledWorkspaceProjects) {
            uncoupleWorkspaceProjectFromGradle(project, progress.newChild(1));
        }

        // synchronize the Gradle projects with their corresponding workspace projects
        for (final OmniEclipseProject gradleProject : this.allProjects) {
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
        final Set<File> gradleProjectDirectories = FluentIterable.from(this.allProjects).transform(new Function<OmniEclipseProject, File>() {

            @Override
            public File apply(OmniEclipseProject gradleProject) {
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
                        return buildConfiguration.getRootProjectDirectory().equals(SynchronizeGradleBuildOperation.this.buildConfig.getRootProjectDirectory())
                                && (project.getLocation() == null || !gradleProjectDirectories.contains(project.getLocation().toFile()));
                    } else {
                        return false;
                    }
            }
        }).toList();
    }

    private void synchronizeGradleProjectWithWorkspaceProject(OmniEclipseProject project, SubMonitor progress) throws CoreException {
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

    private void synchronizeWorkspaceProject(OmniEclipseProject project, IProject workspaceProject, SubMonitor progress) throws CoreException {
        if (workspaceProject.isAccessible()) {
            synchronizeOpenWorkspaceProject(project, workspaceProject, true, progress);
        } else {
            synchronizeClosedWorkspaceProject(progress);
        }
    }

    private void synchronizeOpenWorkspaceProject(OmniEclipseProject project, IProject workspaceProject, boolean refreshNeeded, SubMonitor progress) throws CoreException {
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
        ProjectConfiguration projectConfig = configManager.createProjectConfiguration(this.buildConfig, project.getProjectDirectory());
        configManager.saveProjectConfiguration(projectConfig);

        workspaceProject = ProjectNameUpdater.updateProjectName(workspaceProject, project, this.allProjects, progress.newChild(1));

        CorePlugin.workspaceOperations().addNature(workspaceProject, GradleProjectNature.ID, progress.newChild(1));

        PersistentModelBuilder persistentModel = new PersistentModelBuilder(CorePlugin.modelPersistence().loadModel(workspaceProject));

        BuildScriptLocationUpdater.update(project, persistentModel, progress.newChild(1));
        LinkedResourcesUpdater.update(workspaceProject, project.getLinkedResources(), persistentModel, progress.newChild(1));
        GradleFolderUpdater.update(workspaceProject, project, persistentModel, progress.newChild(1));
        ProjectNatureUpdater.update(workspaceProject, project.getProjectNatures(), persistentModel, progress.newChild(1));
        BuildCommandUpdater.update(workspaceProject, project.getBuildCommands(), persistentModel, progress.newChild(1));

        if (isJavaProject(project)) {
            synchronizeJavaProject(project, workspaceProject, persistentModel, progress);
        } else {
            persistentModel.classpath(ImmutableList.<IClasspathEntry>of());
        }

        CorePlugin.modelPersistence().saveModel(persistentModel.build());
    }

    private void synchronizeJavaProject(final OmniEclipseProject project, final IProject workspaceProject, final PersistentModelBuilder persistentModel, SubMonitor progress) throws CoreException {
        JavaCore.run(new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                SubMonitor progress = SubMonitor.convert(monitor);
                synchronizeJavaProjectInTransaction(project, workspaceProject, persistentModel, progress);
            }
        }, progress.newChild(1));
    }

    private void synchronizeJavaProjectInTransaction(final OmniEclipseProject project, final IProject workspaceProject, PersistentModelBuilder persistentModel, SubMonitor progress) throws JavaModelException, CoreException {
        progress.setWorkRemaining(8);
        //old Gradle versions did not expose natures, so we need to add the Java nature explicitly
        CorePlugin.workspaceOperations().addNature(workspaceProject, JavaCore.NATURE_ID, progress.newChild(1));
        IJavaProject javaProject = JavaCore.create(workspaceProject);
        OutputLocationUpdater.update(javaProject, project.getOutputLocation(), progress.newChild(1));
        SourceFolderUpdater.update(javaProject, project.getSourceDirectories(), progress.newChild(1));
        LibraryFilter.update(javaProject, project, progress.newChild(1));
        ClasspathContainerUpdater.update(javaProject, project.getClasspathContainers(), project.getJavaSourceSettings().get(), progress.newChild(1));
        JavaSourceSettingsUpdater.update(javaProject, project, progress.newChild(1));
        GradleClasspathContainerUpdater.updateFromModel(javaProject, project, SynchronizeGradleBuildOperation.this.allProjects, persistentModel, progress.newChild(1));
        WtpClasspathUpdater.update(javaProject, project, progress.newChild(1));
        CorePlugin.externalLaunchConfigurationManager().updateClasspathProviders(workspaceProject);
    }

    private boolean isJavaProject(OmniEclipseProject project) {
        return project.getJavaSourceSettings().isPresent();
    }

    private void synchronizeClosedWorkspaceProject(SubMonitor childProgress) {
        // do not modify closed projects
    }

    private void synchronizeNonWorkspaceProject(OmniEclipseProject project, SubMonitor progress) throws CoreException {
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

    private IProject addExistingEclipseProjectToWorkspace(OmniEclipseProject project, IProjectDescription projectDescription, SubMonitor progress) throws CoreException {
        progress.setWorkRemaining(3);
        ProjectNameUpdater.ensureProjectNameIsFree(project, this.allProjects, progress.newChild(1));
        IProject workspaceProject = CorePlugin.workspaceOperations().includeProject(projectDescription, ImmutableList.<String>of(), progress.newChild(1));
        synchronizeOpenWorkspaceProject(project, workspaceProject, false, progress.newChild(1));
        return workspaceProject;
    }

    private IProject addNewEclipseProjectToWorkspace(OmniEclipseProject project, SubMonitor progress) throws CoreException {
        progress.setWorkRemaining(3);
        ProjectNameUpdater.ensureProjectNameIsFree(project, this.allProjects, progress.newChild(1));
        IProject workspaceProject = CorePlugin.workspaceOperations().createProject(project.getName(), project.getProjectDirectory(), ImmutableList.<String>of(), progress.newChild(1));
        synchronizeOpenWorkspaceProject(project, workspaceProject, false, progress.newChild(1));
        return workspaceProject;
    }

    private void uncoupleWorkspaceProjectFromGradle(IProject workspaceProject, SubMonitor monitor) {
        monitor.setWorkRemaining(3);
        monitor.subTask(String.format("Uncouple workspace project %s from Gradle", workspaceProject.getName()));
        CorePlugin.workspaceOperations().refreshProject(workspaceProject, monitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));
        CorePlugin.workspaceOperations().removeNature(workspaceProject, GradleProjectNature.ID, monitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));
        CorePlugin.modelPersistence().deleteModel(workspaceProject);
        CorePlugin.configurationManager().deleteProjectConfiguration(workspaceProject);
    }
}
