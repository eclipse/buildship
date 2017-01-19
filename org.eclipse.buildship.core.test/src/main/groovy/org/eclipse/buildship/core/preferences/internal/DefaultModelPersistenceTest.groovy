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
    DefaultPersistentModel model

    void setup() {
        project = newProject('sample-project')
        model = CorePlugin.modelPersistence().loadModel(project)
    }

    def "Null value is returned for empty model elements"() {
        expect:
        model.buildDir == null
        model.subprojectPaths == null
        model.classpath == null
        model.derivedResources == null
        model.linkedResources == null
    }

    def "Can store values for a project"() {
        setup:
        def buildDir = new Path('buildDir')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]

        when:
        PersistentModel model = CorePlugin.modelPersistence().loadModel(project)
        model.buildDir = buildDir
        model.subprojectPaths = subProjectPaths
        model.classpath = classpath
        model.derivedResources = derivedResources
        model.linkedResources = linkedResources
        CorePlugin.modelPersistence().saveModel(model)
        model = CorePlugin.modelPersistence().loadModel(project)

        then:
        model.buildDir == buildDir
        model.subprojectPaths == subProjectPaths
        model.classpath == classpath
        model.derivedResources == derivedResources
        model.linkedResources == linkedResources
    }

    def "Can set null values"() {
        setup:
        PersistentModel model = CorePlugin.modelPersistence().loadModel(project)
        model.buildDir = new Path('buildDir')
        model.subprojectPaths = [new Path('subproject')]
        model.classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        model.derivedResources = [new Path('derived')]
        model.linkedResources = [project.getFolder('linked')]
        CorePlugin.modelPersistence().saveModel(model)

        when:
        model = CorePlugin.modelPersistence().loadModel(project)
        model.buildDir = null
        model.subprojectPaths = null
        model.classpath = null
        model.derivedResources = null
        model.linkedResources = null
        CorePlugin.modelPersistence().saveModel(model)
        model = CorePlugin.modelPersistence().loadModel(project)

        then:
        model.buildDir == null
        model.subprojectPaths == null
        model.classpath == null
        model.derivedResources == null
        model.linkedResources == null
    }

    def "Storage stays intact if a project is renamed"() {
        setup:
        def buildDir = new Path('buildDir')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]

        PersistentModel model = CorePlugin.modelPersistence().loadModel(project)
        model.buildDir = buildDir
        model.subprojectPaths = subProjectPaths
        model.classpath = classpath
        model.derivedResources = derivedResources
        model.linkedResources = linkedResources
        CorePlugin.modelPersistence().saveModel(model)

        when:
        project = CorePlugin.workspaceOperations().renameProject(project, 'new-project-name', new NullProgressMonitor())
        model = CorePlugin.modelPersistence().loadModel(project)

        then:
        model.buildDir == buildDir
        model.subprojectPaths == subProjectPaths
        model.classpath == classpath
        model.derivedResources == derivedResources
        model.linkedResources == linkedResources
    }

    def "Storage is cleaned up if a project is deleted from the workspace"() {
        setup:
        def buildDir = new Path('buildDir')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]

        PersistentModel model = CorePlugin.modelPersistence().loadModel(project)
        model.buildDir = buildDir
        model.subprojectPaths = subProjectPaths
        model.classpath = classpath
        model.derivedResources = derivedResources
        model.linkedResources = linkedResources
        CorePlugin.modelPersistence().saveModel(model)

        when:
        deleteAllProjects(true)
        model = CorePlugin.modelPersistence().loadModel(project)

        then:
        model.buildDir == null
        model.subprojectPaths == null
        model.classpath == null
        model.derivedResources == null
        model.linkedResources == null
    }

    def "Can delete model from project entirely"() {
        setup:
        def buildDir = new Path('buildDir')
        def subProjectPaths = [new Path('subproject')]
        def classpath = [JavaCore.newProjectEntry(new Path('/project-path'))]
        def derivedResources = [new Path('derived')]
        def linkedResources = [project.getFolder('linked')]

        PersistentModel model = CorePlugin.modelPersistence().loadModel(project)
        model.buildDir = buildDir
        model.subprojectPaths = subProjectPaths
        model.classpath = classpath
        model.derivedResources = derivedResources
        model.linkedResources = linkedResources
        CorePlugin.modelPersistence().saveModel(model)

        when:
        CorePlugin.modelPersistence().deleteModel(project)
        model = CorePlugin.modelPersistence().loadModel(project)

        then:
        model.buildDir == null
        model.subprojectPaths == null
        model.classpath == null
        model.derivedResources == null
        model.linkedResources == null
    }
}
