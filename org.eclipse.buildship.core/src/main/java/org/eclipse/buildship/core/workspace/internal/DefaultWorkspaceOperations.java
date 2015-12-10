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

package org.eclipse.buildship.core.workspace.internal;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.util.object.MoreObjects;
import org.eclipse.buildship.core.workspace.WorkspaceOperations;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the {@link WorkspaceOperations} interface.
 */
public final class DefaultWorkspaceOperations implements WorkspaceOperations {

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
    public Optional<IProjectDescription> findProjectInFolder(File location, IProgressMonitor monitor) {
        if (location == null || !location.exists()) {
            return Optional.absent();
        }

        File dotProjectFile = new File(location, ".project");
        if (!dotProjectFile.exists() || !dotProjectFile.isFile()) {
            return Optional.absent();
        }

        try {
            // calculate the project location
            IPath projectLocation = resolveProjectLocation(location);

            // get, configure, and return the project description
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            FileInputStream dotProjectStream = new FileInputStream(dotProjectFile);
            IProjectDescription projectDescription = workspace.loadProjectDescription(dotProjectStream);
            projectDescription.setLocation(projectLocation);
            return Optional.of(projectDescription);
        } catch (Exception e) {
            String message = String.format("Cannot open existing Eclipse project from %s.", dotProjectFile.getAbsolutePath());
            throw new GradlePluginsRuntimeException(message, e);
        }
    }

    @Override
    public void deleteAllProjects(IProgressMonitor monitor) {
        monitor = MoreObjects.firstNonNull(monitor, new NullProgressMonitor());
        monitor.beginTask("Delete all Eclipse projects from workspace", 100);
        try {
            List<IProject> allProjects = getAllProjects();
            for (IProject project : allProjects) {
                try {
                    // don't delete the project from the file system, only from the workspace
                    // moreover, force the removal even if the object is out-of-sync with the file system
                    project.delete(false, true, new SubProgressMonitor(monitor, 100 / allProjects.size()));
                } catch (Exception e) {
                    String message = String.format("Cannot delete project %s.", project.getName());
                    throw new GradlePluginsRuntimeException(message, e);
                }
            }
        } finally {
            monitor.done();
        }
    }

