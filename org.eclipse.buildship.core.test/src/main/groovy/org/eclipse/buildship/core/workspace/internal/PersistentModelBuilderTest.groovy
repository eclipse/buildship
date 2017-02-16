package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.preferences.internal.DefaultPersistentModel
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class PersistentModelBuilderTest extends WorkspaceSpecification {

    IProject project

    void setup() {
        project = newProject('sample-project')
    }

    def "Builder from existing model"() {
        setup:
        def buildDir = new Path('buildDir')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]

        def previous = new DefaultPersistentModel(project, buildDir, subProjectPaths, classpath, derivedResources, linkedResources)
        def model = new PersistentModelBuilder(previous).build()

        expect:
        model.present
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

        def previous = new DefaultPersistentModel(project, buildDir, subProjectPaths, classpath, derivedResources, linkedResources)
        def builder = new PersistentModelBuilder(previous)
        builder."${method}"(null)

        when:
        builder.build()

        then:
        thrown NullPointerException

        where:
        method << [ 'buildDir', 'subprojectPaths', 'classpath', 'derivedResources', 'linkedResources' ]
    }
}
