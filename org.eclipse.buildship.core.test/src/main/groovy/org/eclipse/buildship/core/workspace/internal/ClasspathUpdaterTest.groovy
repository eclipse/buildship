package org.eclipse.buildship.core.workspace.internal

import com.google.common.base.Optional

import com.gradleware.tooling.toolingmodel.OmniEclipseProject

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class ClasspathUpdaterTest extends WorkspaceSpecification {

    def "Deletes custom lib entries"() {
        setup:
        IJavaProject project = projectWithCustomLib()

        expect:
        hasLibsInClasspath(project)

        when:
        OmniEclipseProject model = Mock(OmniEclipseProject)
        model.classpathContainers >> Optional.of(Mock(List))
        ClasspathUpdater.update(project, model, new NullProgressMonitor())

        then:
        !hasLibsInClasspath(project)
    }

    def "Leaves custom lib entries untouched for older Gradle versions"() {
        setup:
        IJavaProject project = projectWithCustomLib()

        expect:
        hasLibsInClasspath(project)

        when:
        OmniEclipseProject model = Mock(OmniEclipseProject)
        model.classpathContainers >> Optional.absent()
        ClasspathUpdater.update(project, model, new NullProgressMonitor())

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
