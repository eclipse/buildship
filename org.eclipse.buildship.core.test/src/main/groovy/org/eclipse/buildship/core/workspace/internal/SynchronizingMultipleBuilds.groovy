package org.eclipse.buildship.core.workspace.internal

import spock.lang.Ignore

import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.core.notification.UserNotification
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.test.fixtures.TestEnvironment

class SynchronizingMultipleBuilds extends ProjectSynchronizationSpecification {

    def "If there are no name clashes, no de-duplication is done"() {
        setup:
        def firstProject = dir('first') { file 'settings.gradle' }
        def secondProject = dir('second') { file 'settings.gradle' }

        when:
        importAndWait(firstProject)
        importAndWait(secondProject)

        then:
        allProjects().size() == 2
        findProject('first')
        findProject('second')
    }

    def "If one project has a problem, no project is synchronized"() {
        setup:
        def firstProject = dir('first') { file 'settings.gradle' }
        def secondProject = dir('second') { file 'settings.gradle' }
        importAndWait(firstProject)
        importAndWait(secondProject)

        fileTree(firstProject) {
            file 'settings.gradle', "rootProject.name = 'foo'"
        }

        fileTree(secondProject) {
            file 'settings.gradle', "error"
        }

        when:
        synchronizeAndWait()

        then:
        findProject("first")
        !findProject("foo")
    }
}
