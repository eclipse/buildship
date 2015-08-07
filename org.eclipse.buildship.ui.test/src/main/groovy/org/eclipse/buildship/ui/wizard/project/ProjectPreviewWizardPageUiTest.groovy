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

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import org.eclipse.core.runtime.ILog
import org.eclipse.core.runtime.ILogListener
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Platform

import org.eclipse.swt.SWTException

import org.eclipse.swtbot.eclipse.finder.waits.Conditions
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell

import org.eclipse.buildship.ui.test.fixtures.SwtBotSpecification

class ProjectPreviewWizardPageUiTest extends SwtBotSpecification {

    @Rule
    TemporaryFolder tempFolder

    def "Stop preview and close import wizard before import job finishes"() {
        setup:
        // a log listener to collect logged entries
        def logEntries = []
        def logListener = new ILogListener() {
            void logging(IStatus status, String plugin) {
                logEntries.add(status)
            }
        }
        Platform.addLogListener(logListener)

        // a project taking a long time to load its model
        File location = tempFolder.newFolder("new-folder")

        new File(location, "build.gradle") << """
            Thread.sleep(5000)
            task takesLongTimeToLoad {
                Thread.sleep(5000)
                doLast {
                    Thread.sleep(5000)
                }
            }"""
        new File(location, 'settings.gradle') << ''

        when:
        startImportPreviewAndCancelWizard(location)
        waitForJobsToFinish()

        then:
        !logEntries.any {it.exception?.cause instanceof SWTException && it.exception?.cause?.message?.contains('disposed') }

        cleanup:
        Platform.removeLogListener(logListener)
    }

    private static def startImportPreviewAndCancelWizard(File location) {
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
