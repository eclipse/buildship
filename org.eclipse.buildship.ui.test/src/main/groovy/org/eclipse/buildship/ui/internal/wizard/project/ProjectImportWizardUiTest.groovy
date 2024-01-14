/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.wizard.project

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jface.dialogs.IDialogConstants
import org.eclipse.swtbot.eclipse.finder.waits.Conditions
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.UnsupportedConfigurationException
import org.eclipse.buildship.core.internal.workspace.WorkspaceOperations
import org.eclipse.buildship.ui.internal.test.fixtures.SwtBotSpecification

class ProjectImportWizardUiTest extends SwtBotSpecification {

    def "can open import wizard from menu bar"()  {
        when:
        openGradleImportWizard()

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
        bot.textWithLabel(ProjectWizardMessages.Label_ProjectRootDirectory).setText(projectDir.canonicalPath)
        bot.button(IDialogConstants.FINISH_LABEL).click()
        waitForGradleJobsToFinish()

        then:
        bot.waitUntil(Conditions.widgetIsEnabled(bot.button(IDialogConstants.FINISH_LABEL)))

        cleanup:
        bot.button("Cancel").click()
    }

    def "import project from folder does not include Java resources of subprojects in the root project"() {
        given:
        def rootDir = dir('rootProject') {
            file 'settings.gradle', 'include "subProject"'
            dir('subProject') {
                file 'build.gradle', 'plugins { id "java-library" }'
                dir('src/main/java') {
                    file 'Foobar.java', 'class Foobar { }'
                }
            }
        }

        when:
        importProjectFromFolder rootDir
        waitForImportJobsToFinish()
        waitForGradleJobsToFinish()

        then:
        findProject('rootProject').hasNature(JavaCore.NATURE_ID) == false
        findProject('subProject').hasNature(JavaCore.NATURE_ID) == true
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

    private void importProjectFromFolder(File projectDir) {
        bot.menu("File").menu("Import...").click()
        bot.shell("Import").activate()
        bot.waitUntil(Conditions.shellIsActive("Import"))
        bot.tree().expandNode("General").select("Projects from Folder or Archive")
        bot.button("Next >").click()
        bot.comboBoxWithLabel("Import source:").setText(projectDir.canonicalPath)
        bot.button(IDialogConstants.FINISH_LABEL).click()
    }

    private void waitForImportJobsToFinish() {
        def jobFamily = Class.forName("org.eclipse.ui.internal.wizards.datatransfer.SmartImportJob")
        Job.getJobManager().join(jobFamily, null)
    }

    class FaultyWorkspaceOperations {
        @Delegate WorkspaceOperations delegate = CorePlugin.workspaceOperations()

        IProject createProject(String name, File location, List<String> natureIds, IProgressMonitor monitor) {
            throw new UnsupportedConfigurationException('test')
        }
    }
}
