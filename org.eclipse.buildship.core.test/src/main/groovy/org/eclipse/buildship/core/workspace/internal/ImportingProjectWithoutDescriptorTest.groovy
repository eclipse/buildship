package org.eclipse.buildship.core.workspace.internal

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.test.fixtures.GradleModel

class ImportingProjectWithoutDescriptorTest extends CoupledProjectSynchronizationSpecification {

    def "The project is created and added to the workspace"() {
        setup:
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        expect:
        CorePlugin.workspaceOperations().getAllProjects().isEmpty()

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

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
