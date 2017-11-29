package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.runtime.CoreException

import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.core.notification.UserNotification
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.util.progress.ToolingApiStatus.ToolingApiStatusType

class ImportingMultipleBuildsWithClashingNames extends ProjectSynchronizationSpecification {

    def "Duplicate root project names are rejected"() {
        setup:
        def firstProject = dir('first') { file 'settings.gradle', "rootProject.name = 'root'" }
        def secondProject = dir('second') { file 'settings.gradle', "rootProject.name = 'root'" }

        when:
        importAndWait(firstProject)
        importAndWait(secondProject)

        then:
        CoreException e = thrown(CoreException)
        e.status.code == ToolingApiStatusType.UNSUPPORTED_CONFIGURATION.code
        allProjects().size() == 1
        findProject('root')
    }

    // TODO (donat) the test randomly imports subprojects from project 'second'
    // ensure that the project synchronization is ordered
    def "Same subproject names in different builds interrupt the project synchronization"() {
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
        importAndWait(secondProject)

        then:
        CoreException e = thrown(CoreException)
        e.status.code == ToolingApiStatusType.UNSUPPORTED_CONFIGURATION.code
    }
}
