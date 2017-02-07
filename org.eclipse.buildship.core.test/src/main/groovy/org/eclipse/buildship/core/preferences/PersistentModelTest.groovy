package org.eclipse.buildship.core.preferences

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.preferences.ModelPersistence
import org.eclipse.buildship.core.preferences.PersistentModel
import org.eclipse.buildship.core.preferences.internal.DefaultPersistentModel
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class PersistentModelTest extends WorkspaceSpecification {

    IProject project

    void setup() {
        project = newProject('sample-project')
    }

    def "Empty model has sensible defaults"() {
        setup:
        def model = PersistentModel.empty(project)

        expect:
        model.project.is project
        model.buildDir == new Path('build')
        model.emptyModel
        model.subprojectPaths.isEmpty()
        model.classpath.isEmpty()
        model.derivedResources.isEmpty()
        model.linkedResources.isEmpty()
    }

    def "Nonempty model has sensible defaults"() {
        setup:
        def model = PersistentModel.builder(project).build()

        expect:
        model.project.is project
        model.buildDir == new Path('build')
        !model.emptyModel
        model.subprojectPaths.isEmpty()
        model.classpath.isEmpty()
        model.derivedResources.isEmpty()
        model.linkedResources.isEmpty()
    }

    def "Builder can set attributes"() {
        setup:
        def buildDir = new Path('buildDir')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]

        def model = PersistentModel.builder(project)
            .buildDir(buildDir)
            .subprojectPaths(subProjectPaths)
            .classpath(classpath)
            .derivedResources(derivedResources)
            .linkedResources(linkedResources)
            .build()

        expect:
        model.project.is project
        !model.emptyModel
        model.buildDir == buildDir
        model.subprojectPaths == subProjectPaths
        model.classpath == classpath
        model.derivedResources == derivedResources
        model.linkedResources == linkedResources
    }

    def "Builder can be initialized from another model"() {
        setup:
        def buildDir = new Path('buildDir')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]

        def model = PersistentModel.builder(project)
            .buildDir(buildDir)
            .subprojectPaths(subProjectPaths)
            .classpath(classpath)
            .derivedResources(derivedResources)
            .linkedResources(linkedResources)
            .build()
        model = PersistentModel.builder(model).build()

        expect:
        model.project.is project
        !model.emptyModel
        model.buildDir == buildDir
        model.subprojectPaths == subProjectPaths
        model.classpath == classpath
        model.derivedResources == derivedResources
        model.linkedResources == linkedResources
    }


    def "Setting null values revert to default"() {
        setup:
        def buildDir = new Path('buildDir')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]

        def model = PersistentModel.builder(project)
            .buildDir(buildDir)
            .subprojectPaths(subProjectPaths)
            .classpath(classpath)
            .derivedResources(derivedResources)
            .linkedResources(linkedResources)
            .build()

        model = PersistentModel.builder(model)
            .buildDir(null)
            .subprojectPaths(null)
            .classpath(null)
            .derivedResources(null)
            .linkedResources(null)
            .build()

        expect:
        model.buildDir == new Path('build')
        !model.emptyModel
        model.subprojectPaths.isEmpty()
        model.classpath.isEmpty()
        model.derivedResources.isEmpty()
        model.linkedResources.isEmpty()
    }
}
