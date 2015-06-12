/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.wizard.project;

import org.junit.Test;

import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import org.eclipse.buildship.ui.AbstractSwtbotTest;

public class ProjectImportWizardUiTest extends AbstractSwtbotTest {

    @Test
    public void canOpenImportWizardFromMenuBar() throws Exception {
        openGradleImportWizard();

        // if the wizard was opened the label is available, otherwise a WidgetNotFoundException is
        // thrown
        bot.styledText(ProjectWizardMessages.InfoMessage_GradleWelcomeWizardPageContext);

        // cancel the wizard
        bot.button("Cancel").click();
    }

    private void openGradleImportWizard() {
        bot.menu("File").menu("Import...").click();
        bot.waitUntil(Conditions.shellIsActive("Import"));
        SWTBotShell shell = bot.shell("Import");
        shell.activate();
        bot.tree().expandNode("Gradle").select("Gradle Project");
        bot.button("Next >").click();
    }

}
