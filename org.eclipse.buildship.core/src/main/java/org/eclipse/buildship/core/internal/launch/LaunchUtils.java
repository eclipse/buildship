/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.launch;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import org.eclipse.buildship.core.internal.CorePlugin;

/**
 * Utility class to launch Gradle tasks and tests.
 */
public final class LaunchUtils {

    private LaunchUtils() {
    }

    static void launch(String name, ILaunchConfiguration configuration, String mode, ILaunch launch, Optional<? extends Job> jobOrNull, IProgressMonitor monitor) {
        monitor.beginTask(name, IProgressMonitor.UNKNOWN);
        try {
            if (!jobOrNull.isPresent()) {
                return;
            }
            Job job = jobOrNull.get();

            // schedule the task
            final CountDownLatch latch = new CountDownLatch(1);
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
