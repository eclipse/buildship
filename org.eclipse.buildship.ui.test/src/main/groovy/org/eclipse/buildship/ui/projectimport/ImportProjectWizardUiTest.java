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

package org.eclipse.buildship.ui.projectimport;

import org.eclipse.buildship.ui.AbstractSwtbotTest;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.Test;

public class ImportProjectWizardUiTest extends AbstractSwtbotTest {

    // todo (etst) review

    @Test
    public void canOpenImportWizardFromMenuBar() throws Exception {
        // open the import wizard
        bot.menu("File").menu("Import...").click();
        SWTBotShell shell = bot.shell("Import");
        shell.activate();
        bot.tree().expandNode("Gradle").select("Gradle Project");
        bot.button("Next >").click();

        // if the wizard was opened the label is available, otherwise a WidgetNotFoundException is thrown
        bot.label("Project root directory");

        // cancel the wizard
        bot.button("Cancel").click();
    }

}
