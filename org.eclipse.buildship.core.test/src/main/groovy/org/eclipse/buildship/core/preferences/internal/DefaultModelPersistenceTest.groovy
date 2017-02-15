package org.eclipse.buildship.core.preferences.internal

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.preferences.PersistentModel
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class DefaultModelPersistenceTest extends WorkspaceSpecification {

    IProject project

    void setup() {
        project = newProject('sample-project')
    }

    def "Absent model is returned if no models were saved for the project"() {
        when:
        PersistentModel model = CorePlugin.modelPersistence().loadModel(project)

        then:
        !model.present
    }

    def "Absent model throws runtime exceptions from all getters"() {
        setup:
        PersistentModel model = CorePlugin.modelPersistence().loadModel(project)

        when:
        model."$method"()

        then:
        thrown IllegalStateException

        where:
        method << [ 'getBuildDir', 'getSubprojectPaths', 'getClasspath', 'getDerivedResources', 'getLinkedResources' ]
    }

    def "Can store and load a model"() {
        setup:
        def buildDir = new Path('buildDir')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]

        PersistentModel model = new DefaultPersistentModel(project, buildDir, subProjectPaths, classpath, derivedResources, linkedResources)

        when:
        CorePlugin.modelPersistence().saveModel(model)
        model = CorePlugin.modelPersistence().loadModel(project)

        then:
        model.present
        model.project == project
        model.buildDir == buildDir
        model.subprojectPaths == subProjectPaths
        model.classpath == classpath
        model.derivedResources == derivedResources
        model.linkedResources == linkedResources
    }

    def "Can delete a model"() {
        setup:
        def buildDir = new Path('buildDir')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]

        PersistentModel model = new DefaultPersistentModel(project, buildDir, subProjectPaths, classpath, derivedResources, linkedResources)
        CorePlugin.modelPersistence().saveModel(model)

        when:
        CorePlugin.modelPersistence().deleteModel(project)
        model = CorePlugin.modelPersistence().loadModel(project)

        then:
        !model.present
    }

    def "Model is still accessible if the referenced project is renamed"() {
        setup:
        def buildDir = new Path('buildDir')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]

        PersistentModel model = new DefaultPersistentModel(project, buildDir, subProjectPaths, classpath, derivedResources, linkedResources)
        CorePlugin.modelPersistence().saveModel(model)

        when:
        project = CorePlugin.workspaceOperations().renameProject(project, 'new-project-name', new NullProgressMonitor())
        model = CorePlugin.modelPersistence().loadModel(project)

        then:
        model.present
        model.buildDir == buildDir
        model.subprojectPaths == subProjectPaths
        model.classpath == classpath
        model.derivedResources == derivedResources
        model.linkedResources == linkedResources
    }
}
