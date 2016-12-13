package org.eclipse.buildship.core.preferences.internal

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.preferences.PersistentModel
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class DefaultModelPersistenceTest extends WorkspaceSpecification {

    IProject project

    void setup() {
        project = newProject('sample-project')
        PersistentModel model = CorePlugin.modelPersistence().loadModel(project)
        model.setValue('existing-key', 'existing-value')
        CorePlugin.modelPersistence().saveModel(model)
    }

    def "Can store values for a project"() {
        given:
        PersistentModel model = CorePlugin.modelPersistence().loadModel(project)
        model.setValue('key', 'value')
        CorePlugin.modelPersistence().saveModel(model)

        expect:
        CorePlugin.modelPersistence().loadModel(project).getValue('key', 'default-value') == 'value'
    }

    def "Default value is returned for non-existing key"() {
        given:
        PersistentModel model = CorePlugin.modelPersistence().loadModel(project)

        expect:
        model.getValue('nonexisting-key', 'default-value') == 'default-value'
    }

    def "Default value can be null"() {
        given:
        PersistentModel model = CorePlugin.modelPersistence().loadModel(project)

        expect:
        model.getValue('nonexisting-key', null) == null
    }

    def "Setting a null value removes the entry from the model"() {
        when:
        PersistentModel model = CorePlugin.modelPersistence().loadModel(project)
        model.setValue('existing-key', null)
        CorePlugin.modelPersistence().saveModel(model)

        then:
        CorePlugin.modelPersistence().loadModel(project).getValue('existing-key', 'default-value') == 'default-value'
   }

    def "Storage stays intact if a project is renamed"() {
        when:
        project = CorePlugin.workspaceOperations().renameProject(project, 'new-project-name', new NullProgressMonitor())

        then:
        CorePlugin.modelPersistence().loadModel(project).getValue('existing-key', null) == 'existing-value'
    }

    def "Storage is cleaned up if a project is deleted from the workspace"() {
        when:
        deleteAllProjects(true)

        then:
        CorePlugin.modelPersistence().loadModel(project).getValue('existing-key', 'default-value') == 'default-value'
    }

    def "Can delete model from project entirely"() {
        when:
        CorePlugin.modelPersistence().deleteModel(project)

        then:
        CorePlugin.modelPersistence().loadModel(project).getValue('existing-key', null) == null
    }
}
