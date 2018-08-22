package org.eclipse.buildship.ui.internal.view.task

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem

class TaskViewExecutionTest extends BaseTaskViewTest {

    static String FILE_SEP = File.separator
    static String LINE_SEP = System.getProperty('line.separator')

    def "Project task executes task on target project only"() {
        setup:
        def project = sampleHierarchicalProject()
        importAndWait(project)

        when:
        focusTasksView()
        tree.setFocus()
        SWTBotTreeItem groupNode = tree.expandNode('sub', 'custom')

        then:
        groupNode.items.size() == 2

        when:
        groupNode.items[0].select()
        groupNode.items[0].doubleClick()

        String consoleOutput = consoles.activeConsoleContent

        then:
        consoleOutput.contains("Working Directory: ${project.canonicalPath}${LINE_SEP}")
        !consoleOutput.contains("Running task on project root")
        consoleOutput.contains("Running task on project sub")
        !consoleOutput.contains("Running task on project ss1")
        !consoleOutput.contains("Running task on project ss2")
    }

    def "Task selector executes task on target project and on all subprojects"() {
        setup:
        def project = sampleHierarchicalProject()
        importAndWait(project)

        when:
        focusTasksView()
        tree.setFocus()
        SWTBotTreeItem groupNode = tree.expandNode('sub', 'custom')

        then:
        groupNode.items.size() == 2

        when:
        groupNode.items[1].select()
        groupNode.items[1].doubleClick()

        String consoleOutput = consoles.activeConsoleContent

        then:
        consoleOutput.contains("Working Directory: ${project.canonicalPath}${FILE_SEP}sub${LINE_SEP}")
        !consoleOutput.contains("Running task on project root")
        consoleOutput.contains("Running task on project sub")
        consoleOutput.contains("Running task on project ss1")
        consoleOutput.contains("Running task on project ss2")
    }

    def "Only project tasks are enabled for flat projects"() {
        setup:
        def project = sampleFlatProject()
        importAndWait(project)

        when:
        focusTasksView()
        tree.setFocus()
        SWTBotTreeItem groupNode = tree.expandNode('ss1', 'custom')

        then:
        groupNode.items.size() == 2

        when:
        groupNode.items[1].select()

        then:
        !groupNode.items[1].contextMenu(TaskViewMessages.Action_RunTasks_Text_Disabled_NonStandard_layout).enabled

        when:
        groupNode.items[0].select()
        groupNode.items[0].doubleClick()
        waitForGradleJobsToFinish()

        String consoleOutput = consoles.activeConsoleContent

        then:
        consoleOutput.contains("Working Directory: ${project.canonicalPath}${LINE_SEP}")
        !consoleOutput.contains("Running task on project root")
        consoleOutput.contains("Running task on project ss1")
        !consoleOutput.contains("Running task on project ss2")
    }

    private File sampleHierarchicalProject() {
        dir("root") {
            file 'settings.gradle', """
                include 'sub', 'sub:ss1', 'sub:ss2'
            """

            file 'build.gradle', sampleTaskScript()

            dir ('sub') {
                dir('ss1')
                dir('ss2')
            }
        }
    }

    private File sampleFlatProject() {
        dir('ss1')
        dir('ss2')
        dir('root') {
            file 'build.gradle', sampleTaskScript()
            file 'settings.gradle', """
                    includeFlat 'ss1'
                    includeFlat 'ss2'
                """
        }
    }

    private String sampleTaskScript() {
        return """
            allprojects {
                task foo() {
                    group = 'custom'
                     doLast {
                        println "Running task on project \$project.name"
                     }
                 }
             }
             """
    }
}
