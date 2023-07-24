/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.invocation

import org.hamcrest.core.IsAnything

import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IProject
import org.eclipse.swtbot.eclipse.finder.waits.Conditions
import org.eclipse.ui.IEditorDescriptor
import org.eclipse.ui.IEditorReference
import org.eclipse.ui.IWorkbenchPage
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.part.FileEditorInput

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.console.ProcessStreamsProvider
import org.eclipse.buildship.ui.internal.test.fixtures.SwtBotSpecification
import org.eclipse.buildship.ui.internal.test.fixtures.TestProcessStreamProvider

class RunEclipseAutoBuildTasksTest extends SwtBotSpecification {

    def setup() {
        environment.registerService(ProcessStreamsProvider, new TestProcessStreamProvider() {})
    }

    def cleanup() {
        bot.closeAllEditors()
    }

    String getSyncConsoleOutput() {
        TestProcessStreamProvider testStreams = CorePlugin.processStreamsProvider()
        testStreams.backroundStream.out
    }

    def "Runs auto-build task"() {
        setup:
        def location = dir('run-eclipse-auto-build-task') {
            file 'build.gradle', '''
                plugins {
                    id 'java-library'
                    id 'eclipse'
                }

                task foo {
                    doLast {
                        println 'foo'
                    }
                }

                eclipse {
                    autoBuildTasks foo
                }
            '''
            dir('src/main/java') {
                file 'A.java', ''
            }
            file 'settings.gradle', ''
         }
         importAndWait(location)
         openInEditor(findProject('run-eclipse-auto-build-task').getFile('src/main/java/A.java'))

         expect:
         !syncConsoleOutput.contains("> Task :foo")

         when:
         bot.sleep(500)
         bot.activeEditor().toTextEditor().insertText("public class A {}")
         bot.activeEditor().save()

         then:
         waitFor { syncConsoleOutput.contains("> Task :foo") }

         cleanup:
         bot.activeEditor().close()
    }

    private void openInEditor(IFile file) {
        IEditorDescriptor desc = PlatformUI.workbench.editorRegistry.getDefaultEditor(file.getName());
        IWorkbenchPage page = PlatformUI.workbench.workbenchWindows[0].pages[0];
        runOnUiThread { page.openEditor(new FileEditorInput(file), desc.getId()) }
        bot.waitUntil(Conditions.waitForEditor(new IsAnything<IEditorReference>()))
    }
}
