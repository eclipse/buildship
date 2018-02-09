package org.eclipse.buildship.ui.view.execution

import org.eclipse.core.runtime.jobs.Job
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchManager
import org.eclipse.swt.widgets.Tree
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree
import org.eclipse.ui.IWorkbenchPage

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes
import org.eclipse.buildship.core.util.gradle.GradleDistribution
import org.eclipse.buildship.core.util.gradle.GradleDistributionSerializer
import org.eclipse.buildship.ui.external.viewer.FilteredTree
import org.eclipse.buildship.ui.test.fixtures.SwtBotSpecification
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils

abstract class BaseExecutionViewTest extends SwtBotSpecification {

    ExecutionsView view

    def setup() {
        runOnUiThread {
            view = WorkbenchUtils.showView(ExecutionsView.ID, null, IWorkbenchPage.VIEW_ACTIVATE)
        }
    }

    protected SWTBotTree getCurrentTree() {
        Tree filteredTree = ((FilteredTree)view.currentPage.getPageControl()).viewer.tree
        new SWTBotTree(filteredTree)
    }


    protected void waitForExecutionFinished() {
        waitFor { view.currentPage.progressListener.updateExecutionPageJob.state == Job.NONE }
    }

    protected void launchTaskAndWait(File projectDir, String task, List<String> arguments = []) {
        launchTask(task, projectDir, arguments)
        waitForExecutionFinished()
    }

    private void launchTask(String task, File projectDir, List<String> arguments) {
        GradleRunConfigurationAttributes attributes = new GradleRunConfigurationAttributes(
                [task],
                projectDir.absolutePath,
                GradleDistributionSerializer.INSTANCE.serializeToString(GradleDistribution.fromBuild()),
                "",
                null,
                [],
                arguments,
                true,
                true,
                false,
                false,
                false);
        ILaunchConfiguration configuration = CorePlugin.gradleLaunchConfigurationManager().getOrCreateRunConfiguration(attributes)
        configuration.launch(ILaunchManager.RUN_MODE, null)
    }
}
