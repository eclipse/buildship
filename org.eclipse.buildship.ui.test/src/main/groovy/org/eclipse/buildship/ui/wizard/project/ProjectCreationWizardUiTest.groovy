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

package org.eclipse.buildship.ui.wizard.project

import static org.junit.Assert.assertTrue

import org.junit.Test

import org.eclipse.swtbot.eclipse.finder.waits.Conditions
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell

import org.eclipse.buildship.ui.test.fixtures.SwtBotSpecification;

class ProjectCreationWizardUiTest extends SwtBotSpecification {

    def "can open new wizard from menu bar"() {
        setup:
        openGradleNewWizard()

        when:
        bot.text(ProjectWizardMessages.InfoMessage_NewGradleProjectWizardPageDefault)

        then:
        // if widget is not available then a WidgetNotFoundException is thrown
        notThrown Exception

        cleanup:
        // cancel the wizard
        bot.button("Cancel").click()
    }

    def "default location button initially checked"() {
        setup:
        openGradleNewWizard()

        expect:
        // check whether the checkbox is initially checked
        SWTBotCheckBox useDefaultLocationCheckBox = bot.checkBox(ProjectWizardMessages.Button_UseDefaultLocation, 0)
        useDefaultLocationCheckBox.isChecked()

        cleanup:
        // cancel the wizard
        bot.button("Cancel").click()
    }

    private def openGradleNewWizard() {
        bot.menu("File").menu("New").menu("Other...").click()
        SWTBotShell shell = bot.shell("New")
        shell.activate()
        bot.waitUntil(Conditions.shellIsActive("New"))
        bot.tree().expandNode("Gradle").select("Gradle Project")
        bot.button("Next >").click()
    }
}
