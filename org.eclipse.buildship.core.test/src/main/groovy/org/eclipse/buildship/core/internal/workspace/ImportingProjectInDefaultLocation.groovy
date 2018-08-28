package org.eclipse.buildship.core.internal.workspace

import org.eclipse.buildship.core.internal.ImportRootProjectException
import org.eclipse.buildship.core.internal.UnsupportedConfigurationException
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.internal.test.fixtures.TestEnvironment.*

class ImportingProjectInDefaultLocation extends ProjectSynchronizationSpecification {

    def "Can import projects located in workspace folder with default name"() {
        when:
        importAndWait(workspaceDir('sample'))

        then:
        findProject("sample")
    }

    def "Disallow importing projects located in workspace folder and with custom root name"() {
        setup:
        File rootProject = fileTree(workspaceDir('sample')) {
            file 'settings.gradle', "rootProject.name = 'my-project-name-is-different-than-the-folder'"
        }

        when:
        importAndWait(rootProject)

        then:
        thrown(UnsupportedConfigurationException)
    }

    def "Disallow synchornizing projects located in workspace folder and with custom root name"() {
        setup:
        File rootProject = workspaceDir('sample2')
        importAndWait(rootProject)

        when:
        new File(rootProject, 'settings.gradle') << "rootProject.name = 'my-project-name-is-different-than-the-folder'"
        importAndWait(rootProject)

        then:
        thrown(UnsupportedConfigurationException)
    }

    def "Disallow importing the workspace root"() {
        when:
        importAndWait(workspaceDir)

        then:
        thrown(ImportRootProjectException)
    }

    def "Disallow importing any modules located at the workspace root"() {
        setup:
        File rootProject = workspaceDir('sample2') {
            file 'settings.gradle', "include '..'"
        }

        when:
        importAndWait(workspaceDir)

        then:
        thrown(ImportRootProjectException)
    }

}
