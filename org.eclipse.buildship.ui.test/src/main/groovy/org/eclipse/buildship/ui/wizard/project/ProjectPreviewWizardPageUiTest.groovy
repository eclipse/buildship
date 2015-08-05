/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ian Stewart-Binks (Red Hat, Inc.) - Bug 471095
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - Bug 471095
 */

package org.eclipse.buildship.ui.wizard.project

import org.eclipse.swtbot.eclipse.finder.waits.Conditions
import org.eclipse.core.runtime.ILogListener
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Platform;

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.ui.SWTBotTestHelper
import org.eclipse.buildship.ui.UiPlugin;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import spock.lang.Specification
import org.eclipse.buildship.ui.test.fixtures.TestEnvironment
import org.eclipse.core.runtime.jobs.Job


class ProjectPreviewWizardPageUiTest extends Specification {
    SWTWorkbenchBot bot = SWTBotTestHelper.getBot()

    @Rule
    TemporaryFolder tempFolder

    ILogListener logListener

    def setup() {
        SWTBotTestHelper.closeAllShellsExceptTheApplicationShellAndForceShellActivation()
        logListener = Mock(ILogListener)
        UiPlugin.getInstance().getLog().addLogListener(logListener)
    }

    def cleanup() {
        UiPlugin.getInstance().getLog().removeLogListener(logListener)
    }

    def setupSpec() {
        SWTBotTestHelper.closeWelcomePageIfAny()
    }

    def "Stop preview and close import wizard before import job finishes"() {
        given:
        File location = tempFolder.newFolder("new-folder")

        new File(location, "build.gradle") << """
            Thread.sleep(5000)"
            task takesLongTimeToLoad {
                Thread.sleep(5000)
                doLast {
                    Thread.sleep(5000)
                }
            }"""

        CorePlugin.workspaceOperations().createProject('project-name', location, [], [], new NullProgressMonitor())

        when:
        startImportPreviewAndCancelWizard(location)
        SWTBotTestHelper.waitForJobsToFinish()

        then:
        1 * logListener.logging(*_) >> { arguments ->
            IStatus status = arguments[0]
            assert status.getException() instanceof InterruptedException
        }
    }

    private def startImportPreviewAndCancelWizard(File location) {
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
