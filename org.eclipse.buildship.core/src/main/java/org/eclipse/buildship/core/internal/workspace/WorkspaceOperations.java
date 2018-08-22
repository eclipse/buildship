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

package org.eclipse.buildship.core.internal.workspace;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Provides operations related to querying and modifying the Eclipse elements that exist in a
 * workspace.
 */
public interface WorkspaceOperations {

    /**
     * Returns all of the workspace's projects. Open and closed projects are included.
     *
     * @return all projects of the workspace
     */
    ImmutableList<IProject> getAllProjects();

    /**
     * Returns the workspace's project with the given name, if it exists. Open and closed projects
     * are included.
     *
     * @param name the name of the project to find
     * @return the matching project, otherwise {@link Optional#absent()}
     */
    Optional<IProject> findProjectByName(String name);

    /**
     * Returns the workspace's project with the given location, if it exists. Open and closed projects
     * are included.
     *
     * @param location the location of the project to find
     * @return the matching project, otherwise {@link Optional#absent()}
     */
    Optional<IProject> findProjectByLocation(File location);

    /**
     * Returns the Eclipse project descriptor at the given physical location, if it exists.
     *
     * @param location the physical location where to look for an existing Eclipse project
     * @param monitor  the monitor to report progress on
     * @return the found Eclipse project, otherwise {@link Optional#absent()}
     */
    Optional<IProjectDescription> findProjectDescriptor(File location, IProgressMonitor monitor);

   /**
     * Creates a new {@link IProject} in the workspace using the specified name and location. The
     * location must exist and no project with the specified name must currently exist in the
    * workspace. The new project gets the specified natures applied.
     *
     * @param name the unique name of the project to create
     * @param location the location of the project to import
     * @param natureIds the nature ids to associate with the project
     * @param monitor the monitor to report progress on
     * @return the created project
     * @throws org.eclipse.buildship.core.internal.GradlePluginsRuntimeException thrown if the project creation fails
     */
   IProject createProject(String name, File location, List<String> natureIds, IProgressMonitor monitor);

    /**
     * Includes an existing {@link IProject} in the workspace. The project must not yet exist in the workspace.
     * The project is also opened and the specified natures are added.
     *
     * @param projectDescription the project to include
     * @param extraNatureIds the nature ids to add to the project
     * @param monitor the monitor to report the progress on
     * @return the included project
     * @throws org.eclipse.buildship.core.internal.GradlePluginsRuntimeException thrown if the project inclusion fails
     */
    IProject includeProject(IProjectDescription projectDescription, List<String> extraNatureIds, IProgressMonitor monitor);

    /**
     * Refreshes the content of an existing {@link IProject} to get it in sync with the file system.
     *
     * Useful to avoid having out-of-sync warnings showing up in the IDE.
     *
     * @param project the project to be refreshed
     * @param monitor the monitor to report progress on
     */
    void refreshProject(IProject project, IProgressMonitor monitor);

    /**
     * Adds the given nature to an existing {@link IProject}.
     * <p/>
     * If the target project already has the nature, then it will remain unchanged.
     *
     * @param project the project to which to add the nature
     * @param natureId the nature to add
     * @param monitor the monitor to report progress on
     */
    void addNature(IProject project, String natureId, IProgressMonitor monitor);

    /**
     * Remove the given nature from an existing {@link IProject}.
     *
     * @param project  the project from which to remove the nature
     * @param natureId the nature to remove
     * @param monitor  the monitor to report progress on
     */
    void removeNature(IProject project, String natureId, IProgressMonitor monitor);

    /**
     * Adds a new build command to the target project.
     * <p/>
     * If the target project already has the same build command defined, then the the project will
     * remain unchanged. If the build command is defined with a different arguments map, then the
     * arguments will be updated.
     *
     * @param project the target project
     * @param name the name of the new build command
     * @param arguments the arguments of the new build command
     * @param monitor the monitor to report the progress on
     */
    void addBuildCommand(IProject project, String name, Map<String, String> arguments, IProgressMonitor monitor);

    /**
     * Removes a build command from the target project.
     * <p/>
     * If there is no build command with the given name, then the project will remain unchanged.
     *
     * @param project the target project
     * @param name the name of the build command to remove
     * @param monitor the monitor to report the progress on
     */
    void removeBuildCommand(IProject project, String name, IProgressMonitor monitor);

    /**
     * Ensures that the target project name and location is valid.
     *
     * @param name the project name
     * @param location the project location
     * @throws NullPointerException if any of the arguments are {@code null}
     * @throws org.eclipse.buildship.core.internal.UnsupportedConfigurationException if the target
     *             directory is located under the workspace root and the directory's name
     *             is not equal to the project's name
     */
    void validateProjectName(String name, File location);

    /**
     * Renames the project. Has no effect if the project already has the given name. Projects
     * in the default location (directly contained in the workspace root) cannot be renamed.
     * Renaming will also fail if there already is a project with the given name.
     *
     * Important note: The passed in project instance is no longer valid after this operation.
     * Use the returned project instance to work with the renamed project.
     *
     * @param project the target project
     * @param newName the name to rename the project to
     * @param monitor the monitor to report progress on
     * @return the renamed project
     * @throws org.eclipse.buildship.core.internal.GradlePluginsRuntimeException if the project cannot be renamed
     */
    IProject renameProject(IProject project, String newName, IProgressMonitor monitor);

    /**
     * Checks whether Eclipse supports the given project nature.
     *
     * @param natureId the nature to check for, must not be null
     * @return true if Eclipse supports this nature, false otherwise
     */
    boolean isNatureRecognizedByEclipse(String natureId);

    /**
     * Checks whether WTP is installed in the current instance.
     *
     * @return true if WTP is installed, false otherwise
     */
    boolean isWtpInstalled ();
}
