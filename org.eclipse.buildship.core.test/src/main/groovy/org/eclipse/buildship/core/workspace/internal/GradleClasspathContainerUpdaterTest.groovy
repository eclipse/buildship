package org.eclipse.buildship.core.workspace.internal

import com.google.common.base.Optional

import com.gradleware.tooling.toolingmodel.OmniEclipseProject
import com.gradleware.tooling.toolingmodel.OmniEclipseProjectDependency
import com.gradleware.tooling.toolingmodel.OmniExternalDependency

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.preferences.PersistentModel
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.workspace.GradleClasspathContainer

class GradleClasspathContainerUpdaterTest extends WorkspaceSpecification {

    IJavaProject project

    void setup() {
        project = newJavaProject("sample")
        project.setRawClasspath([JavaCore.newContainerEntry(GradleClasspathContainer.CONTAINER_PATH)] as IClasspathEntry[], null)
    }

    def "Folders are valid external dependencies"() {
        given:
        def gradleProject = gradleProjectWithClasspath(
            externalDependency(dir("foo"))
        )
        PersistentModelBuilder persistentModel = builder(project.project)


        when:
        GradleClasspathContainerUpdater.updateFromModel(project, gradleProject, gradleProject.all.toSet(), persistentModel, null)

        then:
        resolvedClasspath[0].entryKind == IClasspathEntry.CPE_LIBRARY
        resolvedClasspath[0].path.toFile() == dir("foo")
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

    private PersistentModel emptyPersistentModel(IProject project) {
        PersistentModel.builder(project)
            .buildDir(new Path("build"))
            .subprojectPaths([])
            .classpath([])
            .derivedResources([])
            .linkedResources([])
            .build()
    }

    private PersistentModelBuilder builder(IProject project) {
        new PersistentModelBuilder(emptyModel(project))
    }

    private PersistentModel emptyModel(IProject project) {
        new PersistentModel(project, new Path("build"), [], [], [], [])
    }
}
