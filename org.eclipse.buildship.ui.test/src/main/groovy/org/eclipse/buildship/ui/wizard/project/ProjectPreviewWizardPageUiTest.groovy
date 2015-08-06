/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ian Stewart-Binks (Red Hat, Inc.) - Bug 471095
 */

package org.eclipse.buildship.ui.wizard.project

import org.eclipse.swtbot.eclipse.finder.waits.Conditions
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.ui.SWTBotTestHelper
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import org.eclipse.buildship.ui.test.fixtures.TestEnvironment
import org.eclipse.core.runtime.jobs.Job


class ProjectPreviewWizardPageUiTest extends Specification {
    SWTWorkbenchBot bot = SWTBotTestHelper.getBot()

    @Rule
    TemporaryFolder tempFolder
    File location

    def setup() {
        SWTBotTestHelper.closeAllShellsExceptTheApplicationShellAndForceShellActivation()
    }

    def setupSpec() {
        SWTBotTestHelper.closeWelcomePageIfAny()
    }

    def "Stop preview and close import wizard before import job finishes"() {
        given:
        Logger logger = Mock()
        TestEnvironment.registerService(Logger, logger)
        location = tempFolder.newFolder("new-folder")

        new File(location.toString() + "/build.gradle").withWriter('utf-8') { writer ->
            writer.writeLine "Thread.sleep(5000)"
            writer.writeLine "task takesLongTimeToLoad {"
            writer.writeLine "    Thread.sleep(5000)"
            writer.writeLine "    doLast {"
            writer.writeLine "        Thread.sleep(5000)"
            writer.writeLine "    }"
            writer.writeLine "}"
        }

        CorePlugin.workspaceOperations().createProject('project-name', location, [], [], new NullProgressMonitor())

        when:
        startImportPreviewAndCancelWizard()
        SWTBotTestHelper.waitForJobsToFinish()

        then:
        0 * logger.error(_, _)

        cleanup:
        TestEnvironment.cleanup()
    }

    private void startImportPreviewAndCancelWizard() {
        bot.menu("File").menu("Import...").click()
        SWTBotShell shell = bot.shell("Import")
        shell.activate()
        bot.waitUntil(Conditions.shellIsActive("Import"))
        bot.tree().expandNode("Gradle").select("Gradle Project")
        bot.button("Next >").click()
        bot.button("Next >").click()
        bot.textWithLabel("Project root directory").setText(location.toString())
        bot.button("Next >").click()
        bot.button("Next >").click()
        bot.toolbarButtonWithTooltip("Cancel Operation").click()
        bot.button("Cancel").click()
    }

}
