package org.eclipse.buildship.core.workspace.internal

import org.eclipse.buildship.core.CorePlugin

class ImportingProjectWithoutDescriptor extends SingleProjectSynchronizationSpecification {

    def "The project is created and added to the workspace"() {
        setup:
        def projectDir = dir('sample-project') {
            file 'settings.gradle'
        }

        expect:
        CorePlugin.workspaceOperations().allProjects.empty

        when:
        synchronizeAndWait(projectDir)

        then:
        CorePlugin.workspaceOperations().allProjects.size() == 1
        findProject('sample-project')
    }

    @Override
    protected void prepareProject(String name) {
    }

    @Override
    protected void prepareJavaProject(String name) {
    }
}
