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

package com.gradleware.tooling.eclipse.ui;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * Contains convenience test setup and helper methods for tests based on SWTBot .
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class AbstractSwtbotTest {

    // todo (etst) review

    protected static SWTWorkbenchBot bot;

    @BeforeClass
    public static void closeWelcomePageIfAny() throws Exception {
        bot = new SWTWorkbenchBot();
        try {
            SWTBotView view = bot.activeView();
            if (view.getTitle().equals("Welcome")) {
                view.close();
            }
        } catch (WidgetNotFoundException e) {
            UiPlugin.logger().error("Failed to initialize SWTBot test", e);
        }
    }

    @Before
    public void forceShellActivateToMakeTestWorkingOnLinux() {
        // http://wiki.eclipse.org/SWTBot/Troubleshooting#No_active_Shell_when_running_SWTBot_tests_in_Xvfb
        UIThreadRunnable.syncExec(new VoidResult() {

            @Override
            public void run() {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().forceActive();
            }
        });
    }

    @After
    public void waitJobsToFinishAfterTextExecution() {
        waitForJobs();
    }

    protected void delay(long waitTimeMillis) {
        Display display = Display.getCurrent();
        if (display != null) {
            long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
            while (System.currentTimeMillis() < endTimeMillis) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
            display.update();
        } else {
            try {
                Thread.sleep(waitTimeMillis);
            } catch (InterruptedException e) {
            }
        }
    }

    protected void waitForJobs() {
        while (Job.getJobManager().currentJob() != null) {
            delay(400);
        }
    }

}
