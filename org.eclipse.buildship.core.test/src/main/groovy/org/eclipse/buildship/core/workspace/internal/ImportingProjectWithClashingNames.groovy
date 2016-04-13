package org.eclipse.buildship.core.workspace.internal

import spock.lang.Ignore

import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.core.notification.UserNotification
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.test.fixtures.TestEnvironment

@Ignore('Enable when composite build feature is added')
class ImportingProjectWithClashingNames extends ProjectSynchronizationSpecification {

    def "Same root name is not accepted"() {
        setup:
        UserNotification userNotification = Mock(UserNotification)
        environment.registerService(UserNotification, userNotification)
        environment.registerService(Logger, Mock(Logger))

        def firstProject = dir('first') { file 'settings.gradle', "rootProject.name = 'root'" }
        def secondProject = dir('second') { file 'settings.gradle', "rootProject.name = 'root'" }

        when:
        importAndWait(firstProject)
        importAndWait(secondProject)

        then:
        1 * userNotification.errorOccurred(*_)
    }

    def "Same subproject names are deduped"() {
        setup:
        def firstProject = dir('first') {
            dir 'sub'
            file 'settings.gradle', "include 'sub'"
        }
        def secondProject = dir('second') {
            dir 'sub'
            file 'settings.gradle', "include 'sub'"
        }

        when:
        importAndWait(firstProject)
        importAndWait(secondProject)

        then:
        allProjects().size() == 4
        findProject('first')
        findProject('first-sub')
        findProject('second')
        findProject('second-sub')
    }

    def "Clashing deduped names are further deduped"() {
        setup:
        def firstProject = dir('first') {
            dir 'sub-subsub'
            file 'settings.gradle', "include 'sub-subsub'"
        }
        def secondProject = dir('second') {
            dir 'sub/subsub'
            file 'settings.gradle', "include 'sub'; include 'sub:subsub' "
        }

        when:
        importAndWait(firstProject)

        then:
        allProjects().size() == 2
        findProject('first')
        findProject('sub-subsub')

        when:
        importAndWait(secondProject)

        then:
        allProjects().size() == 5
        findProject('first')
        findProject('first-sub-subsub')
        findProject('second')
        findProject('second-sub')
        findProject('second-sub-subsub')
    }
}
