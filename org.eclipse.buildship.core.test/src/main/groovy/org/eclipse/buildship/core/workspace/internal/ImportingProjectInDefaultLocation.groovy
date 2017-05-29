package org.eclipse.buildship.core.workspace.internal

import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.core.notification.UserNotification
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.test.fixtures.TestEnvironment.*

class ImportingProjectInDefaultLocation extends ProjectSynchronizationSpecification {

    def "Can import projects located in workspace folder with default name"() {
        when:
        synchronizeAndWait(workspaceDir('sample'))

        then:
        findProject("sample")
    }

    def "Disallow importing projects located in workspace folder and with custom root name"() {
        setup:
        Logger logger = Mock(Logger)
        UserNotification notification = Mock(UserNotification)
        environment.registerService(Logger, logger)
        environment.registerService(UserNotification, notification)
        File rootProject = fileTree(workspaceDir('sample')) {
            file 'settings.gradle', "rootProject.name = 'my-project-name-is-different-than-the-folder'"
        }

        when:
        synchronizeAndWait(rootProject)

        then:
        1 * logger.warn(*_)
        1 * notification.errorOccurred(*_)
    }

    def "Disallow synchornizing projects located in workspace folder and with custom root name"() {
        setup:
        Logger logger = Mock(Logger)
        UserNotification notification = Mock(UserNotification)
        environment.registerService(Logger, logger)
        environment.registerService(UserNotification, notification)
        File rootProject = workspaceDir('sample2')

        when:
        synchronizeAndWait(rootProject)

        then:
        0 * logger.warn(*_)
        0 * notification.errorOccurred(*_)

        when:
        new File(rootProject, 'settings.gradle') << "rootProject.name = 'my-project-name-is-different-than-the-folder'"
        synchronizeAndWait(rootProject)

        then:
        1 * logger.warn(*_)
        1 * notification.errorOccurred(*_)
    }

    def "Disallow importing the workspace root"() {
        setup:
        Logger logger = Mock(Logger)
        UserNotification notification = Mock(UserNotification)
        environment.registerService(Logger, logger)
        environment.registerService(UserNotification, notification)

        when:
        synchronizeAndWait(workspaceDir)

        then:
        1 * logger.warn(*_)
        1 * notification.errorOccurred(*_)
    }

    def "Disallow importing any modules located at the workspace root"() {
        setup:
        Logger logger = Mock(Logger)
        UserNotification notification = Mock(UserNotification)
        environment.registerService(Logger, logger)
        environment.registerService(UserNotification, notification)
        File rootProject = workspaceDir('sample2') {
            file 'settings.gradle', "include '..'"
        }

        when:
        synchronizeAndWait(workspaceDir)

        then:
        1 * logger.warn(*_)
        1 * notification.errorOccurred(*_)
    }

}
