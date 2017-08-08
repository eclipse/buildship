package org.eclipse.buildship.ui.view.task

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem
import org.eclipse.ui.IWorkbenchPage
import org.eclipse.ui.console.ConsolePlugin
import org.eclipse.ui.console.IConsole
import org.eclipse.ui.console.IConsoleConstants
import org.eclipse.ui.console.IConsoleListener
import org.eclipse.ui.console.IConsoleManager

import org.eclipse.buildship.ui.console.GradleConsole
import org.eclipse.buildship.ui.test.fixtures.SwtBotSpecification
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils

class TaskExecutionTest extends SwtBotSpecification {

    static String FILE_SEP = File.separator
    static String LINE_SEP = System.getProperty('line.separator')

    ConsoleListener consoleListener

    TaskView view
    SWTBotTree tree

    boolean originalProjectTasksVisible
    boolean originalTaskSelectorsVisible

    def setup() {
        runOnUiThread {
            view = WorkbenchUtils.showView(TaskView.ID, null, IWorkbenchPage.VIEW_ACTIVATE)
            tree = new SWTBotTree(view.treeViewer.tree)

            originalProjectTasksVisible = view.state.projectTasksVisible
            originalTaskSelectorsVisible = view.state.taskSelectorsVisible
            view.state.projectTasksVisible = true
            view.state.taskSelectorsVisible = true

            WorkbenchUtils.showView(IConsoleConstants.ID_CONSOLE_VIEW, null, IWorkbenchPage.VIEW_ACTIVATE)
            ConsolePlugin.default.consoleManager.addConsoleListener(consoleListener = new ConsoleListener())
        }
        waitForTaskView()
    }

    def cleanup() {
        view.state.projectTasksVisible = originalProjectTasksVisible
        view.state.taskSelectorsVisible = originalTaskSelectorsVisible

        ConsolePlugin.default.consoleManager.removeConsoleListener(consoleListener)
        removeCloseableGradleConsoles()
    }

    def "Double click on node can expand or collapse or execute"() {
        setup:
        def project = sampleHierarchicalProject()
        importAndWait(project)

        when:
        bot.viewByTitle('Gradle Tasks').show()
        tree.setFocus()
        SWTBotTreeItem rootItem = tree.getTreeItem("root")

        then:
        rootItem.isExpanded() == false

        when:
        rootItem.doubleClick();

        then:
        rootItem.isExpanded() == true

        when:
        SWTBotTreeItem customNode = rootItem.getNode("custom");

        then:
        customNode.isExpanded() == false

        when:
        customNode.doubleClick()

        then:
        customNode.isExpanded() == true

        when:
        customNode.items[0].doubleClick()

        waitForConsoleOutput()
        String consoleOutput = consoleListener.activeConsole.document.get()

        then:
        consoleOutput.contains(":foo")
        consoleOutput.contains("Running task on project root")
        !consoleOutput.contains("Running task on project sub")
        !consoleOutput.contains("Running task on project ss1")
        !consoleOutput.contains("Running task on project ss2")

        when:
        customNode.doubleClick()

        then:
        customNode.isExpanded() == false

        when:
        rootItem.doubleClick()

        then:
        rootItem.isExpanded() == false
    }

    def "Project task executes task on target project only"() {
        setup:
        def project = sampleHierarchicalProject()
        importAndWait(project)

        when:
        bot.viewByTitle('Gradle Tasks').show()
        tree.setFocus()
        SWTBotTreeItem groupNode = tree.expandNode('sub', 'custom')

        then:
        groupNode.items.size() == 2

        when:
        groupNode.items[0].select()
        groupNode.items[0].doubleClick()

        waitForConsoleOutput()
        String consoleOutput = consoleListener.activeConsole.document.get()

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
        bot.viewByTitle('Gradle Tasks').show()
        tree.setFocus()
        SWTBotTreeItem groupNode = tree.expandNode('sub', 'custom')

        then:
        groupNode.items.size() == 2

        when:
        groupNode.items[1].select()
        groupNode.items[1].doubleClick()

        waitForConsoleOutput()
        String consoleOutput = consoleListener.activeConsole.document.get()

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
        bot.viewByTitle('Gradle Tasks').show()
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
        waitForConsoleOutput()
        String consoleOutput = consoleListener.activeConsole.document.get()

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

    /*
     * The task view is refreshed whenever a project is added/removed.
     * So first we need to wait for this addition/removal event.
     * The task view starts a synchronization job to get the latest model and that job then synchronously updates the view.
     * We need to wait for that job to finish before the task view is guaranteed to be up-to-date.
     */
    private waitForTaskView() {
        waitForResourceChangeEvents()
        waitForGradleJobsToFinish()
    }

    private void waitForConsoleOutput() {
        waitFor {
            GradleConsole console = consoleListener.activeConsole
            console != null && console.isCloseable() && console.isTerminated() && console.partitioner.pendingPartitions.empty
        }
    }

    public void removeCloseableGradleConsoles() {
        IConsoleManager consoleManager = ConsolePlugin.default.consoleManager
        List<GradleConsole> gradleConsoles = consoleManager.consoles.findAll { console -> console instanceof GradleConsole && console.isCloseable() }
        consoleManager.removeConsoles(gradleConsoles as IConsole[])
    }

    class ConsoleListener implements IConsoleListener {
        GradleConsole activeConsole

        @Override
        public void consolesAdded(IConsole[] consoles) {
            activeConsole = consoles[0]
        }

        @Override
        public void consolesRemoved(IConsole[] consoles) {
        }
    }
}
