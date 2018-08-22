package org.eclipse.buildship.core.internal.workspace.impl

import org.gradle.tooling.model.eclipse.EclipseProject

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.internal.util.gradle.CompatEclipseProject
import org.eclipse.buildship.core.internal.util.gradle.ModelUtils

class LibraryFilterTest extends WorkspaceSpecification {

    def "Deletes custom lib entries"() {
        setup:
        IJavaProject project = projectWithCustomLib()

        expect:
        hasLibsInClasspath(project)

        when:
        EclipseProject model = Mock(EclipseProject)
        model.classpathContainers >> ModelUtils.asDomainObjectSet([])
        LibraryFilter.update(project, model, new NullProgressMonitor())

        then:
        !hasLibsInClasspath(project)
    }

    def "Leaves custom lib entries untouched for older Gradle versions"() {
        setup:
        IJavaProject project = projectWithCustomLib()

        expect:
        hasLibsInClasspath(project)

        when:
        EclipseProject model = Mock(EclipseProject)
        model.classpathContainers >> CompatEclipseProject.UNSUPPORTED_CONTAINERS
        LibraryFilter.update(project, model, new NullProgressMonitor())

        then:
        hasLibsInClasspath(project)
    }

    private IJavaProject projectWithCustomLib() {
        IJavaProject project = newJavaProject('project')
        IClasspathEntry[] classpath = project.rawClasspath + JavaCore.newLibraryEntry(new Path('/path/to/lib.jar'), null, null)
        project.setRawClasspath(classpath, new NullProgressMonitor())
        project
    }

    private Boolean hasLibsInClasspath(IJavaProject project) {
        project.rawClasspath.find { it.entryKind == IClasspathEntry.CPE_LIBRARY } != null
    }
}
