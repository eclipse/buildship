package org.eclipse.buildship.ui.view.task

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree
import org.eclipse.ui.IWorkbenchPage
import org.eclipse.ui.console.ConsolePlugin
import org.eclipse.ui.console.IConsole
import org.eclipse.ui.console.IConsoleConstants
import org.eclipse.ui.console.IConsoleListener
import org.eclipse.ui.console.IConsoleManager

import org.eclipse.buildship.ui.console.GradleConsole
import org.eclipse.buildship.ui.test.fixtures.SwtBotSpecification
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils

abstract class BaseTaskViewTest extends SwtBotSpecification {

    ConsoleListener consoleListener
    boolean originalProjectTasksVisible
    boolean originalTaskSelectorsVisible

    TaskView view
    SWTBotTree tree

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

    protected void focusTasksView() {
        bot.viewByTitle('Gradle Tasks').show()
    }

    protected GradleConsole getActiveConsole() {
        consoleListener.activeConsole
    }

    protected void removeCloseableGradleConsoles() {
        IConsoleManager consoleManager = ConsolePlugin.default.consoleManager
        List<GradleConsole> gradleConsoles = consoleManager.consoles.findAll { console -> console instanceof GradleConsole && console.isCloseable() }
        consoleManager.removeConsoles(gradleConsoles as IConsole[])
    }

    /*
     * The task view is refreshed whenever a project is added/removed.
     * So first we need to wait for this addition/removal event.
     * The task view starts a synchronization job to get the latest model and that job then synchronously updates the view.
     * We need to wait for that job to finish before the task view is guaranteed to be up-to-date.
     */
    protected waitForTaskView() {
        waitForResourceChangeEvents()
        waitForGradleJobsToFinish()
    }

    protected void waitForConsoleOutput() {
        waitFor {
            activeConsole != null && activeConsole.isCloseable() && activeConsole.isTerminated() && activeConsole.partitioner.pendingPartitions.empty
        }
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
