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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
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

        // if the wizard was opened the label is available, otherwise a WidgetNotFoundException is
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
        bot.textWithLabel("Project name").setText(TEST_PROJECT_NAME);

        bot.waitUntil(Conditions.shellIsActive(NEW_GRADLE_PROJECT_WIZARD_TITLE));
        bot.button(IDialogConstants.NEXT_LABEL).click();

        bot.waitUntil(Conditions.shellIsActive(NEW_GRADLE_PROJECT_WIZARD_TITLE));
        bot.button(IDialogConstants.NEXT_LABEL).click();

        // wait at the project preview page until the "< Back" button is enabled
        bot.waitUntil(Conditions.widgetIsEnabled(bot.button(IDialogConstants.BACK_LABEL)));

        File workspaceRootDir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();

        assertTrue(workspaceRootDir.isDirectory());

        File[] workspaceRootFiles = workspaceRootDir.listFiles();
        Optional<File> firstMatch = FluentIterable.of(workspaceRootFiles)
                .firstMatch(new FileNamePredicate(TEST_PROJECT_NAME));

        // check if the "TestProject" has been created
        assertTrue(firstMatch.isPresent());
        File testProjectDir = firstMatch.get();
        assertTrue(testProjectDir.isDirectory());
        File[] testProjectFiles = testProjectDir.listFiles();

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
        bot.textWithLabel("Project name").setText(TEST_PROJECT_NAME);

        bot.waitUntil(Conditions.shellIsActive(NEW_GRADLE_PROJECT_WIZARD_TITLE));
        bot.button(IDialogConstants.NEXT_LABEL).click();

        bot.waitUntil(Conditions.shellIsActive(NEW_GRADLE_PROJECT_WIZARD_TITLE));
        bot.button(IDialogConstants.NEXT_LABEL).click();

        // wait at the project preview page until the "< Back" button is enabled
        bot.waitUntil(Conditions.widgetIsEnabled(bot.button(IDialogConstants.BACK_LABEL)));

        File workspaceRootDir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();

        assertTrue(workspaceRootDir.isDirectory());

        File[] workspaceRootFiles = workspaceRootDir.listFiles();
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
}
