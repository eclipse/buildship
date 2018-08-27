package org.eclipse.buildship.ui.internal.wizard.project

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.jface.dialogs.IDialogConstants
import org.eclipse.jface.wizard.WizardDialog
import org.eclipse.swtbot.eclipse.finder.waits.Conditions
import org.eclipse.swtbot.swt.finder.SWTBot
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition
import org.eclipse.swtbot.swt.finder.waits.ICondition
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.UnsupportedConfigurationException
import org.eclipse.buildship.core.internal.workspace.WorkspaceOperations
import org.eclipse.buildship.ui.internal.test.fixtures.SwtBotSpecification

class ProjectImportWizardUiTest extends SwtBotSpecification {

    def "can open import wizard from menu bar"()  {
        setup:
        openGradleImportWizard()

        when:
        bot.styledText(ProjectWizardMessages.InfoMessage_GradleWelcomeWizardPageContext)

        then:
        // if widget is not available then a WidgetNotFoundException is thrown
        notThrown Exception

        cleanup:
        // cancel the wizard
        bot.button("Cancel").click()
    }

    def "import wizard closes even project import fails and only imports the root project"() {
        setup:
        File projectDir = dir('broken-project') {
            file 'build.gradle', ''
            file 'settings.gradle', 'include "sub"'
            dir('sub') {
                file 'build.gradle', 'I_AM_ERROR'
            }
        }

        when:
        SWTBotShell wizard = openGradleImportWizard()
        bot.button(IDialogConstants.NEXT_LABEL).click()
        bot.textWithLabel(ProjectWizardMessages.Label_ProjectRootDirectory).setText(projectDir.canonicalPath)
        bot.button(IDialogConstants.FINISH_LABEL).click()

        then:
        bot.waitUntil(Conditions.shellCloses(wizard), 10000)
    }

    def "import wizard does not close if the root project import fails"() {
        setup:
        environment.registerService(WorkspaceOperations, new FaultyWorkspaceOperations())
        File projectDir = dir('broken-project')

        when:
        openGradleImportWizard()
        bot.button(IDialogConstants.NEXT_LABEL).click()
        bot.textWithLabel(ProjectWizardMessages.Label_ProjectRootDirectory).setText(projectDir.canonicalPath)
        bot.button(IDialogConstants.FINISH_LABEL).click()
        waitForGradleJobsToFinish()

        then:
        bot.waitUntil(Conditions.widgetIsEnabled(bot.button(IDialogConstants.FINISH_LABEL)))

        cleanup:
        bot.button("Cancel").click()
    }

    private static SWTBotShell openGradleImportWizard() {
        bot.menu("File").menu("Import...").click()
        SWTBotShell shell = bot.shell("Import")
        shell.activate()
        bot.waitUntil(Conditions.shellIsActive("Import"))
        bot.tree().expandNode("Gradle").select("Existing Gradle Project")
        bot.button("Next >").click()
        bot.shell(ProjectWizardMessages.Title_GradleProjectWizardPage)
    }

    class FaultyWorkspaceOperations {
        @Delegate WorkspaceOperations delegate = CorePlugin.workspaceOperations()

        void addNature(IProject project, String natureId, IProgressMonitor monitor) {
            throw new UnsupportedConfigurationException('test')
        }
    }
}
