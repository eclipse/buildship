/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task

import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.ProjectScope
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.preferences.IEclipsePreferences
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.workspace.FetchStrategy


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

    def "If one project has invalid configuration then tasks from other projects are still visible"() {
        given:
        def first = dir("a") { file 'build.gradle' }
        def second = dir("b") { file 'build.gradle' }
        importAndWait(first)
        importAndWait(second)
        IEclipsePreferences preferences = new ProjectScope(findProject('a')).getNode(CorePlugin.PLUGIN_ID)
        preferences.put('connection.project.dir', 'invalid')
        preferences.flush()

        findProject('a').refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor())

        when:
        reloadTaskView()

        then:
        waitFor { !taskTree.a }
        waitFor { taskTree.b }
    }

    def "The task view is refreshed when projects are added/deleted"() {
        given:
        def first = dir("a") { file 'build.gradle' }
        def second = dir("b") { file 'build.gradle' }
        importAndWait(first)
        importAndWait(second)

        when:
        findProject("b").delete(true, true, null)
        waitForTaskView()

        then:
        waitFor { taskTree.collect { k, v -> k } == ['a'] }
    }

    def "The task view is refreshed when projects are closed/opened"() {
        given:
        def closed = dir("a") { file 'build.gradle' }
        def opened = dir("b") { file 'build.gradle' }
        importAndWait(closed)
        importAndWait(opened)

        when:
        findProject("b").close(null)
        waitForTaskView()

        then:
        waitFor { taskTree.collect { k, v -> k } == ['a'] }
    }

    def "Subprojects should be under the parent project's folder when not showing flatten project hierarchy"() {
        given:
        view.state.projectHierarchyFlattened = false

        def firstRoot = dir("root1") {
            file 'settings.gradle', "include 'sub', 'sub:ss1'"

            file 'build.gradle'

            dir ('sub') {
                dir('ss1')
            }
        }

         def secondRoot = dir("root2") {
            file 'settings.gradle'
        }

        importAndWait(firstRoot)
        importAndWait(secondRoot)

        when:
        waitForTaskView()

        then:
        taskTree.collect { k, v -> k } == ['root1','root2']
        taskTree.root1.sub.containsKey('ss1')
    }

    def "Project nodes should come before task group nodes if hierarchy is not flattened"() {
        setup:
        view.state.projectHierarchyFlattened = false

        when:
        def project = dir("root") {
            file 'build.gradle', """
                task foo() {
                    group = 'b'
                }
            """
            file 'settings.gradle', """
                include 'z'
                include 'a'
            """
            dir('z')
            dir('a')
        }
        importAndWait(project)
        waitForTaskView()

        then:
        List childrenNames = taskTree.root.collect { k, v -> k }
        childrenNames[0..2] == ['a', 'z', 'b']
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
