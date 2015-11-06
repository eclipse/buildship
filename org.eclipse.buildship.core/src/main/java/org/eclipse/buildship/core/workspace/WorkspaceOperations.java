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

package org.eclipse.buildship.core.workspace;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;

import java.io.File;
import java.util.List;

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
     * Returns the Eclipse project at the given physical location, if it exists. Looks for a <i>.project</i>
     * file in the given folder.
     *
     * @param location the physical location where to look for an existing Eclipse project
     * @param monitor  the monitor to report progress on
     * @return the found Eclipse project, otherwise {@link Optional#absent()}
     */
    Optional<IProjectDescription> findProjectInFolder(File location, IProgressMonitor monitor);

    /**
     * Removes all of the workspace's projects.
     * <p/>
     * Calling this method doesn't delete the files physically, it only removes the {@link IProject}
     * instances from the workspace.
     *
     * @param monitor the monitor to report progress on
     * @throws org.eclipse.buildship.core.GradlePluginsRuntimeException thrown if any of the deletions fails
     */
    void deleteAllProjects(IProgressMonitor monitor);

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
     * @throws org.eclipse.buildship.core.GradlePluginsRuntimeException thrown if the project creation fails
     */
   IProject createProject(String name, File location, List<String> natureIds, IProgressMonitor monitor);

    /**
     * Includes an existing {@link IProject} in the workspace. The project must not yet exist in the workspace.
     * The project is also opened and the specified natures are added.
     *
     * @param description the project to include
     * @param extraNatureIds the nature ids to add to the project
     * @param monitor the monitor to report the progress on
     * @return the included project
     * @throws org.eclipse.buildship.core.GradlePluginsRuntimeException thrown if the project inclusion fails
     */
    IProject includeProject(IProjectDescription description, List<String> extraNatureIds, IProgressMonitor monitor);

    /**
     * Configures an existing {@link IProject} to also be an {@link IJavaProject}.
     *
     * @param project the project to turn into a Java project
     * @param jrePath the path of the Java runtime which will be added to the project
     * @param classpathContainer the classpath container to add to the classpath entries
     * @param monitor the monitor to report progress on
     * @return the created Java project
     */
    IJavaProject createJavaProject(IProject project, IPath jrePath, IClasspathEntry classpathContainer, IProgressMonitor monitor);

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
     * Renames the project.
     * <p/>
     * If the project is located directly under the workspace root, then no change is performed.
     * <p/>
     * Important note: renaming a project changes the path making the target project reference
     * obsolete. The reference should be updated with returned one.
     *
     * @param project the target project
     * @param newName the name to rename the project to
     * @param monitor the monitor to report progress on
     * @return the reference of the new, renamed project
     * @throws org.eclipse.buildship.core.GradlePluginsRuntimeException if the operation fails or a
     *             project already exists in the workspace with the same name
     */
    IProject renameProject(IProject project, String newName, IProgressMonitor monitor);

}
