package org.eclipse.buildship.ui.internal.view.task

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree
import org.eclipse.ui.IWorkbenchPage
import org.eclipse.ui.console.IConsoleConstants

import org.eclipse.buildship.ui.internal.test.fixtures.SwtBotSpecification
import org.eclipse.buildship.ui.internal.util.workbench.WorkbenchUtils

abstract class BaseTaskViewTest extends SwtBotSpecification {

    boolean originalProjectTasksVisible
    boolean originalTaskSelectorsVisible
    boolean originalflattenProjectHiearchy

    TaskView view
    SWTBotTree tree

    def setup() {
        runOnUiThread {
            view = WorkbenchUtils.showView(TaskView.ID, null, IWorkbenchPage.VIEW_ACTIVATE)
            tree = new SWTBotTree(view.treeViewer.tree)

            originalProjectTasksVisible = view.state.projectTasksVisible
            originalTaskSelectorsVisible = view.state.taskSelectorsVisible
            originalflattenProjectHiearchy=view.state.projectHierarchyFlattened
            view.state.projectTasksVisible = true
            view.state.taskSelectorsVisible = true
            view.state.projectHierarchyFlattened = true

            WorkbenchUtils.showView(IConsoleConstants.ID_CONSOLE_VIEW, null, IWorkbenchPage.VIEW_ACTIVATE)
        }
        waitForTaskView()
    }

    def cleanup() {
        view.state.projectTasksVisible = originalProjectTasksVisible
        view.state.taskSelectorsVisible = originalTaskSelectorsVisible
        view.state.projectHierarchyFlattened = originalflattenProjectHiearchy
    }

    protected void focusTasksView() {
        bot.viewByTitle('Gradle Tasks').show()
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
}
