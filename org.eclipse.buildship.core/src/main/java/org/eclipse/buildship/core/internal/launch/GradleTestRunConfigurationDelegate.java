/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.launch;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

/**
 * Execute Gradle tests.
 */
public final class GradleTestRunConfigurationDelegate extends LaunchConfigurationDelegate {

    // configuration type id declared in the plugin.xml
    public static final String ID = "org.eclipse.buildship.core.launch.test.runconfiguration";

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) {
        LaunchUtils.launch("Launch Gradle Test", configuration, mode, launch, RunGradleJvmTestLaunchRequestJob.createJob(configuration, mode), monitor);
    }
}
