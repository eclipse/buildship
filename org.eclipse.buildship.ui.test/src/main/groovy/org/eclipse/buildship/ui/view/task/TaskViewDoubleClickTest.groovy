package org.eclipse.buildship.ui.view.task

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem

class TaskViewDoubleClickTest extends BaseTaskViewTest {

    def setup() {
        def project = sampleProject()
        importAndWait(project)
        focusTasksView()
        tree.setFocus()
    }

    def "Double-clicking project nodes expand and collapse children"() {
        setup:
        SWTBotTreeItem node = tree.getTreeItem('root')

        expect:
        !node.expanded

        when:
        node.doubleClick()

        then:
        node.expanded

        when:
        node.doubleClick()

        then:
        !node.expanded
    }

    def "Double-clicking group nodes expand and collapse children"() {
        setup:
        SWTBotTreeItem rootNode = tree.getTreeItem('root')
        rootNode.expand()
        SWTBotTreeItem node = rootNode.getNode('custom')

        expect:
        !node.expanded

        when:
        node.doubleClick()

        then:
        node.expanded

        when:
        node.doubleClick()

        then:
        !node.expanded
    }

    def "Double-clicking task nodes start Gradle build"() {
        setup:
        SWTBotTreeItem rootNode = tree.getTreeItem('root')
        rootNode.expand()
        SWTBotTreeItem groupNode = rootNode.getNode('custom')
        groupNode.expand()
        SWTBotTreeItem projectTaskNode = groupNode.items[0]
        SWTBotTreeItem taskSelectorNode = groupNode.items[1]

        expect:
        groupNode.items.size() == 2

        when:
        projectTaskNode.doubleClick()
        waitForConsoleOutput()

        then:
        activeConsole.document.get().contains("Running task on root project")

        when:
        removeCloseableGradleConsoles()
        focusTasksView()
        taskSelectorNode.doubleClick()
        waitForConsoleOutput()

        then:
        activeConsole.document.get().contains("Running task on root project")
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
