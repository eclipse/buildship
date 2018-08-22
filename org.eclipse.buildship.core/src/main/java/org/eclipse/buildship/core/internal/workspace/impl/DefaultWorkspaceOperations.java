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

package org.eclipse.buildship.core.internal.workspace.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.UnsupportedConfigurationException;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.buildship.core.internal.workspace.GradleNatureAddedEvent;
import org.eclipse.buildship.core.internal.workspace.WorkspaceOperations;

/**
 * Default implementation of the {@link WorkspaceOperations} interface.
 */
public class DefaultWorkspaceOperations implements WorkspaceOperations {

    @Override
    public ImmutableList<IProject> getAllProjects() {
        return ImmutableList.copyOf(ResourcesPlugin.getWorkspace().getRoot().getProjects());
    }

    @Override
    public Optional<IProject> findProjectByName(final String name) {
        return FluentIterable.from(getAllProjects()).firstMatch(new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                return project.getName().equals(name);
            }
        });
    }

    @Override
    public Optional<IProject> findProjectByLocation(final File directory) {
        return FluentIterable.from(getAllProjects()).firstMatch(new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                IPath location = project.getLocation();
                // since Eclipse 3.4 projects can be non-local and they could return null locations
                // for Buildship this is not the case, Gradle projects are always available on the
                // local file system
                return location != null && location.toFile().equals(directory);
            }
        });
    }

    @Override
    public Optional<IProjectDescription> findProjectDescriptor(File location, IProgressMonitor monitor) {
        IPath descriptorLocation = Path.fromOSString(location.getPath()).append(".project");
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            IProjectDescription projectDescription = workspace.loadProjectDescription(descriptorLocation);
            return Optional.of(projectDescription);
        } catch (CoreException e) {
            return Optional.absent();
        }
    }

    @Override
    public IProject createProject(String name, File location, List<String> natureIds, IProgressMonitor monitor) {
        // validate arguments
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(location);
        Preconditions.checkNotNull(natureIds);
        Preconditions.checkArgument(!name.isEmpty(), "Project name must not be empty.");
        Preconditions.checkArgument(location.isDirectory(), String.format("Project location %s must be a directory.", location));
        Preconditions.checkState(!findProjectByName(name).isPresent(), String.format("Workspace already contains a project with name %s.", name));

        SubMonitor progress = SubMonitor.convert(monitor, 3);
        try {
            // calculate the name and the project location
            validateProjectName(name, location);
            IPath projectLocation = normalizeProjectLocation(location);

            // get an IProject instance and create the project
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProjectDescription projectDescription = workspace.newProjectDescription(name);
            projectDescription.setLocation(projectLocation);
            projectDescription.setComment(String.format("Project %s created by Buildship.", name));
            IProject project = workspace.getRoot().getProject(name);
            project.create(projectDescription, progress.newChild(1));

            // open the project
            project.open(IResource.NONE, progress.newChild(1));

            // add project natures separately to trigger IProjectNature#configure
            // the project needs to be open while the natures are added
            SubMonitor natureProgress = progress.newChild(1).setWorkRemaining(natureIds.size());
            for (String natureId : natureIds) {
                addNature(project, natureId, natureProgress.newChild(1));
            }

            // return the created, open project
            return project;
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    @Override
    public IProject includeProject(IProjectDescription projectDescription, List<String> extraNatureIds, IProgressMonitor monitor) {
        // validate arguments
        Preconditions.checkNotNull(projectDescription);
        Preconditions.checkNotNull(extraNatureIds);
        String projectName = projectDescription.getName();
        Preconditions.checkState(!findProjectByName(projectName).isPresent(), String.format("Workspace already contains a project with name %s.", projectName));

        SubMonitor progress = SubMonitor.convert(monitor, 3);
        try {
            // include the project in the workspace
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProject project = workspace.getRoot().getProject(projectName);
            project.create(projectDescription, progress.newChild(1));

            // open the project
            project.open(IResource.NONE, progress.newChild(1));

            // add project natures separately to trigger IProjectNature#configure
            // the project needs to be open while the natures are added
            SubMonitor natureProgress = progress.newChild(1).setWorkRemaining(extraNatureIds.size());
            for (String natureId : extraNatureIds) {
                addNature(project, natureId, natureProgress.newChild(1));
            }

            // return the included, open project
            return project;
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        } finally {
            monitor.done();
        }
    }

    @Override
    public void refreshProject(IProject project, IProgressMonitor monitor) {
        // validate arguments
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(project.isAccessible(), "Project must be open.");
        try {
            project.refreshLocal(IProject.DEPTH_INFINITE, monitor);
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    @Override
    public void validateProjectName(String desiredName, File location) {
        Preconditions.checkNotNull(desiredName);
        Preconditions.checkNotNull(location);
        if (isDirectChildOfWorkspaceRootFolder(location) && !location.getName().equals(desiredName))  {
            throw new UnsupportedConfigurationException(String.format("Project at '%s' can't be named '%s' because it's located directly under the workspace"
                    + " root. If such a project is renamed, Eclipse would move the container directory. To resolve this problem, move the project out of "
                    + "the workspace root or configure it to have the name '%s'.", location.getAbsolutePath(), desiredName, location.getName()));
        }
    }

    /*
     * To put a project in the 'default location' (direct child of the workspace root),
     * its location attribute must be set to null. Otherwise Eclipse <4.4 will throw a validation error.
     */
    private IPath normalizeProjectLocation(File location) {
        return isDirectChildOfWorkspaceRootFolder(location) ? null : Path.fromOSString(location.getPath());
    }

    private boolean isDirectChildOfWorkspaceRootFolder(File location) {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IPath rootLocationPath = workspace.getRoot().getLocation();
        IPath locationPath = Path.fromOSString(location.getPath());
        return rootLocationPath.equals(locationPath) || rootLocationPath.equals(locationPath.removeLastSegments(1));
    }

    @Override
    public void addNature(IProject project, String natureId, IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, 1);
        try {
            // get the description
            IProjectDescription description = project.getDescription();

            // abort if the project already has the nature applied or the nature is not defined
            List<String> currentNatureIds = ImmutableList.copyOf(description.getNatureIds());
            if (currentNatureIds.contains(natureId) || !isNatureRecognizedByEclipse(natureId)) {
                return;
            }

            // add the nature to the project
            ImmutableList<String> newIds = ImmutableList.<String>builder().addAll(currentNatureIds).add(natureId).build();
            description.setNatureIds(newIds.toArray(new String[newIds.size()]));

            // save the updated description
            project.setDescription(description, progress.newChild(1));

            // if the currently set nature is the Gradle nature then publish the appropriate event
            if (GradleProjectNature.ID.equals(natureId)) {
                CorePlugin.listenerRegistry().dispatch(new GradleNatureAddedEvent(project));
            }
        } catch (CoreException e) {
            String message = String.format("Cannot add nature %s to Eclipse project %s.", natureId, project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        }
    }

    @Override
    public boolean isNatureRecognizedByEclipse(String natureId) {
        // if a description contains a nature id not defined by any of the Eclipse plugins then setting
        // it on a project throws an exception
        return ResourcesPlugin.getWorkspace().getNatureDescriptor(natureId) != null;
    }

    @Override
    public void removeNature(IProject project, String natureId, IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, 1);
        try {
            // get the description
            IProjectDescription description = project.getDescription();

            // abort if the project currently does not have the nature applied
            List<String> currentNatureIds = ImmutableList.copyOf(description.getNatureIds());
            if (!currentNatureIds.contains(natureId)) {
                return;
            }

            // remove the nature from the project
            List<String> newIds = new ArrayList<>(currentNatureIds);
            newIds.remove(natureId);
            description.setNatureIds(newIds.toArray(new String[newIds.size()]));

            // save the updated description
            project.setDescription(description, progress.newChild(1));
        } catch (CoreException e) {
            String message = String.format("Cannot remove nature %s from Eclipse project %s.", natureId, project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        }
    }

    @Override
    public void addBuildCommand(IProject project, String name, Map<String, String> arguments, IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, 1);
        try {
            IProjectDescription description = project.getDescription();
            List<ICommand> buildCommands = Lists.newArrayList(description.getBuildSpec());
            for (int i = 0; i < buildCommands.size(); i++) {
                ICommand buildCommand = buildCommands.get(i);
                if (buildCommand.getBuilderName().equals(name)) {
                    if (buildCommand.getArguments().equals(arguments)) {
                        return;
                    } else {
                        buildCommands.set(i, createCommand(description, name, arguments));
                        setNewBuildCommands(project, description, buildCommands, progress.newChild(1));
                        return;
                    }
                }
            }

            // if the build command didn't exist before then create a new command instance and assign it to the project
            buildCommands.add(createCommand(description, name, arguments));
            setNewBuildCommands(project, description, buildCommands, progress.newChild(1));
        } catch (CoreException e) {
            String message = String.format("Cannot add build command %s with arguments %s to Eclipse project %s.", name, arguments, project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        }
    }

    private ICommand createCommand(IProjectDescription description, String name, Map<String, String> arguments) {
        ICommand command = description.newCommand();
        command.setBuilderName(name);
        command.setArguments(ImmutableMap.copyOf(arguments));
        return command;
    }

    private void setNewBuildCommands(IProject project, IProjectDescription description, List<ICommand> buildCommands, IProgressMonitor monitor) throws CoreException {
        description.setBuildSpec(buildCommands.toArray(new ICommand[buildCommands.size()]));
        project.setDescription(description, monitor);
    }

    @Override
    public void removeBuildCommand(IProject project, final String name, IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, 1);
        try {
            IProjectDescription description = project.getDescription();
            ImmutableList<ICommand> existingCommands = ImmutableList.copyOf(description.getBuildSpec());

            // remove the build command based on the name
            ImmutableList<ICommand> updatedCommands = FluentIterable.from(existingCommands).filter(new Predicate<ICommand>() {

                @Override
                public boolean apply(ICommand command) {
                    return !command.getBuilderName().equals(name);
                }
            }).toList();

            // only update the project description if the build command to remove exists
            SubMonitor updateProgress = progress.newChild(1);
            if (existingCommands.size() != updatedCommands.size()) {
                description.setBuildSpec(updatedCommands.toArray(new ICommand[updatedCommands.size()]));
                project.setDescription(description, updateProgress);
            }
        } catch (CoreException e) {
            String message = String.format("Cannot remove build command %s from Eclipse project %s.", name, project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        }
    }

    @Override
    public IProject renameProject(IProject project, String newName, IProgressMonitor monitor) {
        Preconditions.checkNotNull(project);
        Preconditions.checkNotNull(newName);
        Preconditions.checkArgument(project.isAccessible(), "Project must be open.");

        SubMonitor progress = SubMonitor.convert(monitor, 1);

        if (project.getName().equals(newName)) {
            return project;
        }

        IPath location = project.getLocation();
        if (location != null && isDirectChildOfWorkspaceRootFolder(location.toFile())) {
            throw new GradlePluginsRuntimeException(String.format("Project %s cannot be renamed, because it is in the default location.", project.getName()));
        }

        if (findProjectByName(newName).isPresent()) {
            throw new GradlePluginsRuntimeException(String.format("Workspace already contains a project with name %s.", newName));
        }

        try {
            IProjectDescription description = project.getDescription();
            description.setName(newName);
            project.move(description, false, progress.newChild(1));
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        }
        return findProjectByName(newName).get();
    }

    @Override
    public boolean isWtpInstalled() {
        return Platform.getBundle("org.eclipse.wst.common.core") != null;
    }

}
