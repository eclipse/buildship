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

import com.google.common.base.Optional;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.launch.GradleRunConfigurationDelegate;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.UiPluginConstants;
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * {@link ILaunchListener} implementation showing/activating the Console View when a new Gradle build has launched and the {@link
 * GradleRunConfigurationAttributes#isShowConsoleView()} setting is enabled.
 * <p/>
 * The listener implementation is necessary since opening a view is a UI-related task and the launching is performed in the core component.
 */
public final class ConsoleShowingLaunchListener implements ILaunchListener {

    @Override
    public void launchAdded(ILaunch launch) {
        final Optional<GradleRunConfigurationAttributes> attributes = convertToGradleRunConfigurationAttributes(launch);
        if (attributes.isPresent() && attributes.get().isShowConsoleView()) {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    // if both the executions view and the console view should be shown, do not activate the console view
                    int mode = attributes.get().isShowExecutionView() ? IWorkbenchPage.VIEW_VISIBLE : IWorkbenchPage.VIEW_ACTIVATE;
                    WorkbenchUtils.showView(UiPluginConstants.CONSOLE_VIEW_ID, null, mode);
                }
            });
        }
    }

    private Optional<GradleRunConfigurationAttributes> convertToGradleRunConfigurationAttributes(ILaunch launch) {
        ILaunchConfigurationType type;
        try {
            type = launch.getLaunchConfiguration().getType();
        } catch (CoreException e) {
            UiPlugin.logger().error("Unable to determine launch configuration type", e);
            return Optional.absent();
        }
        if (GradleRunConfigurationDelegate.ID.equals(type.getIdentifier())) {
            return Optional.of(GradleRunConfigurationAttributes.from(launch.getLaunchConfiguration()));
        }
        return Optional.absent();
    }

    @Override
    public void launchRemoved(ILaunch launch) {
    }

    @Override
    public void launchChanged(ILaunch launch) {
    }

}
