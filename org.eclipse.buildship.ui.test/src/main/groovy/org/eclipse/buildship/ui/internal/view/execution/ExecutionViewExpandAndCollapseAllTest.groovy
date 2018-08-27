package org.eclipse.buildship.ui.internal.view.execution

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree

class ExecutionViewExpandAndCollapseAllTest extends BaseExecutionViewTest {

    def "Expand and collapse all"() {
        setup:
        File projectDir = sampleProject()
        importAndWait(projectDir)
        launchTaskAndWait(projectDir, 'foo')

        SWTBotTree tree = getCurrentTree()

        expect:
        tree.getTreeItem('Run build').expanded

        when:
        bot.viewByTitle('Gradle Executions').toolbarButton("Collapse All").click()

        then:
        !tree.getTreeItem('Run build').expanded

        when:
        bot.viewByTitle('Gradle Executions').toolbarButton("Expand All").click()

        then:
        tree.getTreeItem('Run build').expanded
        tree.getTreeItem('Run build').getNode('Run tasks').expanded
    }

    File sampleProject() {
        dir('root') {
            file 'build.gradle', """
                task foo() {
                    group = 'custom'
                }
            """
        }
    }

}
