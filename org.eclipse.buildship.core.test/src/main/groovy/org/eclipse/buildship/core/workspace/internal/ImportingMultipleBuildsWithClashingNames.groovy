package org.eclipse.buildship.core.workspace.internal

import spock.lang.Ignore

import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.core.notification.UserNotification
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.test.fixtures.TestEnvironment

class ImportingMultipleBuildsWithClashingNames extends ProjectSynchronizationSpecification {

    def "Duplicate root project names are rejected"() {
        setup:
        def firstProject = dir('first') { file 'settings.gradle', "rootProject.name = 'root'" }
        def secondProject = dir('second') { file 'settings.gradle', "rootProject.name = 'root'" }

        when:
        importAndWait(firstProject)
        importAndWait(secondProject)

        then:
        allProjects().size() == 1
        findProject('root')
    }

    def "Same subproject names are deduped"() {
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
        allProjects().size() == 6
        findProject('first')
        findProject('first-sub')
        findProject('first-sub-subsub')
        findProject('second')
        findProject('second-sub')
        findProject('second-sub-subsub')
    }
}
