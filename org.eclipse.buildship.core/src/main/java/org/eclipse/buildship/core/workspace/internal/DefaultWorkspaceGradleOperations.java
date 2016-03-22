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
import com.google.common.collect.Lists;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.util.Maybe;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.util.file.RelativePathUtils;
import org.eclipse.buildship.core.util.predicate.Predicates;
import org.eclipse.buildship.core.workspace.GradleClasspathContainer;
import org.eclipse.buildship.core.workspace.NewProjectHandler;
import org.eclipse.buildship.core.workspace.WorkspaceGradleOperations;

/**
 * Default implementation of the {@link WorkspaceGradleOperations} interface.
 */
public final class DefaultWorkspaceGradleOperations implements WorkspaceGradleOperations {

    @Override
    public void synchronizeGradleBuildWithWorkspace(final OmniEclipseGradleBuild gradleBuild, final FixedRequestAttributes rootRequestAttributes, final NewProjectHandler newProjectHandler, IProgressMonitor monitor) {
        try {
            JavaCore.run(new IWorkspaceRunnable() {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    atomicallySynchronizeGradleBuildWithWorkspace(gradleBuild, rootRequestAttributes, newProjectHandler, monitor);
                }
            }, monitor);
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    private void atomicallySynchronizeGradleBuildWithWorkspace(OmniEclipseGradleBuild gradleBuild, FixedRequestAttributes rootRequestAttributes, NewProjectHandler newProjectHandler, IProgressMonitor monitor) {
        // collect Gradle projects and Eclipse workspace projects to sync
        List<OmniEclipseProject> allGradleProjects = gradleBuild.getRootEclipseProject().getAll();
        List<IProject> decoupledWorkspaceProjects = collectOpenWorkspaceProjectsRemovedFromGradleBuild(allGradleProjects, rootRequestAttributes);
        SubMonitor progress = SubMonitor.convert(monitor, decoupledWorkspaceProjects.size() + allGradleProjects.size());
        // uncouple the open workspace projects that do not have a corresponding Gradle project anymore
        for (IProject project : decoupledWorkspaceProjects) {
            uncoupleWorkspaceProjectFromGradle(project, progress.newChild(1));
        }
        // synchronize the Gradle projects with their corresponding workspace projects
        for (OmniEclipseProject gradleProject : allGradleProjects) {
            synchronizeGradleProjectWithWorkspaceProject(gradleProject, gradleBuild, rootRequestAttributes, newProjectHandler, progress.newChild(1));
        }
    }

    private List<IProject> collectOpenWorkspaceProjectsRemovedFromGradleBuild(List<OmniEclipseProject> gradleProjects, final FixedRequestAttributes rootRequestAttributes) {
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
                return projectConfiguration.getRequestAttributes().getProjectDir().equals(rootRequestAttributes.getProjectDir()) &&
                        (project.getLocation() == null || !gradleProjectDirectories.contains(project.getLocation().toFile()));
            }
        }).toList();
    }

    private void synchronizeGradleProjectWithWorkspaceProject(OmniEclipseProject project, OmniEclipseGradleBuild gradleBuild, FixedRequestAttributes rootRequestAttributes, NewProjectHandler newProjectHandler, SubMonitor progress) {
        progress.setWorkRemaining(1);
        progress.subTask(String.format("Synchronize Gradle project %s with workspace project", project.getName()));
        // check if a project already exists in the workspace at the location of the Gradle project to import
        Optional<IProject> workspaceProject = CorePlugin.workspaceOperations().findProjectByLocation(project.getProjectDirectory());
        SubMonitor childProgress = progress.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS);
        if (workspaceProject.isPresent()) {
            synchronizeWorkspaceProject(project, gradleBuild, workspaceProject.get(), rootRequestAttributes, childProgress);
        } else {
            if (project.getProjectDirectory().exists() && newProjectHandler.shouldImport(project)) {
                synchronizeNonWorkspaceProject(project, gradleBuild, rootRequestAttributes, newProjectHandler, childProgress);
            }
        }
    }

    private void synchronizeWorkspaceProject(OmniEclipseProject project, OmniEclipseGradleBuild gradleBuild, IProject workspaceProject, FixedRequestAttributes rootRequestAttributes, SubMonitor progress) {
        if (workspaceProject.isAccessible()) {
            synchronizeOpenWorkspaceProject(project, gradleBuild, workspaceProject, rootRequestAttributes, progress);
        } else {
            synchronizeClosedWorkspaceProject(progress);
        }
    }

    private void synchronizeOpenWorkspaceProject(OmniEclipseProject project, OmniEclipseGradleBuild gradleBuild, IProject workspaceProject, FixedRequestAttributes rootRequestAttributes, SubMonitor progress) {
        progress.setWorkRemaining(12);
        try {
            // sync the Eclipse project with the file system first
            CorePlugin.workspaceOperations().refreshProject(workspaceProject, progress.newChild(1));

            // update the project name in case the Gradle project name has changed
            workspaceProject = ProjectNameUpdater.updateProjectName(workspaceProject, project, gradleBuild, progress.newChild(1));

            // add Gradle nature, if needed
            CorePlugin.workspaceOperations().addNature(workspaceProject, GradleProjectNature.ID, progress.newChild(1));

            // persist the Gradle-specific configuration in the Eclipse project's .settings folder, if the configuration is available
            if (rootRequestAttributes != null) {
                ProjectConfiguration configuration = ProjectConfiguration.from(rootRequestAttributes, project);
                CorePlugin.projectConfigurationManager().saveProjectConfiguration(configuration, workspaceProject);
            }

            // update linked resources
            LinkedResourcesUpdater.update(workspaceProject, project.getLinkedResources(), progress.newChild(1));

            // mark derived folders
            markDerivedFolders(project, workspaceProject, progress.newChild(1));

            SubMonitor javaProgress = progress.newChild(4);
            if (isJavaProject(project)) {
                IJavaProject javaProject;
                if (hasJavaNature(workspaceProject)) {
                    javaProgress.newChild(1);
                    javaProject = JavaCore.create(workspaceProject);
                } else {
                    IPath jrePath = JavaRuntime.getDefaultJREContainerEntry().getPath();
                    IClasspathEntry classpathContainer = GradleClasspathContainer.newClasspathEntry();
                    javaProject = CorePlugin.workspaceOperations().createJavaProject(workspaceProject, jrePath, classpathContainer, javaProgress.newChild(1));
                }
                JavaSourceSettingsUpdater.update(javaProject, project.getJavaSourceSettings().get(), javaProgress.newChild(1));
                SourceFolderUpdater.update(javaProject, project.getSourceDirectories(), javaProgress.newChild(1));
                ClasspathContainerUpdater.updateFromModel(javaProject, project, javaProgress.newChild(1));
            }

            // set project natures and build commands
            ProjectNatureUpdater.update(workspaceProject, project.getProjectNatures(), progress.newChild(1));
            BuildCommandUpdater.update(workspaceProject, project.getBuildCommands(), progress.newChild(1));
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    private void synchronizeClosedWorkspaceProject(SubMonitor childProgress) {
        // do not modify closed projects
    }

    private void synchronizeNonWorkspaceProject(OmniEclipseProject project, OmniEclipseGradleBuild gradleBuild, FixedRequestAttributes rootRequestAttributes, NewProjectHandler newProjectHandler, SubMonitor progress) {
        progress.setWorkRemaining(2);
        IProject workspaceProject;

        // check if an Eclipse project already exists at the location of the Gradle project to import
        Optional<IProjectDescription> projectDescription = CorePlugin.workspaceOperations().findProjectDescriptor(project.getProjectDirectory(), progress.newChild(1));
        if (projectDescription.isPresent()) {
            if (newProjectHandler.shouldOverwriteDescriptor(projectDescription.get(), project)) {
                CorePlugin.workspaceOperations().deleteProjectDescriptors(project.getProjectDirectory());
                workspaceProject = addNewEclipseProjectToWorkspace(project, gradleBuild, rootRequestAttributes, progress.newChild(1));
            } else {
                workspaceProject = addExistingEclipseProjectToWorkspace(project, gradleBuild, projectDescription.get(), rootRequestAttributes, progress.newChild(1));
            }
        } else {
            workspaceProject = addNewEclipseProjectToWorkspace(project, gradleBuild, rootRequestAttributes, progress.newChild(1));
        }

        newProjectHandler.afterImport(workspaceProject, project);
    }

    private IProject addExistingEclipseProjectToWorkspace(OmniEclipseProject project, OmniEclipseGradleBuild gradleBuild, IProjectDescription projectDescription, FixedRequestAttributes rootRequestAttributes, SubMonitor progress) {
        progress.setWorkRemaining(3);
        ProjectNameUpdater.ensureProjectNameIsFree(project, gradleBuild, progress.newChild(1));
        IProject workspaceProject = CorePlugin.workspaceOperations().includeProject(projectDescription, ImmutableList.<String>of(), progress.newChild(1));
        synchronizeOpenWorkspaceProject(project, gradleBuild, workspaceProject, rootRequestAttributes, progress.newChild(1));
        return workspaceProject;
    }

    private IProject addNewEclipseProjectToWorkspace(OmniEclipseProject project, OmniEclipseGradleBuild gradleBuild, FixedRequestAttributes rootRequestAttributes, SubMonitor progress) {
        progress.setWorkRemaining(3);
        ProjectNameUpdater.ensureProjectNameIsFree(project, gradleBuild, progress.newChild(1));
        IProject workspaceProject = CorePlugin.workspaceOperations().createProject(project.getName(), project.getProjectDirectory(), ImmutableList.<String>of(), progress.newChild(1));
        synchronizeOpenWorkspaceProject(project, gradleBuild, workspaceProject, rootRequestAttributes, progress.newChild(1));
        return workspaceProject;
    }

    private List<IFolder> getSubProjectFolders(OmniEclipseProject project, final IProject workspaceProject) {
        return FluentIterable.from(project.getChildren()).transform(new Function<OmniEclipseProject, IFolder>() {
            @Override
            public IFolder apply(OmniEclipseProject childProject) {
                File dir = childProject.getProjectDirectory();
                IPath relativePath = RelativePathUtils.getRelativePath(workspaceProject.getLocation(), new Path(dir.getPath()));
                return workspaceProject.getFolder(relativePath);
            }
        }).toList();
    }

    private void markDerivedFolders(OmniEclipseProject gradleProject, IProject workspaceProject, SubMonitor progress) {
        List<String> derivedResources = Lists.newArrayList();

        derivedResources.add(".gradle");

        Optional<IFolder> possibleBuildDirectory = getBuildDirectory(gradleProject, workspaceProject);
        if (possibleBuildDirectory.isPresent()) {
            IFolder buildDirectory = possibleBuildDirectory.get();
            derivedResources.add(buildDirectory.getName());
            if (buildDirectory.exists()) {
                CorePlugin.workspaceOperations().markAsBuildFolder(buildDirectory);
            }
        }

        for (IFolder subProjectFolder : getSubProjectFolders(gradleProject, workspaceProject)) {
            derivedResources.add(subProjectFolder.getName());
            if (subProjectFolder.exists()) {
                CorePlugin.workspaceOperations().markAsSubProject(subProjectFolder);
            }
        }

        DerivedResourcesUpdater.update(workspaceProject, derivedResources, progress);
    }

    private Optional<IFolder> getBuildDirectory(OmniEclipseProject project, IProject workspaceProject) {
        OmniGradleProject gradleProject = project.getGradleProject();
        Maybe<File> buildDirectory = gradleProject.getBuildDirectory();
        if (buildDirectory.isPresent() && buildDirectory.get() != null) {
            IPath projectLocation = workspaceProject.getLocation();
            Path buildDirLocation = new Path(buildDirectory.get().getPath());
            if (projectLocation.isPrefixOf(buildDirLocation)) {
                IPath relativePath = RelativePathUtils.getRelativePath(projectLocation, buildDirLocation);
                return Optional.of(workspaceProject.getFolder(relativePath));
            } else {
                return Optional.absent();
            }
        } else {
            return Optional.of(workspaceProject.getFolder("build"));
        }
    }

    private boolean isJavaProject(OmniEclipseProject project) {
        return project.getJavaSourceSettings().isPresent();
    }

    private boolean hasJavaNature(IProject project) {
        try {
            return project.hasNature(JavaCore.NATURE_ID);
        } catch (CoreException e) {
            return false;
        }
    }

    private void uncoupleWorkspaceProjectFromGradle(IProject workspaceProject, SubMonitor monitor) {
        monitor.setWorkRemaining(3);
        monitor.subTask(String.format("Uncouple workspace project %s from Gradle", workspaceProject.getName()));
        CorePlugin.workspaceOperations().refreshProject(workspaceProject, monitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));
        CorePlugin.workspaceOperations().removeNature(workspaceProject, GradleProjectNature.ID, monitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));
        DerivedResourcesUpdater.clear(workspaceProject, monitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));
        CorePlugin.projectConfigurationManager().deleteProjectConfiguration(workspaceProject);
    }

}
