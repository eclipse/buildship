package org.eclipse.buildship.ui.view.execution

import java.io.File

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.debug.core.ILaunchManager
import org.eclipse.swt.widgets.Tree
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree
import org.eclipse.ui.IWorkbenchPage

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes
import org.eclipse.buildship.core.util.gradle.GradleDistributionSerializer
import org.eclipse.buildship.ui.external.viewer.FilteredTree
import org.eclipse.buildship.ui.test.fixtures.SwtBotSpecification
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils

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
        dir('root') { file 'build.gradle', """
                task foo() {
                    group = 'custom'
                 }

            """ }
    }

}
