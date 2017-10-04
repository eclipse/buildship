package org.eclipse.buildship.ui.view.execution

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.core.runtime.jobs.Job
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationType
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.debug.core.ILaunchManager
import org.eclipse.swt.widgets.Tree
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree
import org.eclipse.ui.IWorkbenchPage
import org.eclipse.ui.console.ConsolePlugin
import org.eclipse.ui.console.IConsole
import org.eclipse.ui.console.IConsoleListener
import org.eclipse.ui.console.IConsoleManager

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes
import org.eclipse.buildship.core.launch.GradleRunConfigurationDelegate
import org.eclipse.buildship.core.launch.RunGradleBuildLaunchRequestJob
import org.eclipse.buildship.core.util.gradle.GradleDistributionSerializer
import org.eclipse.buildship.ui.console.GradleConsole
import org.eclipse.buildship.ui.external.viewer.FilteredTree
import org.eclipse.buildship.ui.test.fixtures.SwtBotSpecification
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils

abstract class BaseExecutionViewTest extends SwtBotSpecification {

    ConsoleListener consoleListener
    ExecutionsView view

    def setup() {
        runOnUiThread {
            view = WorkbenchUtils.showView(ExecutionsView.ID, null, IWorkbenchPage.VIEW_ACTIVATE)
            ConsolePlugin.default.consoleManager.addConsoleListener(consoleListener = new ConsoleListener())
        }
    }

    def cleanup() {
        ConsolePlugin.default.consoleManager.removeConsoleListener(consoleListener)
        removeCloseableGradleConsoles()
    }

    protected GradleConsole getActiveConsole() {
        consoleListener.activeConsole
    }

    protected SWTBotTree getCurrentTree() {
        Tree filteredTree = ((FilteredTree)view.currentPage.getPageControl()).viewer.tree
        new SWTBotTree(filteredTree)
    }

    protected void removeCloseableGradleConsoles() {
        IConsoleManager consoleManager = ConsolePlugin.default.consoleManager
        List<GradleConsole> gradleConsoles = consoleManager.consoles.findAll { console -> console instanceof GradleConsole && console.isCloseable() }
        consoleManager.removeConsoles(gradleConsoles as IConsole[])
    }

    protected void waitForConsoleOutput() {
        waitFor { activeConsole != null && activeConsole.isCloseable() && activeConsole.isTerminated() && activeConsole.partitioner.pendingPartitions.empty }
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
