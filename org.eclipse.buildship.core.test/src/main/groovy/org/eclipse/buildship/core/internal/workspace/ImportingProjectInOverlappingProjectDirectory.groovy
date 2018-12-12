package org.eclipse.buildship.core.internal.workspace

import org.eclipse.core.runtime.IStatus

import org.eclipse.buildship.core.SynchronizationResult
import org.eclipse.buildship.core.internal.UnsupportedConfigurationException
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.internal.test.fixtures.TestEnvironment.*

class ImportingProjectInOverlappingProjectDirectory extends ProjectSynchronizationSpecification {

    def "Disallow importing projects with overlapping project directory"() {
        setup:
        File rootProject = fileTree(dir('overlapping-project-dir')) {
            file 'settings.gradle', """
                include('sub1')
                include('sub2')

                rootProject.children.find { it.name == 'sub1' }.projectDir = file('sub')
                rootProject.children.find { it.name == 'sub2' }.projectDir = file('sub')
            """
            dir "sub"
        }

        when:
        SynchronizationResult result = tryImportAndWait(rootProject)

        then:
        result.status.severity == IStatus.WARNING
        result.status.exception instanceof UnsupportedConfigurationException
    }
}
