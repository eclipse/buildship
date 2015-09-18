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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.util.Maybe;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.gradle.Specs;
import org.eclipse.buildship.core.workspace.GradleClasspathContainer;
import org.eclipse.buildship.core.workspace.ProjectCreatedEvent;
import org.eclipse.buildship.core.workspace.WorkspaceGradleOperations;
import org.eclipse.buildship.core.workspace.WorkspaceOperations;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;

import java.io.File;
import java.util.List;

/**
 * Default implementation of the {@link WorkspaceGradleOperations} interface.
 */
public final class DefaultWorkspaceGradleOperations implements WorkspaceGradleOperations {

    @Override
    public void synchronizeGradleProjectWithWorkspaceProject(OmniEclipseProject project, OmniEclipseGradleBuild gradleBuild, FixedRequestAttributes rootRequestAttributes, List<String> workingSets, IProgressMonitor monitor) {
        monitor.beginTask(String.format("Synchronize Gradle project %s with workspace project", project.getName()), 2);
        try {
            Optional<IProject> workspaceProject = CorePlugin.workspaceOperations().findProjectByLocation(project.getProjectDirectory());
            if (workspaceProject.isPresent()) {
                synchronizeWorkspaceProject(project, workspaceProject.get(), rootRequestAttributes, new SubProgressMonitor(monitor, 1));
            } else {
                synchronizeNonWorkspaceProject(project, gradleBuild, rootRequestAttributes, workingSets, new SubProgressMonitor(monitor, 1));
            }
        } catch (CoreException e) {
            String message = String.format("Cannot synchronize Gradle project %s with workspace project.", project.getName());
            CorePlugin.logger().error(message, e);
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

    private void synchronizeWorkspaceProject(OmniEclipseProject project, IProject workspaceProject, FixedRequestAttributes rootRequestAttributes, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(String.format("Synchronize workspace Gradle project %s", project.getName()), 3);
        try {
            // do not modify closed projects
            if (!workspaceProject.isAccessible()) {
                return;
            }

            // add Gradle nature, if needed
            CorePlugin.workspaceOperations().addNature(workspaceProject, GradleProjectNature.ID, new SubProgressMonitor(monitor, 1));

            // persist the Gradle-specific configuration in the Eclipse project's .settings folder, if the configuration is available
            if (rootRequestAttributes != null) {
                ProjectConfiguration configuration = ProjectConfiguration.from(rootRequestAttributes, project);
                CorePlugin.projectConfigurationManager().saveProjectConfiguration(configuration, workspaceProject);
            }

            // update linked resources
            LinkedResourcesUpdater.update(workspaceProject, project.getLinkedResources(), new SubProgressMonitor(monitor, 1));

            // additional updates for Java projects
            if (hasJavaNature(workspaceProject)) {
                IJavaProject javaProject = JavaCore.create(workspaceProject);

                // update the sources
                SourceFolderUpdater.update(javaProject, project.getSourceDirectories(), new SubProgressMonitor(monitor, 1));

                // update project/external dependencies
                ClasspathContainerUpdater.update(javaProject, project, new SubProgressMonitor(monitor, 1));
            } else {
                monitor.worked(2);
            }
        } finally {
            monitor.done();
        }
    }

    private void synchronizeNonWorkspaceProject(OmniEclipseProject project, OmniEclipseGradleBuild gradleBuild, FixedRequestAttributes rootRequestAttributes, List<String> workingSets, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(String.format("Synchronize non-workspace Gradle project %s", project.getName()), 3);
        try {
            // check if an Eclipse project already exists at the location of the Gradle project to import
            WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations();
            File projectDirectory = project.getProjectDirectory();
            Optional<IProjectDescription> projectDescription = workspaceOperations.findProjectInFolder(projectDirectory, new SubProgressMonitor(monitor, 1));

            // collect all the sub folders to hide under the project
            List<File> filteredSubFolders = ImmutableList.<File>builder().
                    addAll(collectChildProjectLocations(project)).
                    add(getBuildDirectory(gradleBuild, project)).
                    add(getDotGradleDirectory(project)).build();
            ImmutableList<String> gradleNature = ImmutableList.of(GradleProjectNature.ID);

            IProject workspaceProject;
            if (projectDescription.isPresent()) {
                workspaceProject = addExistingEclipseProjectToWorkspace(project, projectDescription.get(), rootRequestAttributes, workspaceOperations, filteredSubFolders, gradleNature, monitor);
            } else {
                workspaceProject = addNewEclipseProjectToWorkspace(project, rootRequestAttributes, monitor, workspaceOperations, filteredSubFolders, gradleNature);
            }

            // notify the listeners that a new IProject has been created
            ProjectCreatedEvent event = new DefaultProjectCreatedEvent(workspaceProject, workingSets);
            CorePlugin.listenerRegistry().dispatch(event);
        } finally {
            monitor.done();
        }
    }

    private IProject addExistingEclipseProjectToWorkspace(OmniEclipseProject project, IProjectDescription projectDescription, FixedRequestAttributes rootRequestAttributes, WorkspaceOperations workspaceOperations, List<File> filteredSubFolders, ImmutableList<String> gradleNature, IProgressMonitor monitor) {
        // include the existing Eclipse project in the workspace
        IProject workspaceProject = workspaceOperations.includeProject(projectDescription, filteredSubFolders, gradleNature, new SubProgressMonitor(monitor, 2));

        // persist the Gradle-specific configuration in the Eclipse project's .settings folder
        ProjectConfiguration projectConfiguration = ProjectConfiguration.from(rootRequestAttributes, project);
        CorePlugin.projectConfigurationManager().saveProjectConfiguration(projectConfiguration, workspaceProject);

        return workspaceProject;
    }

    private IProject addNewEclipseProjectToWorkspace(OmniEclipseProject project, FixedRequestAttributes rootRequestAttributes, IProgressMonitor monitor, WorkspaceOperations workspaceOperations, List<File> filteredSubFolders, ImmutableList<String> gradleNature) throws CoreException {
        // create a new Eclipse project in the workspace for the current Gradle project
        IProject workspaceProject = workspaceOperations.createProject(project.getName(), project.getProjectDirectory(), filteredSubFolders, gradleNature, new SubProgressMonitor(monitor, 1));

        // persist the Gradle-specific configuration in the Eclipse project's .settings folder
        ProjectConfiguration projectConfiguration = ProjectConfiguration.from(rootRequestAttributes, project);
        CorePlugin.projectConfigurationManager().saveProjectConfiguration(projectConfiguration, workspaceProject);

        // set linked resources
        LinkedResourcesUpdater.update(workspaceProject, project.getLinkedResources(), new SubProgressMonitor(monitor, 1));

        // if the current Gradle project is a Java project, configure the Java nature, the classpath, and the source paths
        if (isJavaProject(project)) {
            IPath jrePath = JavaRuntime.getDefaultJREContainerEntry().getPath();
            IClasspathEntry classpathContainer = GradleClasspathContainer.newClasspathEntry();
            workspaceOperations.createJavaProject(workspaceProject, jrePath, classpathContainer, new SubProgressMonitor(monitor, 1));
        } else {
            monitor.worked(1);
        }

        return workspaceProject;
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

    private boolean isJavaProject(OmniEclipseProject project) {
        return !project.getSourceDirectories().isEmpty();
    }

    private boolean hasJavaNature(IProject project) {
        try {
            return project.hasNature(JavaCore.NATURE_ID);
        } catch (CoreException e) {
            return false;
        }
    }

    @Override
    public void makeWorkspaceProjectGradleUnaware(IProject workspaceProject, IProgressMonitor monitor) {
        monitor.beginTask("Detach Gradle specifics from project " + workspaceProject.getName(), 2);
        try {
            CorePlugin.workspaceOperations().removeNature(workspaceProject, GradleProjectNature.ID, new SubProgressMonitor(monitor, 1));
            CorePlugin.projectConfigurationManager().deleteProjectConfiguration(workspaceProject);
            ResourceFilter.detachAllFilters(workspaceProject, new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
    }

}
