/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
