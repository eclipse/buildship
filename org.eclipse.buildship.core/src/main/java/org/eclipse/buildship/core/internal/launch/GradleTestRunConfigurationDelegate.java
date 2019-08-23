/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

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
