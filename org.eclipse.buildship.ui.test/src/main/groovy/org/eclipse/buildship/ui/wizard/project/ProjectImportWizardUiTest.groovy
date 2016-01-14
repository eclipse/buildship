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

import org.junit.Ignore;

import com.google.common.base.Charsets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell
import static org.eclipse.buildship.ui.test.fixtures.LegacyEclipseSpockTestHelper.*

import org.eclipse.buildship.ui.test.fixtures.SwtBotSpecification

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

    @Ignore("Deadlocks on TC. When SWT bot times out, the synchronize job is still running and waiting for input, so the project.delete at the end of the test deadlocks.")
    def "asks the user whether to keep existing .project files"() {
        setup:
        def project = createOpenProject("sample-project")
        file project, "build.gradle", "apply plugin: 'java'"
        def location = project.location.toString()
        project.delete(false, true, null)
        openGradleImportWizard()
        pressNext()
        bot.text(0).setText(location)
        pressFinish()
        bot.waitUntil(Conditions.shellIsActive(ProjectWizardMessages.Existing_Descriptors_Overwrite_Dialog_Header))

        when:
        bot.button(ProjectWizardMessages.Existing_Descriptors_Overwrite).click()
        waitForJobsToFinish()

        then:
        project.hasNature(JavaCore.NATURE_ID)

        cleanup:
        project.delete(true, null)
    }

    private static def IProject createOpenProject(String name) {
        def IProject project = getWorkspace().getRoot().getProject(name)
        project.create(null)
        project.open(null)
        return project
    }

    private static def file (IProject project, String name, CharSequence content) {
        project.getFile(name).create(new ByteArrayInputStream(content.toString().getBytes(Charsets.UTF_8)), true, null)
    }

    private static def openGradleImportWizard() {
        bot.menu("File").menu("Import...").click()
        SWTBotShell shell = bot.shell("Import")
        shell.activate()
        bot.waitUntil(Conditions.shellIsActive("Import"))
        bot.tree().expandNode("Gradle").select("Gradle Project")
        pressNext()
    }

    private static def pressNext() {
        bot.button("Next >").click()
    }

    private static def pressFinish() {
        bot.button("Finish").click()
    }

}
