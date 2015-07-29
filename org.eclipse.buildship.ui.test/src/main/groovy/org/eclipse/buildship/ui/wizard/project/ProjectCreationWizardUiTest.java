/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.wizard.project;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.buildship.ui.BaseSWTBotTest;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

public class ProjectCreationWizardUiTest extends BaseSWTBotTest {

    private static final String NEW_GRADLE_PROJECT_WIZARD_TITLE = "New Gradle Project";
    private static final String TEST_PROJECT_NAME = "TestProject";

    @Test
    public void canOpenNewWizardFromMenuBar() throws Exception {
        openGradleNewWizard();

        // if the wizard was opened the label is available, otherwise a
        // WidgetNotFoundException is
        // thrown
        bot.text(ProjectWizardMessages.InfoMessage_NewGradleProjectWizardPageDefault);

        // cancel the wizard
        bot.button(IDialogConstants.CANCEL_LABEL).click();
    }

    @Test
    public void defaultLocationButtonInitiallyChecked() throws Exception {
        openGradleNewWizard();

        // check whether the checkbox is initially checked
        SWTBotCheckBox useDefaultLocationCheckBox = bot.checkBox(ProjectWizardMessages.Button_UseDefaultLocation, 0);
        assertTrue(useDefaultLocationCheckBox.isChecked());

        // cancel the wizard
        bot.button(IDialogConstants.CANCEL_LABEL).click();
    }

    @Test
    public void previewPageFilesAreCreatedAndDeleted() {
        openGradleNewWizard();

        bot.waitUntil(Conditions.shellIsActive(NEW_GRADLE_PROJECT_WIZARD_TITLE));
        bot.textWithLabel(ProjectWizardMessages.Label_ProjectName).setText(TEST_PROJECT_NAME);

        SWTBotButton nextButton = bot.button(IDialogConstants.NEXT_LABEL);
        bot.waitUntil(Conditions.widgetIsEnabled(nextButton));
        bot.button(IDialogConstants.NEXT_LABEL).click();

        bot.waitUntil(Conditions.shellIsActive(NEW_GRADLE_PROJECT_WIZARD_TITLE));
        bot.button(IDialogConstants.NEXT_LABEL).click();

        // wait at the project preview page until the "< Back" button is enabled
        bot.waitUntil(Conditions.widgetIsEnabled(bot.button(IDialogConstants.BACK_LABEL)));

        File workspaceRootDir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();

        assertTrue(workspaceRootDir.isDirectory());

        File[] workspaceRootFiles = workspaceRootDir.listFiles();
        assertTrue(workspaceRootFiles.length > 0);
        Optional<File> firstMatch = FluentIterable.of(workspaceRootFiles)
                .firstMatch(new FileNamePredicate(TEST_PROJECT_NAME));

        // check if the "TestProject" has been created
        assertTrue(firstMatch.isPresent());
        File testProjectDir = firstMatch.get();
        assertTrue(testProjectDir.isDirectory());
        File[] testProjectFiles = testProjectDir.listFiles();
        assertTrue(testProjectFiles.length > 0);

        // also check for at least the "build.gradle" and "settings.gradle"
        // files
        assertTrue(FluentIterable.of(testProjectFiles).anyMatch(new FileNamePredicate("build.gradle")));
        assertTrue(FluentIterable.of(testProjectFiles).anyMatch(new FileNamePredicate("settings.gradle")));

        bot.button(IDialogConstants.BACK_LABEL).click();

        // make sure that the "TestProject" is deleted in case of clicking back
        // in the wizard
        assertFalse(testProjectDir.exists());

        // cancel the wizard
        bot.button(IDialogConstants.CANCEL_LABEL).click();
    }

    @Test
    public void previewPageFilesAreDeletedOnCancel() {
        openGradleNewWizard();

        bot.waitUntil(Conditions.shellIsActive(NEW_GRADLE_PROJECT_WIZARD_TITLE));
        bot.textWithLabel(ProjectWizardMessages.Label_ProjectName).setText(TEST_PROJECT_NAME);

        SWTBotButton nextButton = bot.button(IDialogConstants.NEXT_LABEL);
        bot.waitUntil(Conditions.widgetIsEnabled(nextButton));
        nextButton.click();

        bot.waitUntil(Conditions.shellIsActive(NEW_GRADLE_PROJECT_WIZARD_TITLE));
        bot.button(IDialogConstants.NEXT_LABEL).click();

        // wait at the project preview page until the "< Back" button is enabled
        bot.waitUntil(Conditions.widgetIsEnabled(bot.button(IDialogConstants.BACK_LABEL)));

        File workspaceRootDir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();

        assertTrue(workspaceRootDir.isDirectory());

        File[] workspaceRootFiles = workspaceRootDir.listFiles();
        assertTrue(workspaceRootFiles.length > 0);
        Optional<File> firstMatch = FluentIterable.of(workspaceRootFiles)
                .firstMatch(new FileNamePredicate(TEST_PROJECT_NAME));

        // check if the "TestProject" has been created
        assertTrue(firstMatch.isPresent());
        File testProjectDir = firstMatch.get();
        assertTrue(testProjectDir.isDirectory());

        // cancel the wizard
        bot.button(IDialogConstants.CANCEL_LABEL).click();

        // make sure that the "TestProject" is deleted in case of clicking
        // cancel in the wizard
        assertFalse(testProjectDir.exists());
    }

