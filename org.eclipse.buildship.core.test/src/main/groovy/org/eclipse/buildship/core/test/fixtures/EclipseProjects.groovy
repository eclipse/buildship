package org.eclipse.buildship.core.test.fixtures

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.runtime.Path


/**
 * Helper class to create projects in the Eclipse workspace.
 */
abstract class EclipseProjects {

    /**
     * Creates a project the default {@code ${workspace_loc}/${project-name}} location.
     *
     * @param name the name of the project
     * @return the reference of the created project
     */
    static IProject newProject(String name) {
        newProject(name, null)
    }

    /**
     * Creates a project at a custom location.
     *
     * @param name the name of the project
     * @param location the location where to store the project
     * @return the reference of the created project
     */
    static IProject newProject(String name, File location) {
        def workspace = LegacyEclipseSpockTestHelper.workspace
        IProjectDescription projectDescription = workspace.newProjectDescription("project")
        projectDescription.setLocation(new Path(location.absolutePath))
        IProject project = workspace.getRoot().getProject('another')
        project.create(projectDescription, null)
        project.open(null)
        project
    }

    /**
     * Creates a project the default {@code ${workspace_loc}/${project-name}} location.
     * <p/>
     * The returned project will be closed.
     *
     * @param name the name of the project
     * @return the reference of the created project
     */
    static IProject newClosedProject(String name) {
        newClosedProject(name, null)
    }

    /**
     * Creates a project at a custom location.
     * <p/>
     * The returned project will be closed.
     *
     * @param name the name of the project
     * @param location the location where to store the project
     * @return the reference of the created project
     */
    static IProject newClosedProject(String name, File location) {
        IProject project = newProject(name, location)
        project.close(null)
        project
    }
}
