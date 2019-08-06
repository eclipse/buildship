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

package org.eclipse.buildship.core.internal.launch;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

/**
 * Execute Gradle tasks from the run configurations.
 * <p>
 * The delegate invokes the {@link RunGradleBuildLaunchRequestJob} job to do the actual execution
 * and waits until it finishes. It also propagates the cancellation to that job.
 */
public final class GradleRunConfigurationDelegate extends LaunchConfigurationDelegate {

    // configuration type id declared in the plugin.xml
    public static final String ID = "org.eclipse.buildship.core.launch.runconfiguration";

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) {
        LaunchUtils.launch("Launch Gradle tasks", configuration, mode, launch, new RunGradleBuildLaunchRequestJob(launch), monitor);
    }
}
