package org.eclipse.buildship.core.internal.workspace.internal

import org.eclipse.core.runtime.CoreException

import org.eclipse.buildship.core.UnsupportedConfigurationException
import org.eclipse.buildship.core.operation.ToolingApiStatus.ToolingApiStatusType
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.test.fixtures.TestEnvironment.*
import org.eclipse.buildship.core.workspace.internal.ImportRootProjectOperation.ImportRootProjectException

class ImportingProjectInDefaultLocation extends ProjectSynchronizationSpecification {

    def "Can import projects located in workspace folder with default name"() {
        when:
        synchronizeAndWait(workspaceDir('sample'))

        then:
        findProject("sample")
    }

    def "Disallow importing projects located in workspace folder and with custom root name"() {
        setup:
        File rootProject = fileTree(workspaceDir('sample')) {
            file 'settings.gradle', "rootProject.name = 'my-project-name-is-different-than-the-folder'"
        }

        when:
        synchronizeAndWait(rootProject)

        then:
        thrown(UnsupportedConfigurationException)
    }

    def "Disallow synchornizing projects located in workspace folder and with custom root name"() {
        setup:
        File rootProject = workspaceDir('sample2')
        synchronizeAndWait(rootProject)

        when:
        new File(rootProject, 'settings.gradle') << "rootProject.name = 'my-project-name-is-different-than-the-folder'"
        synchronizeAndWait(rootProject)

        then:
        thrown(UnsupportedConfigurationException)
    }

    def "Disallow importing the workspace root"() {
        when:
        synchronizeAndWait(workspaceDir)

        then:
        thrown(ImportRootProjectException)
    }

    def "Disallow importing any modules located at the workspace root"() {
        setup:
        File rootProject = workspaceDir('sample2') {
            file 'settings.gradle', "include '..'"
        }

        when:
        synchronizeAndWait(workspaceDir)

        then:
        thrown(ImportRootProjectException)
    }

}
