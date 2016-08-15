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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import com.gradleware.tooling.toolingmodel.OmniEclipseLinkedResource;
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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.gradle.Predicates;
import org.eclipse.buildship.core.util.file.RelativePathUtils;
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
 * <li>the Gradle nature is set</li>
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
    private final FixedRequestAttributes build;
    private final NewProjectHandler newProjectHandler;

    SynchronizeGradleBuildOperation(Set<OmniEclipseProject> allProjects, FixedRequestAttributes build, NewProjectHandler newProjectHandler) {
        this.allProjects = allProjects;
        this.build = build;
        this.newProjectHandler = newProjectHandler;
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        JavaCore.run(new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                runInWorkspace(monitor);
            }
        }, monitor);
    };

    private void runInWorkspace(IProgressMonitor monitor) throws CoreException {
        // collect Gradle projects and Eclipse workspace projects to sync
        List<OmniEclipseProject> projectsInThisBuild = getProjectsInThisBuild();
        List<IProject> decoupledWorkspaceProjects = getOpenWorkspaceProjectsRemovedFromGradleBuild();
        SubMonitor progress = SubMonitor.convert(monitor, decoupledWorkspaceProjects.size() + projectsInThisBuild.size());

        progress.setTaskName(String.format("Synchronizing Gradle build at %s", this.build.getProjectDir()));

        // uncouple the open workspace projects that do not have a corresponding Gradle project anymore
        for (IProject project : decoupledWorkspaceProjects) {
            uncoupleWorkspaceProjectFromGradle(project, progress.newChild(1));
        }

        // synchronize the Gradle projects with their corresponding workspace projects
        for (OmniEclipseProject gradleProject : projectsInThisBuild) {
            synchronizeGradleProjectWithWorkspaceProject(gradleProject, progress.newChild(1));
        }
    }

    private List<IProject> getOpenWorkspaceProjectsRemovedFromGradleBuild() {
        // in the workspace, find all projects with a Gradle nature that belong to the same Gradle build (based on the root project directory) but
        // which do not match the location of one of the Gradle projects of that build
        final Set<File> gradleProjectDirectories = FluentIterable.from(getProjectsInThisBuild()).transform(new Function<OmniEclipseProject, File>() {

            @Override
            public File apply(OmniEclipseProject gradleProject) {
                return gradleProject.getProjectDirectory();
            }
        }).toSet();

        ImmutableList<IProject> allWorkspaceProjects = CorePlugin.workspaceOperations().getAllProjects();

        return FluentIterable.from(allWorkspaceProjects).filter(GradleProjectNature.isPresentOn()).filter(new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                Optional<ProjectConfiguration> projectConfiguration = CorePlugin.projectConfigurationManager().tryReadProjectConfiguration(project);
                return projectConfiguration.isPresent()
                        && projectConfiguration.get().toRequestAttributes().getProjectDir().equals(SynchronizeGradleBuildOperation.this.build.getProjectDir())
                        && (project.getLocation() == null || !gradleProjectDirectories.contains(project.getLocation().toFile()));
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
            if (project.getProjectDirectory().exists() && this.newProjectHandler.shouldImport(project)) {
                synchronizeNonWorkspaceProject(project, childProgress);
            }
        }
    }

    private void synchronizeWorkspaceProject(OmniEclipseProject project, IProject workspaceProject, SubMonitor progress) throws CoreException {
        if (workspaceProject.isAccessible()) {
            synchronizeOpenWorkspaceProject(project, workspaceProject, progress);
        } else {
            synchronizeClosedWorkspaceProject(progress);
        }
    }

    private void synchronizeOpenWorkspaceProject(OmniEclipseProject project, IProject workspaceProject, SubMonitor progress) throws CoreException {
        progress.setWorkRemaining(8);

        //currently lots of our synchronization logic assumes that the whole resource tree is readable.
        CorePlugin.workspaceOperations().refreshProject(workspaceProject, progress.newChild(1));

        workspaceProject = ProjectNameUpdater.updateProjectName(workspaceProject, project, this.allProjects, progress.newChild(1));

        CorePlugin.workspaceOperations().addNature(workspaceProject, GradleProjectNature.ID, progress.newChild(1));

        if (this.build != null) {
            ProjectConfiguration configuration = ProjectConfiguration.from(this.build, project);
            CorePlugin.projectConfigurationManager().saveProjectConfiguration(configuration, workspaceProject);
        }

        LinkedResourcesUpdater.update(workspaceProject, project.getLinkedResources(), progress.newChild(1));
        markGradleSpecificFolders(project, workspaceProject, progress.newChild(1));
        ProjectNatureUpdater.update(workspaceProject, project.getProjectNatures(), progress.newChild(1));
        BuildCommandUpdater.update(workspaceProject, project.getBuildCommands(), progress.newChild(1));

        if (isJavaProject(project)) {
            synchronizeOpenJavaProject(project, workspaceProject, progress.newChild(1));
        }
    }

    private void synchronizeOpenJavaProject(OmniEclipseProject project, IProject workspaceProject, SubMonitor progress) throws JavaModelException, CoreException {
        progress.setWorkRemaining(7);
        //old Gradle versions did not expose natures, so we need to add the Java nature explicitly
        CorePlugin.workspaceOperations().addNature(workspaceProject, JavaCore.NATURE_ID, progress.newChild(1));
        IJavaProject javaProject = JavaCore.create(workspaceProject);
        OutputLocationUpdater.update(javaProject, project.getOutputLocation(), progress.newChild(1));
        SourceFolderUpdater.update(javaProject, project.getSourceDirectories(), progress.newChild(1));
        JavaSourceSettingsUpdater.update(javaProject, project, progress.newChild(1));
        ClasspathContainerUpdater.update(javaProject, project.getClasspathContainers(), progress.newChild(1));
        GradleClasspathContainerUpdater.updateFromModel(javaProject, project, this.allProjects, progress.newChild(1));
        WtpClasspathUpdater.update(javaProject, project, progress.newChild(1));
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
            if (this.newProjectHandler.shouldOverwriteDescriptor(projectDescription.get(), project)) {
                CorePlugin.workspaceOperations().deleteProjectDescriptors(project.getProjectDirectory());
                workspaceProject = addNewEclipseProjectToWorkspace(project, progress.newChild(1));
            } else {
                workspaceProject = addExistingEclipseProjectToWorkspace(project, projectDescription.get(), progress.newChild(1));
            }
        } else {
            workspaceProject = addNewEclipseProjectToWorkspace(project, progress.newChild(1));
        }

        this.newProjectHandler.afterImport(workspaceProject, project);
    }

    private IProject addExistingEclipseProjectToWorkspace(OmniEclipseProject project, IProjectDescription projectDescription, SubMonitor progress) throws CoreException {
        progress.setWorkRemaining(3);
        ProjectNameUpdater.ensureProjectNameIsFree(project, this.allProjects, progress.newChild(1));
        IProject workspaceProject = CorePlugin.workspaceOperations().includeProject(projectDescription, ImmutableList.<String>of(), progress.newChild(1));
        synchronizeOpenWorkspaceProject(project, workspaceProject, progress.newChild(1));
        return workspaceProject;
    }

    private IProject addNewEclipseProjectToWorkspace(OmniEclipseProject project, SubMonitor progress) throws CoreException {
        progress.setWorkRemaining(3);
        ProjectNameUpdater.ensureProjectNameIsFree(project, this.allProjects, progress.newChild(1));
        IProject workspaceProject = CorePlugin.workspaceOperations().createProject(project.getName(), project.getProjectDirectory(), ImmutableList.<String>of(), progress.newChild(1));
        synchronizeOpenWorkspaceProject(project, workspaceProject, progress.newChild(1));
        return workspaceProject;
    }

    private List<IFolder> getNestedSubProjectFolders(OmniEclipseProject project, final IProject workspaceProject) {
        List<IFolder> subProjectFolders = Lists.newArrayList();
        final IPath parentPath = workspaceProject.getLocation();
        for (OmniEclipseProject child : project.getChildren()) {
            IPath childPath = Path.fromOSString(child.getProjectDirectory().getPath());
            if (parentPath.isPrefixOf(childPath)) {
                IPath relativePath = RelativePathUtils.getRelativePath(parentPath, childPath);
                subProjectFolders.add(workspaceProject.getFolder(relativePath));
            }
        }
        return subProjectFolders;
    }

    private void markGradleSpecificFolders(OmniEclipseProject gradleProject, IProject workspaceProject, SubMonitor progress) {
        for (IFolder subProjectFolder : getNestedSubProjectFolders(gradleProject, workspaceProject)) {
            if (subProjectFolder.exists()) {
                CorePlugin.workspaceOperations().markAsSubProject(subProjectFolder);
            }
        }

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

        DerivedResourcesUpdater.update(workspaceProject, derivedResources, progress);
    }

    /*
     * If no build directory is available via the TAPI, use 'build'.
     * If build directory is physically contained in the project, use that folder.
     * If build directory is a linked resource, use the linked folder.
     * Optional.absent() if all of the above fail.
     */
    private Optional<IFolder> getBuildDirectory(OmniEclipseProject project, IProject workspaceProject) {
        OmniGradleProject gradleProject = project.getGradleProject();
        Maybe<File> buildDirectory = gradleProject.getBuildDirectory();
        if (buildDirectory.isPresent() && buildDirectory.get() != null) {
            Path buildDirLocation = new Path(buildDirectory.get().getPath());
            return normalizeBuildDirectory(buildDirLocation, workspaceProject, project);
        } else {
            return Optional.of(workspaceProject.getFolder("build"));
        }
    }

    private Optional<IFolder> normalizeBuildDirectory(Path buildDirLocation, IProject workspaceProject, OmniEclipseProject project) {
        IPath projectLocation = workspaceProject.getLocation();
        if (projectLocation.isPrefixOf(buildDirLocation)) {
            IPath relativePath = RelativePathUtils.getRelativePath(projectLocation, buildDirLocation);
            return Optional.of(workspaceProject.getFolder(relativePath));
        } else {
            for (OmniEclipseLinkedResource linkedResource : project.getLinkedResources()) {
                if (buildDirLocation.toString().equals(linkedResource.getLocation())) {
                    return Optional.of(workspaceProject.getFolder(linkedResource.getName()));
                }
            }
            return Optional.absent();
        }
    }

    private boolean isJavaProject(OmniEclipseProject project) {
        return project.getJavaSourceSettings().isPresent();
    }

    private void uncoupleWorkspaceProjectFromGradle(IProject workspaceProject, SubMonitor monitor) {
        monitor.setWorkRemaining(3);
        monitor.subTask(String.format("Uncouple workspace project %s from Gradle", workspaceProject.getName()));
        CorePlugin.workspaceOperations().refreshProject(workspaceProject, monitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));
        CorePlugin.workspaceOperations().removeNature(workspaceProject, GradleProjectNature.ID, monitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));
        DerivedResourcesUpdater.clear(workspaceProject, monitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));
        CorePlugin.projectConfigurationManager().deleteProjectConfiguration(workspaceProject);
    }

    private List<OmniEclipseProject> getProjectsInThisBuild() {
        return ImmutableList.copyOf(Iterables.<OmniEclipseProject>filter(this.allProjects, Predicates.isSubProjectOf(this.build.getProjectDir())));
    }

}
