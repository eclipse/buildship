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

package org.eclipse.buildship.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.launch.GradleRunConfigurationDelegate;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.UiPluginConstants;
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils;

/**
 * {@link ILaunchListener} implementation revealing the console view when a new Gradle build has
 * launched and the {@link GradleRunConfigurationAttributes#isRevealConsoleView()} is set to true.
 * <p/>
 * The listener implementation is necessary since opening a view is a UI-related task and the
 * launching is performed in the core component.
 */
public final class ConsoleOpenerLaunchListener implements ILaunchListener {

    @Override
    public void launchAdded(ILaunch launch) {
        try {
            ILaunchConfiguration configuration = launch.getLaunchConfiguration();
            if (configuration.getType().getIdentifier().equals(GradleRunConfigurationDelegate.ID)) {
                GradleRunConfigurationAttributes attributes = GradleRunConfigurationAttributes.from(configuration);
                if (attributes.isRevealConsoleView()) {
                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            WorkbenchUtils.showView(UiPluginConstants.CONSOLE_VIEW_ID, null, IWorkbenchPage.VIEW_ACTIVATE);
                        }
                    });
                }
            }
        } catch (CoreException e) {
            UiPlugin.logger().error("Failed to open the Console View", e);
        }
    }

    @Override
    public void launchRemoved(ILaunch launch) {
    }

    @Override
    public void launchChanged(ILaunch launch) {
    }

}
