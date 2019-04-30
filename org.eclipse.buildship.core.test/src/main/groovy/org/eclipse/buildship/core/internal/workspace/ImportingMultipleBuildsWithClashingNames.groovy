package org.eclipse.buildship.core.internal.workspace

import org.eclipse.core.runtime.IStatus

import org.eclipse.buildship.core.SynchronizationResult
import org.eclipse.buildship.core.internal.UnsupportedConfigurationException
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class ImportingMultipleBuildsWithClashingNames extends ProjectSynchronizationSpecification {

    def "Duplicate root project names are rejected"() {
        setup:
        def firstProject = dir('first') { file 'settings.gradle', "rootProject.name = 'root'" }
        def secondProject = dir('second') { file 'settings.gradle', "rootProject.name = 'root'" }

        when:
        importAndWait(firstProject)
        tryImportAndWait(secondProject)

        then:
        allProjects().size() == 2
        findProject('root')
        findProject('second')
    }

    // TODO (donat) the test randomly imports subprojects from project 'second'
    // ensure that the project synchronization is ordered
    def "Same subproject names in different builds are deduplicated"() {
        setup:
        def firstProject = dir('first') {
            dir 'sub/subsub'
            file 'settings.gradle', "include 'sub:subsub'"
        }
        def secondProject = dir('second') {
            dir 'sub/subsub'
            file 'settings.gradle', "include 'sub:subsub' "
        }

        when:
        importAndWait(firstProject)

        then:
        allProjects().size() == 3
        findProject('first')
        findProject('sub')
        findProject('subsub')

        when:
        SynchronizationResult result = tryImportAndWait(secondProject)

        then:
        result.status.severity == IStatus.OK
        findProject('first')
        findProject('sub')
        findProject('subsub')

        findProject('second')
        findProject('second-sub')
        findProject('sub-subsub')
    }

}
