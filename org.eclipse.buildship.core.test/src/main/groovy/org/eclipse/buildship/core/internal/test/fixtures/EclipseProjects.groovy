package org.eclipse.buildship.core.internal.test.fixtures

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.JavaRuntime


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
        IProjectDescription projectDescription = workspace.newProjectDescription(name)
        projectDescription.setLocation(location == null ? null : new Path(location.absolutePath))
        IProject project = workspace.getRoot().getProject(name)
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

    /**
     * Creates a new Java project at a custom location.
     * <p/>
     * The result Java project has
     * <ul>
     * <li>A source folder at project_loc/src</li>
     * <li>An output folder at project_loc/bin</li>
     * <li>The default JRE location added to the classpath</li>
     * </ul>
     *
     * @param name the name of the project
     * @param location the location where to store the project
     * @return the reference of the created Java project
     */
    static IJavaProject newJavaProject(String name, File location) {
        IProject project = newProject(name, location)
        IProjectDescription description = project.description
        description.natureIds = description.natureIds + JavaCore.NATURE_ID
        project.setDescription(description, null)
        IJavaProject javaProject = JavaCore.create(project)
        project.getFolder('src').create(true, false, null)
        javaProject.setRawClasspath([JavaCore.newSourceEntry(project.getFolder('src').location),
            JavaCore.newContainerEntry(JavaRuntime.getDefaultJREContainerEntry().getPath())] as IClasspathEntry[], null)
        javaProject.setOutputLocation(project.getFolder('bin').fullPath, null)
        javaProject
    }
}
