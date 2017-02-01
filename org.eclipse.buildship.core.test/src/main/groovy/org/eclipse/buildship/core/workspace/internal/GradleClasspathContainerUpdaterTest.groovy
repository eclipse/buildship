package org.eclipse.buildship.core.workspace.internal

import com.google.common.base.Optional

import com.gradleware.tooling.toolingmodel.OmniEclipseProject
import com.gradleware.tooling.toolingmodel.OmniEclipseProjectDependency
import com.gradleware.tooling.toolingmodel.OmniExternalDependency

import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.workspace.GradleClasspathContainer

class GradleClasspathContainerUpdaterTest extends WorkspaceSpecification {

    IJavaProject project

    void setup() {
        File folder = dir('another')
        File file = file('another/file')
        project = newJavaProject("sample")
        project.project.getFolder('linked_folder').createLink(new Path(folder.absolutePath), IResource.BACKGROUND_REFRESH | IResource.ALLOW_MISSING_LOCAL | IResource.REPLACE, new NullProgressMonitor())
        project.project.getFile('linked_file').createLink(new Path(file.absolutePath), IResource.BACKGROUND_REFRESH | IResource.ALLOW_MISSING_LOCAL | IResource.REPLACE, new NullProgressMonitor())
        project.setRawClasspath([JavaCore.newContainerEntry(GradleClasspathContainer.CONTAINER_PATH)] as IClasspathEntry[], null)
    }

    def "Nonexisting resources are also added to the classpath"() {
        given:
        def file = new File("nonexisting.jar")

        def gradleProject = gradleProjectWithClasspath(
            externalDependency(file)
        )

        when:
        GradleClasspathContainerUpdater.updateFromModel(project, gradleProject, gradleProject.all.toSet(), null)

        then:
        resolvedClasspath[0].entryKind == IClasspathEntry.CPE_LIBRARY
        resolvedClasspath[0].path.toFile() == file.absoluteFile
    }

    def "Folders are valid external dependencies"() {
        given:
        def gradleProject = gradleProjectWithClasspath(
            externalDependency(dir("foo"))
        )

        when:
        GradleClasspathContainerUpdater.updateFromModel(project, gradleProject, gradleProject.all.toSet(), null)

        then:
        resolvedClasspath[0].entryKind == IClasspathEntry.CPE_LIBRARY
        resolvedClasspath[0].path.toFile() == dir("foo")
    }

    def "Linked files can be added to the classpath"(String path) {
        given:
        def gradleProject = gradleProjectWithClasspath(
            externalDependency(new File(path))
        )

        when:
        GradleClasspathContainerUpdater.updateFromModel(project, gradleProject, gradleProject.all.toSet(), null)

        then:
        resolvedClasspath[0].entryKind == IClasspathEntry.CPE_LIBRARY
        resolvedClasspath[0].path.toPortableString() == '/sample/linked_file'

        where:
        path << ['linked_file', '/linked_file']
    }

    def "Linked folders can be added to the classpath"(String path) {
        given:
        def gradleProject = gradleProjectWithClasspath(
            externalDependency(new File(path))
        )

        when:
        GradleClasspathContainerUpdater.updateFromModel(project, gradleProject, gradleProject.all.toSet(), null)

        then:
        resolvedClasspath[0].entryKind == IClasspathEntry.CPE_LIBRARY
        resolvedClasspath[0].path.toPortableString() == '/sample/linked_folder'

        where:
        path << ['linked_folder', '/linked_folder']
    }

    OmniEclipseProject gradleProjectWithClasspath(Object... dependencies) {
        Stub(OmniEclipseProject) {
            getExternalDependencies() >> dependencies.findAll { it instanceof OmniExternalDependency }
            getProjectDependencies() >> dependencies.findAll { it instanceof OmniEclipseProjectDependency }
        }
    }

    OmniExternalDependency externalDependency(File location, File sources = null,
                                              File javaDoc = null, boolean exported = false,
                                              Optional attributes = Optional.of([]), Optional rules = Optional.of([])) {
        Stub(OmniExternalDependency) {
            getFile() >> location
            getSource() >> sources
            getJavadoc() >> javaDoc
            isExported() >> exported
            getClasspathAttributes() >> attributes
            getAccessRules() >> rules
        }
    }

    IClasspathEntry[] getResolvedClasspath() {
        project.getResolvedClasspath(false)
    }
}
