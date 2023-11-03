/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.gradleprop.test;

import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.BeforeClass;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.BoolResult;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.PlatformUI;

public abstract class SwtSpecification extends WorkspaceSpecification {

    protected static SWTWorkbenchBot bot = new SWTWorkbenchBot();

    @BeforeClass
    public static void closeWelcomePageIfAny() {
        try {
            SWTBotView view = bot.activeView();
            if (view.getTitle().equals("Welcome")) {
                view.close();
            }
        } catch (WidgetNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void closeAllShellsExceptTheApplicationShellAndForceShellActivation() throws TimeoutException {
        // in case a UI test fails some shells might not be closed properly, therefore we close
        // these here and log it
        Stream.of(bot.shells()).filter(shell -> shell.isOpen() && !isEclipseApplicationShell(shell)).forEach(shell -> {
            bot.captureScreenshot(shell.getText() + " NotClosed.jpg");
            shell.close();
        });

        // http://wiki.eclipse.org/SWTBot/Troubleshooting#No_active_Shell_when_running_SWTBot_tests_in_Xvfb
        UIThreadRunnable.syncExec(new VoidResult() {

            @Override
            public void run() {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().forceActive();
            }
        });
    }

    protected static boolean isEclipseApplicationShell(final SWTBotShell swtBotShell) {
        return UIThreadRunnable.syncExec(new BoolResult() {

            @Override
            public Boolean run() {
                return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().equals(swtBotShell.widget);
            }
        });
    }
}
