/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task

import org.eclipse.ui.console.ConsolePlugin
import org.eclipse.ui.console.TextConsole

import org.eclipse.buildship.core.internal.workspace.FetchStrategy
import org.eclipse.buildship.ui.internal.console.GradleConsole


class TaskViewReloadTest extends BaseTaskViewTest {

    def "Task view reload does not trigger synchronization tasks"() {
        setup:
        def project = dir("root") {
            file 'build.gradle', """
                plugins {
                    id 'eclipse'
                }

                task testingTaskView {
                }

                eclipse {
                    synchronizationTasks testingTaskView
                }
            """
        }
        importAndWait(project)
        waitForTaskView()

        operationsConsole.clearConsole()
        waitFor { operationsConsole.document.get() == '' }

        when:
        reloadTaskView()

        then:
        !operationsConsole.document.get().contains('> Task :testingTaskView')
    }

    private TextConsole getOperationsConsole() {
        def consoleManager = ConsolePlugin.default.consoleManager
        consoleManager.consoles.find { console -> (console instanceof GradleConsole) || !console.isCloseable()}
    }

    private def reloadTaskView() {
        view.reload(FetchStrategy.FORCE_RELOAD)
        waitForTaskView()
    }
}
