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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

import org.eclipse.buildship.core.internal.CorePlugin;

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
        monitor.beginTask("Launch Gradle tasks", IProgressMonitor.UNKNOWN);
        try {
            // schedule the task
            final CountDownLatch latch = new CountDownLatch(1);
            RunGradleBuildLaunchRequestJob job = new RunGradleBuildLaunchRequestJob(launch);
            job.addJobChangeListener(new JobChangeAdapter() {

                @Override
                public void done(IJobChangeEvent event) {
                    latch.countDown();
                }
            });
            job.schedule();

            // block until the task execution job has finished successfully or failed,
            // periodically check if this launch has been cancelled, and if so, cancel
            // the task execution job
            try {
                boolean cancelRequested = false;
                while (!latch.await(500, TimeUnit.MILLISECONDS)) {
                    // regularly check if the job was cancelled
                    // until the job is either finished or failed
                    if (monitor.isCanceled() && !cancelRequested) {
                        // cancel the job only once
                        job.cancel();
                        cancelRequested = true;
                    }
                }
            } catch (InterruptedException e) {
                CorePlugin.logger().error("Failed to launch Gradle tasks.", e);
            }
        } finally {
            monitor.done();

            // explicitly remove the launch since the code in DebugPlugin never removes the launch
            // (we depend on the removal event being sent in the 'close console actions' since no
            // other events of interest to us get ever fired)
            DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
        }
    }

}
