package org.eclipse.buildship.core.internal.workspace.impl

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.internal.preferences.impl.DefaultPersistentModel
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification

class PersistentModelBuilderTest extends WorkspaceSpecification {

    IProject project

    def setup() {
        project = newProject('sample-project')
    }

    def "Builder from existing model"() {
        setup:
        def buildDir = new Path('buildDir')
        def buildScriptPath = new Path('build.gradle')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]
        def managedNatures = ['org.eclipse.pde.UpdateSiteNature']
        def command = project.description.newCommand()
        command.setBuilderName('custom-command')
        def managedBuilders = [command]

        def previous = new DefaultPersistentModel(project, buildDir, buildScriptPath, subProjectPaths, classpath, derivedResources, linkedResources, managedNatures, managedBuilders)
        def model = new PersistentModelBuilder(previous).build()

        expect:
        model.present
        model.project.is project
        model.buildDir == buildDir
        model.subprojectPaths == subProjectPaths
        model.classpath == classpath
        model.derivedResources == derivedResources
        model.linkedResources == linkedResources
        model.managedNatures == managedNatures
        model.managedBuilders == managedBuilders
    }


    def "Null values cause NPE when the build method is called"() {
        setup:
        def buildDir = new Path('buildDir')
        def buildScriptPath = new Path('build.gradle')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]
        def managedNatures = ['org.eclipse.pde.UpdateSiteNature']
        def command = project.description.newCommand()
        command.setBuilderName('custom-command')
        def managedBuilders = [command]

        def previous = new DefaultPersistentModel(project, buildDir, buildScriptPath, subProjectPaths, classpath, derivedResources, linkedResources, managedNatures, managedBuilders)
        def builder = new PersistentModelBuilder(previous)
        builder."${method}"(null)

        when:
        builder.build()

        then:
        thrown NullPointerException

        where:
        method << [ 'buildDir', 'subprojectPaths', 'classpath', 'derivedResources', 'linkedResources', 'managedNatures', 'managedBuilders' ]
    }
}
