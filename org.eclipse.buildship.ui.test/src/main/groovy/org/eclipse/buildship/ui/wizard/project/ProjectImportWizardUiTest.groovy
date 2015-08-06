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

package org.eclipse.buildship.ui.wizard.project

import org.junit.Test

import org.eclipse.swtbot.eclipse.finder.waits.Conditions
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell

import org.eclipse.buildship.ui.SwtBotSpecification

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

    private def openGradleImportWizard() {
        bot.menu("File").menu("Import...").click()
        SWTBotShell shell = bot.shell("Import")
        shell.activate()
        bot.waitUntil(Conditions.shellIsActive("Import"))
        bot.tree().expandNode("Gradle").select("Gradle Project")
        bot.button("Next >").click()
    }

}
