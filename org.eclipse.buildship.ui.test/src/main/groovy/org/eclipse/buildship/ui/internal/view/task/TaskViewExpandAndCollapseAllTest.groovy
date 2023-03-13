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

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem

import groovy.sql.ExpandedVariable

class TaskViewExpandAndCollapseAllTest extends BaseTaskViewTest {

    def setup() {
        def project = sampleProject()
        importAndWait(project)
        focusTasksView()
        tree.setFocus()
    }

    def "Expand and collapse all"() {
        setup:
        SWTBotTreeItem rootNode = tree.getTreeItem('root')

        expect:
        !rootNode.expanded

        when:
        bot.viewByTitle('Gradle Tasks').toolbarButton("Expand All").click()

        then:
        rootNode.expanded
        rootNode.getNode('build setup').expanded
        rootNode.getNode('help').expanded
        rootNode.getNode('custom').expanded

        when:
        bot.viewByTitle('Gradle Tasks').toolbarButton("Collapse All").click()

        then:
        !rootNode.expanded
    }

    private File sampleProject() {
        dir('root') {
            file 'build.gradle', """
                task foo() {
                    group = 'custom'
                     doLast {
                        println "Running task on root project"
                     }
                 }
            """
        }
    }
}
