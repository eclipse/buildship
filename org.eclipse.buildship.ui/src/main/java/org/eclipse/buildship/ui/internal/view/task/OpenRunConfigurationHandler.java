/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.ui.internal.UiPluginConstants;

/**
 * Opens a run configuration dialog for the selected Gradle tasks. Either a new
 * {@link org.eclipse.debug.core.ILaunchConfiguration} instance is created or an existing one is
 * reused if it already exists for the selected tasks.
 */
public final class OpenRunConfigurationHandler extends BaseRunConfigurationHandler {

    @Override
    public Object execute(ExecutionEvent event) {
        // determine the set of attributes that uniquely identify a run configuration
        GradleRunConfigurationAttributes configurationAttributes = getRunConfigurationAttributes(event);

        // create/reuse a launch configuration for the given attributes
        ILaunchConfiguration launchConfiguration = CorePlugin.gradleLaunchConfigurationManager().getOrCreateRunConfiguration(configurationAttributes);

        // open the launch configuration dialog for the matching launch configuration
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        DebugUITools.openLaunchConfigurationPropertiesDialog(shell, launchConfiguration, UiPluginConstants.RUN_LAUNCH_GROUP_ID);

        return null;
    }

}
