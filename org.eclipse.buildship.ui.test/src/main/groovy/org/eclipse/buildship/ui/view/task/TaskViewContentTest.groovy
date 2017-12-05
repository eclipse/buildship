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
package org.eclipse.buildship.ui.view.task

import com.gradleware.tooling.toolingmodel.repository.FetchStrategy

import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.notification.UserNotification

class TaskViewContentTest extends BaseTaskViewTest {

    def "Task are grouped by default"() {
        when:
        def project = dir("root") {
            file 'build.gradle', """
                task foo() {
                    group = 'custom'
                }
            """
        }
        importAndWait(project)
        waitForTaskView()

        then:
        waitFor { taskTree.root.custom.contains('foo') }

    }

    def "Task selectors are aggregated from subprojects"() {
        when:
        def project = dir("root") {
            file 'settings.gradle', "include 'a'"
            a {
                file 'build.gradle', """
                    task foo() {
                        group = 'custom'
                    }
                """
            }
        }
        importAndWait(project)
        waitForTaskView()

        then:
        waitFor { taskTree.root.custom.contains('foo') }
        waitFor { taskTree.a.custom.contains('foo') }
    }

    def "If a project has errors, it is still visible in the task view"() {
        given:
        def first = dir("a") { file 'build.gradle' }
        importAndWait(first)
        fileTree(first) { file 'build.gradle', 'error' }

        when:
        reloadTaskView()

        then:
        waitFor { taskTree == ['a'] }
    }

    def "Faulty projects are listed below non-faulty ones"() {
        given:
        def a = dir("a") { file 'build.gradle' }
        def b = dir("b") { file 'build.gradle' }
        importAndWait(a)
        importAndWait(b)
        fileTree(a) { file 'build.gradle', 'error' }

        when:
        reloadTaskView()

        then:
        waitFor { taskTree.collect { k, v -> k } == ['b', 'a'] }
    }

    def "Faulty projects are ordered lexicographically"() {
        given:
        def a = dir("a") { file 'build.gradle' }
        def b = dir("b") { file 'build.gradle' }
        importAndWait(a)
        importAndWait(b)
        fileTree(a) { file 'build.gradle', 'error' }
        fileTree(b) { file 'build.gradle', 'error' }

        when:
        reloadTaskView()

        then:
        waitFor { taskTree == ['a', 'b'] }
    }

    def "If one project has invalid build script then tasks from other projects are still visible"() {
        given:
        UserNotification notification = Mock(UserNotification)
        registerService(UserNotification, notification)

        def first = dir("a") { file 'build.gradle' }
        def second = dir("b") { file 'build.gradle' }
        importAndWait(first)
        importAndWait(second)
        fileTree(first) { file 'build.gradle', "error" }

        when:
        reloadTaskView()

        then:
        waitFor { !taskTree.a }
        waitFor { taskTree.b }
    }

    def "If one project has invalid configuration then tasks from other projects are still visible"(String config) {
        given:
        UserNotification notification = Mock(UserNotification)
        registerService(UserNotification, notification)

        def first = dir("a") { file 'build.gradle' }
        def second = dir("b") { file 'build.gradle' }
        importAndWait(first)
        importAndWait(second)
        fileTree(first) {
            dir('.settings') {
                file("${CorePlugin.PLUGIN_ID}.prefs").text = config
            }
        }
        findProject('a').refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor())

        when:
        reloadTaskView()

        then:
        waitFor { !taskTree.a }
        waitFor { taskTree.b }

        where:
        config << ['', 'connection.project.dir=invalid']
    }

    def "The task view is refreshed when projects are added/deleted"() {
        given:
        def first = dir("a") { file 'build.gradle' }
        def second = dir("b") { file 'build.gradle' }
        importAndWait(first)
        importAndWait(second)
        fileTree(first) { file 'build.gradle', "error" }

        when:
        findProject("b").delete(true, true, null)
        waitForTaskView()

        then:
        waitFor { taskTree.find { it == 'a' } }
        waitFor { !taskTree.find { it == 'b' } }
    }

    private def getTaskTree() {
        getChildren(tree.allItems as List)
    }

    private def getChildren(List<SWTBotTreeItem> nodes) {
        nodes.each { it.expand() }
        if (nodes.every { it.items.size() == 0 }) {
            return nodes.collect { it.text }
        } else {
            return nodes.collectEntries {
                [(it.text) : getChildren(it.items as List) ]
            }
        }
    }

    private def reloadTaskView() {
        view.reload(FetchStrategy.FORCE_RELOAD)
        waitForTaskView()
    }
}
