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
