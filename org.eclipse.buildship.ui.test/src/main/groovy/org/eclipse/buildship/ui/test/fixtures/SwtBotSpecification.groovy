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

package org.eclipse.buildship.ui.test.fixtures

import spock.lang.Specification

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.swt.widgets.Display
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable
import org.eclipse.swtbot.swt.finder.results.VoidResult
import org.eclipse.swtbot.swt.finder.results.BoolResult
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.eclipse.ui.PlatformUI

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.ui.UiPlugin

abstract class SwtBotSpecification extends ProjectSynchronizationSpecification {

    protected static SWTWorkbenchBot bot = new SWTWorkbenchBot()

    void setupSpec() {
        closeWelcomePageIfAny()
    }

    void setup() {
        closeAllShellsExceptTheApplicationShellAndForceShellActivation()
    }

    protected void deleteAllProjects(boolean includingContent) {
        for (IProject project : CorePlugin.workspaceOperations().allProjects) {
            project.delete(includingContent, true, null);
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

}