    @Test
    public void checkProjectHasBeenAddedToWorkingSet() {
        openGradleNewWizard();

        bot.waitUntil(Conditions.shellIsActive(NEW_GRADLE_PROJECT_WIZARD_TITLE));
        bot.textWithLabel(ProjectWizardMessages.Label_ProjectName).setText(TEST_PROJECT_NAME);

        // enable working set selection
        SWTBotCheckBox enableWorkingSetsBtn = bot.checkBox("Add project to working sets");
        enableWorkingSetsBtn.select();

        bot.button("Select...").click();
        bot.waitUntil(Conditions.shellIsActive("Select Working Sets"));

        if (!(bot.table().containsItem("Gradle"))) {
            bot.button("New...").click();
            bot.waitUntil(Conditions.shellIsActive("New Working Set"));

            bot.button(IDialogConstants.NEXT_LABEL).click();
            bot.textWithLabel("Working set name:").setText("Gradle");
            bot.button(IDialogConstants.FINISH_LABEL).click();
            bot.waitUntil(Conditions.shellIsActive("Select Working Sets"));
        }

        // deselect All workingsets and select the Gradle workingset
        bot.button("Deselect All").click();
        bot.table().getTableItem("Gradle").check();

        bot.button(IDialogConstants.OK_LABEL).click();

        bot.waitUntil(Conditions.shellIsActive(NEW_GRADLE_PROJECT_WIZARD_TITLE));

        String selection = bot.comboBoxWithLabel("Working sets").selection();
        // check if the workingset is properly selected
        assertTrue("Gradle".equals(selection));

        bot.button(IDialogConstants.FINISH_LABEL).click();

        waitForJobsToFinish();
        // it seems that after clicking finish the new elements for the
        // workingset are not applied already after clicking finish
        waitUntilWorkingSetIsAdded();

        IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
        IWorkingSet gradleWorkingSet = workingSetManager.getWorkingSet("Gradle");
        IAdaptable[] elements = gradleWorkingSet.getElements();

        // check if the gradleWorkingSet contains the new test project
        assertTrue(FluentIterable.of(elements).anyMatch(new ProjectNameInProjectAdaptablePredicate(TEST_PROJECT_NAME)));
    }

    @After
    public void deleteTestProjectAfterwards() throws CoreException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(TEST_PROJECT_NAME);
        if (project != null) {
            project.delete(true, true, new NullProgressMonitor());
        }
    }

    private void waitUntilWorkingSetIsAdded() {
        bot.waitUntil(new DefaultCondition() {

            @Override
            public boolean test() throws Exception {
                IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
                IWorkingSet gradleWorkingSet = workingSetManager.getWorkingSet("Gradle");
                return gradleWorkingSet.getElements().length > 0;
            }

            @Override
            public String getFailureMessage() {
                return "The Gradle workingset has not been added to the IWorkingSetManager, yet.";
            }
        }, 1000);
    }

    private void openGradleNewWizard() {
        bot.menu("File").menu("New").menu("Other...").click();
        SWTBotShell shell = bot.shell("New");
        shell.activate();
        bot.waitUntil(Conditions.shellIsActive("New"));
        bot.tree().expandNode("Gradle").select("Gradle Project");
        bot.button(IDialogConstants.NEXT_LABEL).click();
    }

    public static class FileNamePredicate implements Predicate<File> {

        private String fileName;

        public FileNamePredicate(String fileName) {
            this.fileName = Preconditions.checkNotNull(fileName);
        }

        @Override
        public boolean apply(File file) {
            return this.fileName.equals(file.getName());
        }
    }

    public static class ProjectNameInProjectAdaptablePredicate implements Predicate<IAdaptable> {

        private String projectName;

        public ProjectNameInProjectAdaptablePredicate(String projectName) {
            this.projectName = Preconditions.checkNotNull(projectName);
        }

        @Override
        public boolean apply(IAdaptable adaptable) {
            IProject project = (IProject) adaptable.getAdapter(IProject.class);
            if (null == project) {
                return false;
            }

            return this.projectName.equals(project.getName());
        }
    }
}