    @Override
    public IProject createProject(String name, File location, List<String> natureIds, IProgressMonitor monitor) {
        // validate arguments
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(location);
        Preconditions.checkNotNull(natureIds);
        Preconditions.checkArgument(!name.isEmpty(), "Project name must not be empty.");
        Preconditions.checkArgument(location.exists(), String.format("Project location %s must exist.", location));
        Preconditions.checkArgument(location.isDirectory(), String.format("Project location %s must be a directory.", location));

        monitor = MoreObjects.firstNonNull(monitor, new NullProgressMonitor());
        monitor.beginTask(String.format("Create Eclipse project %s", name), 3 + natureIds.size());
        try {
            // check project name is unique in workspace
            Preconditions.checkState(!findProjectByName(name).isPresent(), String.format("Workspace already contains a project with name %s.", name));

            // calculate the name and the project location
            String projectName = resolveProjectName(name, location);
            IPath projectLocation = resolveProjectLocation(location);

            // get an IProject instance and create the project
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProjectDescription projectDescription = workspace.newProjectDescription(projectName);
            projectDescription.setLocation(projectLocation);
            projectDescription.setComment(String.format("Project %s created by Buildship.", projectName));
            IProject project = workspace.getRoot().getProject(projectName);
            project.create(projectDescription, new SubProgressMonitor(monitor, 1));

            // open the project
            project.open(new SubProgressMonitor(monitor, 1));

            // add project natures separately to trigger IProjectNature#configure
            // the project needs to be open while the natures are added
            for (String natureId : natureIds) {
                addNature(project, natureId, new SubProgressMonitor(monitor, 1));
            }

            // return the created, open project
            return project;
        } catch (Exception e) {
            String message = String.format("Cannot create Eclipse project %s.", name);
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

    @Override
    public IProject includeProject(IProjectDescription projectDescription, List<String> extraNatureIds, IProgressMonitor monitor) {
        // validate arguments
        Preconditions.checkNotNull(projectDescription);
        Preconditions.checkNotNull(extraNatureIds);
        String projectName = projectDescription.getName();

        monitor = MoreObjects.firstNonNull(monitor, new NullProgressMonitor());
        monitor.beginTask(String.format("Include existing non-workspace Eclipse project %s", projectName), 3 + extraNatureIds.size());
        try {
            // check project name is unique in workspace
            Preconditions.checkState(!findProjectByName(projectName).isPresent(), String.format("Workspace already contains a project with name %s.", projectName));

            // include the project in the workspace
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProject project = workspace.getRoot().getProject(projectName);
            project.create(projectDescription, new SubProgressMonitor(monitor, 1));

            // open the project
            project.open(new SubProgressMonitor(monitor, 1));

            // add project natures separately to trigger IProjectNature#configure
            // the project needs to be open while the natures are added
            for (String natureId : extraNatureIds) {
                addNature(project, natureId, new SubProgressMonitor(monitor, 1));
            }

            // return the included, open project
            return project;
        } catch (Exception e) {
            String message = String.format("Cannot include existing Eclipse project %s.", projectName);
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

    @Override
    public IJavaProject createJavaProject(IProject project, IPath jrePath, IClasspathEntry classpathContainer, IProgressMonitor monitor) {
        // validate arguments
        Preconditions.checkNotNull(project);
        Preconditions.checkNotNull(jrePath);
        Preconditions.checkArgument(project.isAccessible(), "Project must be open.");

        monitor = MoreObjects.firstNonNull(monitor, new NullProgressMonitor());
        monitor.beginTask(String.format("Create Eclipse Java project %s", project.getName()), 17);
        try {
            // add Java nature
            addNature(project, JavaCore.NATURE_ID, new SubProgressMonitor(monitor, 2));

            // create the Eclipse Java project from the plain Eclipse project
            IJavaProject javaProject = JavaCore.create(project);
            monitor.worked(5);

            // set up initial classpath container on project
            setClasspathOnProject(javaProject, jrePath, classpathContainer, new SubProgressMonitor(monitor, 5));

            // set up output location
            IFolder outputFolder = createOutputFolder(project, new SubProgressMonitor(monitor, 1));
            javaProject.setOutputLocation(outputFolder.getFullPath(), new SubProgressMonitor(monitor, 1));

            // save the project configuration
            javaProject.save(new SubProgressMonitor(monitor, 2), true);

            // return the created Java project
            return javaProject;
        } catch (Exception e) {
            String message = String.format("Cannot create Eclipse Java project %s.", project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

    private IFolder createOutputFolder(IProject project, IProgressMonitor monitor) {
        monitor.beginTask(String.format("Create output folder for Eclipse project %s", project.getName()), 1);
        try {
            IFolder outputFolder = project.getFolder("bin");
            if (!outputFolder.exists()) {
                outputFolder.create(true, true, new SubProgressMonitor(monitor, 1));
            }
            return outputFolder;
        } catch (Exception e) {
            String message = String.format("Cannot create output folder for Eclipse project %s.", project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

    private void setClasspathOnProject(IJavaProject javaProject, IPath jrePath, IClasspathEntry classpathContainerEntry, IProgressMonitor monitor) {
        monitor.beginTask(String.format("Configure sources and classpath for Eclipse project %s", javaProject.getProject().getName()), 10);
        try {
            // create a new holder for all classpath entries
            Builder<IClasspathEntry> entries = ImmutableList.builder();

            // add the library with the JRE dependencies
            entries.add(JavaCore.newContainerEntry(jrePath));
            monitor.worked(1);

            // add classpath definition of where to store the source/project/external dependencies, the classpath
            // will be populated lazily by the org.eclipse.jdt.core.classpathContainerInitializer
            // extension point (see GradleClasspathContainerInitializer)
            entries.add(classpathContainerEntry);
            monitor.worked(1);

            // assign the whole classpath at once to the project
            List<IClasspathEntry> entriesArray = entries.build();
            javaProject.setRawClasspath(entriesArray.toArray(new IClasspathEntry[entriesArray.size()]), new SubProgressMonitor(monitor, 6));
        } catch (Exception e) {
            String message = String.format("Cannot configure sources and classpath for Eclipse project %s.", javaProject.getProject().getName());
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

    @Override
    public void refreshProject(IProject project, IProgressMonitor monitor) {
        // validate arguments
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(project.isAccessible(), "Project must be open.");

        monitor = MoreObjects.firstNonNull(monitor, new NullProgressMonitor());
        monitor.beginTask(String.format("Refresh Eclipse project %s", project.getName()), 1);
        try {
            project.refreshLocal(IProject.DEPTH_INFINITE, new SubProgressMonitor(monitor, 1));
        } catch (Exception e) {
            String message = String.format("Cannot refresh Eclipse project %s.", project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

    private String resolveProjectName(String nameInGradleModel, File location) {
        // if an Eclipse project is imported from the workspace folder it has to have the same name
        // as the folder name (even when the project is renamed the underlying folder is also renamed)
        // consequently, for this use case, we have to ignore the name provided by the Gradle model
        // and instead use the folder name
        return isDirectChildOfWorkspaceRootFolder(location) ? location.getName() : nameInGradleModel;
    }

    private IPath resolveProjectLocation(File location) {
        // in Eclipse <4.4, the LocationValidator throws an exception in some scenarios
        // see also an in-depth explanation in https://github.com/eclipse/buildship/pull/130
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
        monitor.beginTask(String.format("Add nature %s to Eclipse project %s", natureId, project.getName()), 1);
        try {
            // get the description
            IProjectDescription description = project.getDescription();

            // abort if the project already has the nature applied or the nature is not defined
            List<String> currentNatureIds = ImmutableList.copyOf(description.getNatureIds());
            if (currentNatureIds.contains(natureId) || !natureRecognizedByEclipse(natureId)) {
                return;
            }

            // add the nature to the project
            ImmutableList<String> newIds = ImmutableList.<String>builder().addAll(currentNatureIds).add(natureId).build();
            description.setNatureIds(newIds.toArray(new String[newIds.size()]));

            // save the updated description
            project.setDescription(description, new SubProgressMonitor(monitor, 1));
        } catch (CoreException e) {
            String message = String.format("Cannot add nature %s to Eclipse project %s.", natureId, project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

    private boolean natureRecognizedByEclipse(String natureId) {
        // if a description contains a nature id not defined by any of the Eclipse plugins then setting
        // it on a project throws an exception
        return ResourcesPlugin.getWorkspace().getNatureDescriptor(natureId) != null;
    }

    @Override
    public void removeNature(IProject project, String natureId, IProgressMonitor monitor) {
        monitor.beginTask(String.format("Remove nature %s from Eclipse project %s", natureId, project.getName()), 1);
        try {
            // get the description
            IProjectDescription description = project.getDescription();

            // abort if the project currently does not have the nature applied
            List<String> currentNatureIds = ImmutableList.copyOf(description.getNatureIds());
            if (!currentNatureIds.contains(natureId)) {
                return;
            }

            // remove the nature from the project
            List<String> newIds = new ArrayList<String>(currentNatureIds);
            newIds.remove(natureId);
            description.setNatureIds(newIds.toArray(new String[newIds.size()]));

            // save the updated description
            project.setDescription(description, new SubProgressMonitor(monitor, 1));
        } catch (CoreException e) {
            String message = String.format("Cannot remove nature %s from Eclipse project %s.", natureId, project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

    @Override
    public void addBuildCommand(IProject project, String name, Map<String, String> arguments, IProgressMonitor monitor) {
        monitor.beginTask(String.format("Add build command %s to Eclipse project %s", name, project.getName()), 1);
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
                        setNewBuildCommands(project, description, buildCommands, new SubProgressMonitor(monitor, 1));
                        return;
                    }
                }
            }

            // if the build command didn't exist before then create a new command instance and assign it to the project
            buildCommands.add(createCommand(description, name, arguments));
            setNewBuildCommands(project, description, buildCommands, new SubProgressMonitor(monitor, 1));
        } catch (CoreException e) {
            String message = String.format("Cannot add build command %s with arguments %s to Eclipse project %s.", name, arguments, project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

    private ICommand createCommand(IProjectDescription description, String name, Map<String, String> arguments) {
        ICommand command = description.newCommand();
        command.setBuilderName(name);
        command.setArguments(ImmutableMap.copyOf(arguments));
        return command;
    }

    private void setNewBuildCommands(IProject project, IProjectDescription description, List<ICommand> buildCommands, SubProgressMonitor monitor) throws CoreException {
        description.setBuildSpec(buildCommands.toArray(new ICommand[buildCommands.size()]));
        project.setDescription(description, monitor);
    }

    @Override
    public void removeBuildCommand(IProject project, final String name, IProgressMonitor monitor) {
        monitor.beginTask(String.format("Remove build command %s from Eclipse project %s", name, project.getName()), 1);
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
            if (existingCommands.size() != updatedCommands.size()) {
                description.setBuildSpec(updatedCommands.toArray(new ICommand[updatedCommands.size()]));
                project.setDescription(description, new SubProgressMonitor(monitor, 1));
            } else {
                monitor.worked(1);
            }
        } catch (CoreException e) {
            String message = String.format("Cannot remove build command %s from Eclipse project %s.", name, project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

}
