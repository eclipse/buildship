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

package org.eclipse.buildship.ui.internal.view.task;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationAttributes;

/**
 * Runs the currently selected Gradle tasks. The tasks are run through an
 * {@link ILaunchConfiguration} instance that is either freshly created or reused if it already
 * exists for the selected tasks.
 */
public final class RunTasksHandler extends BaseRunConfigurationHandler {

    @Override
    public Object execute(ExecutionEvent event) {
        // determine the set of attributes that uniquely identify a run configuration
        GradleRunConfigurationAttributes configurationAttributes = getRunConfigurationAttributes(event);

        // create/reuse a launch configuration for the given attributes
        ILaunchConfiguration launchConfiguration = CorePlugin.gradleLaunchConfigurationManager().getOrCreateRunConfiguration(configurationAttributes);

        // launch the launch configuration
        DebugUITools.launch(launchConfiguration, ILaunchManager.RUN_MODE);

        return null;
    }

}
