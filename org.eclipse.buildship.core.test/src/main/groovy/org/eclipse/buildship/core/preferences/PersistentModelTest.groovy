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

    def "Can't create partial persistent model"() {
        when:
        PersistentModel.builder(project).build()

        then:
        thrown NullPointerException
    }

    def "Can crate complete persistent model"() {
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
        model.buildDir == buildDir
        model.subprojectPaths == subProjectPaths
        model.classpath == classpath
        model.derivedResources == derivedResources
        model.linkedResources == linkedResources
    }

    def "Can create new model from existing one"() {
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
        model.buildDir == buildDir
        model.subprojectPaths == subProjectPaths
        model.classpath == classpath
        model.derivedResources == derivedResources
        model.linkedResources == linkedResources
    }


    def "Null values cause NPE when the build method is called"() {
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

        PersistentModelBuilder builder = PersistentModel.builder(model)
        builder."${method}"(null)

        when:
        builder.build()

        then:
        thrown NullPointerException

        where:
        method << [ 'buildDir', 'subprojectPaths', 'classpath', 'derivedResources', 'linkedResources' ]
    }
}
