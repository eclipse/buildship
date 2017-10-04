package org.eclipse.buildship.ui.view.execution

class ExecutionViewExpandAndCollapseAllTest extends BaseExecutionViewTest {

    def "Expand and collapse all"() {
        setup:
        def project = sampleProject()
        importAndWait(project)
        launchTask(project.absolutePath, 'foo')
        waitForConsoleOutput()

        tree = getCurrentTree()

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
                    // we need to declare a longer-running task until the following bug in the executions view is resolved:
                    // https://github.com/eclipse/buildship/issues/586
                    doLast {
                        Thread.sleep(100)
                    }
                }
            """
        }
    }

}
