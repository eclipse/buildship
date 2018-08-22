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

package org.eclipse.buildship.ui.internal.test.fixtures

import org.junit.Rule
import org.junit.rules.ExternalResource

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable
import org.eclipse.swtbot.swt.finder.results.BoolResult
import org.eclipse.swtbot.swt.finder.results.VoidResult
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.console.ConsolePlugin
import org.eclipse.ui.console.IConsole
import org.eclipse.ui.console.IConsoleListener
import org.eclipse.ui.console.IConsoleManager

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.ui.internal.UiPlugin
import org.eclipse.buildship.ui.internal.console.GradleConsole

abstract class SwtBotSpecification extends ProjectSynchronizationSpecification {

    @Rule
    TestConsoleHandler consoles = new TestConsoleHandler()

    protected static SWTWorkbenchBot bot = new SWTWorkbenchBot()

    def setupSpec() {
        closeWelcomePageIfAny()
    }

    def setup() {
        closeAllShellsExceptTheApplicationShellAndForceShellActivation()
    }

    protected void deleteAllProjects(boolean includingContent) {
        for (IProject project : CorePlugin.workspaceOperations().allProjects) {
            project.delete(includingContent, true, null)
        }
    }

    private static void closeWelcomePageIfAny() {
        try {
            SWTBotView view = bot.activeView()
            if (view.title.equals("Welcome")) {
                view.close()
            }
        } catch (WidgetNotFoundException e) {
            UiPlugin.logger().info('No active view', e)
        }
    }

    private static void closeAllShellsExceptTheApplicationShellAndForceShellActivation() {
        // in case a UI test fails some shells might not be closed properly, therefore we close
        // these here and log it
        bot.shells().findAll{ it.isOpen() && !isEclipseApplicationShell(it) }.each {
            bot.captureScreenshot(it.text + " NotClosed.jpg")
            UiPlugin.logger().warn(it.text + " was not closed properly.")
            try {
                it.close()
            } catch (TimeoutException e) {
                UiPlugin.logger().error("Unable to close shell ${it.text}")
            }
        }

        // http://wiki.eclipse.org/SWTBot/Troubleshooting#No_active_Shell_when_running_SWTBot_tests_in_Xvfb
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                PlatformUI.workbench.activeWorkbenchWindow.shell.forceActive()
            }
        })
    }

    protected static boolean isEclipseApplicationShell(final SWTBotShell swtBotShell) {
        return UIThreadRunnable.syncExec(new BoolResult() {
            @Override
            public Boolean run() {
                PlatformUI.workbench.activeWorkbenchWindow.shell.equals(swtBotShell.widget)
            }
        })
    }

    class TestConsoleHandler extends ExternalResource implements IConsoleListener {
        IConsole activeConsole

        @Override
        public void consolesAdded(IConsole[] consoles) {
            activeConsole = consoles[0]
        }

        @Override
        public void consolesRemoved(IConsole[] consoles) {
        }

        @Override
        protected void before() throws Throwable {
            ConsolePlugin.default.consoleManager.addConsoleListener(this)
        }

        @Override
        protected void after() {
            ConsolePlugin.default.consoleManager.removeConsoleListener(this)
            removeConsoles()
        }

        public String getActiveConsoleContent() {
            waitForConsoleOutput()
            activeConsole.document.get().trim()
        }

        public void waitForConsoleOutput() {
            SwtBotSpecification.this.waitFor {
                activeConsole != null &&
                (!(activeConsole instanceof GradleConsole) || (activeConsole.closeable && activeConsole.processDescription.get().job.state == Job.NONE)) &&
                activeConsole.partitioner.pendingPartitions.empty
            }
        }

        protected void removeConsoles() {
            IConsoleManager consoleManager = ConsolePlugin.default.consoleManager
            List<IConsole> consoles = consoleManager.consoles.findAll { console -> !(console instanceof GradleConsole) || console.isCloseable()}
            consoleManager.removeConsoles(consoles as IConsole[])
        }
    }

}
