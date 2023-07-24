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

import spock.lang.Ignore

import org.eclipse.core.resources.IProject
import org.eclipse.jface.dialogs.IDialogConstants
import org.eclipse.swtbot.eclipse.finder.waits.Conditions
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell
import org.eclipse.ui.IWorkingSet
import org.eclipse.ui.IWorkingSetManager
import org.eclipse.ui.PlatformUI

import org.eclipse.buildship.ui.internal.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.ui.internal.test.fixtures.SwtBotSpecification


class ProjectCreationWizardUiTest extends SwtBotSpecification {

    def "Can open new wizard from menu bar"() {
        when:
        openGradleCreationWizard()

        then:
        // if widget is not available then a WidgetNotFoundException is thrown
        notThrown Exception

        cleanup:
        // cancel the wizard
        bot.button(IDialogConstants.CANCEL_LABEL).click()
    }

    def "Default location button initially checked"() {
        setup:
        openGradleCreationWizard()

        expect:
        // check whether the checkbox is initially checked
        SWTBotCheckBox useDefaultLocationCheckBox = bot.checkBox(ProjectWizardMessages.Button_UseDefaultLocation, 0)
        useDefaultLocationCheckBox.isChecked()

        cleanup:
        // cancel the wizard
        bot.button(IDialogConstants.CANCEL_LABEL).click()
    }

    @Ignore // flaky test
    def "Project files are deleted when cancel button is pressed on the preview page"() {
        setup:
        openGradleCreationWizard()

        when:
        goToProjectPreviewPage("TestProject")

        then:
        // after the project preview loaded the test project should be created
        File workspaceRootFolder = LegacyEclipseSpockTestHelper.workspace.root.location.toFile()
        File projectFolder = workspaceRootFolder.listFiles().find{ it.name == "TestProject" }
        projectFolder != null
        projectFolder.exists()
        projectFolder.isDirectory()

        when:
        bot.button(IDialogConstants.CANCEL_LABEL).click()

        then:
        // after the wizard cancelled the test project should be deleted
        !projectFolder.exists()
    }

    def "Can create project with Java keywords in name"() {
        setup:
        openGradleCreationWizard()

        when:
        goToProjectPreviewPage("new.synchronized.default.project")

        then:
        // after the project preview loaded the test project should be created
        File workspaceRootFolder = LegacyEclipseSpockTestHelper.workspace.root.location.toFile()
        File projectFolder = workspaceRootFolder.listFiles().find { it.name == "new.synchronized.default.project" }
        projectFolder != null
        new File(projectFolder, 'lib/src/main/java/_new/_synchronized/_default/project/Library.java').exists()
    }

    @Ignore // flaky test
    def "Check if the created project has been added to the selected working set"() {
        setup:
        openGradleCreationWizard()

        when:
        bot.textWithLabel(ProjectWizardMessages.Label_ProjectName).setText("TestProject")
        bot.checkBox("Add project to working sets").deselect()
        bot.checkBox("Add project to working sets").select()
        bot.button("Select...").click()
        // create a 'Gradle' working set if not exists
        if (!(bot.table().containsItem("Gradle"))) {
            bot.button("New...").click()
            bot.table().getTableItem('Resource').select()
            bot.button(IDialogConstants.NEXT_LABEL).click()
            bot.textWithLabel("Working set name:").setText("Gradle")
            bot.button(IDialogConstants.FINISH_LABEL).click()
        }
        // select the Gradle working set only
        bot.button("Deselect All").click()
        bot.table().getTableItem("Gradle").check()
        bot.button(IDialogConstants.OK_LABEL).click()

        then:
        // the 'Gradle' working set should be selected
        bot.comboBoxWithLabel("Working sets").selection() == 'Gradle'

        when:
        bot.button(IDialogConstants.FINISH_LABEL).click()

        // after clicking finish the new elements for the working set are not immediately applied
        waitUntilWorkingSetIsAdded()

        then:
        // the 'Gradle' working set should contain the new test project
        PlatformUI.workbench.workingSetManager.getWorkingSet('Gradle').elements.any {
            IProject project = (IProject) it.getAdapter(IProject.class)
            project == null ? false : "TestProject" == project.name
        }
    }

    private def goToProjectPreviewPage(String projectName) {
        bot.textWithLabel(ProjectWizardMessages.Label_ProjectName).setText(projectName)
        bot.button(IDialogConstants.NEXT_LABEL).click()
        bot.button(IDialogConstants.NEXT_LABEL).click()
        // wait until the project preview finishes loading and the buttons are enabled again
        // the preview can trigger a Gradle distribution download, thus we need a long timeout
        bot.waitUntil(Conditions.widgetIsEnabled(bot.button(IDialogConstants.BACK_LABEL)), 300000)
    }

    private def openGradleCreationWizard() {
        bot.menu("File").menu("New").menu("Other...").click()
        SWTBotShell shell = bot.shells().find { SWTBotShell shell -> ['New', 'Select a wizard'].contains(shell.text) }
        shell.activate()
        bot.waitUntil(Conditions.shellIsActive(shell.getText()))
        bot.tree().expandNode("Gradle").select("Gradle Project")
        bot.button(IDialogConstants.NEXT_LABEL).click()
    }

    private def waitUntilWorkingSetIsAdded() {
        bot.waitUntil(new DefaultCondition() {

            @Override
            public boolean test() throws Exception {
                IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager()
                IWorkingSet gradleWorkingSet = workingSetManager.getWorkingSet("Gradle")
                return gradleWorkingSet.getElements().length > 0
            }

            @Override
            public String getFailureMessage() {
                return "The Gradle workingset has not been added to the IWorkingSetManager, yet."
            }
        }, 60000)
    }

}
