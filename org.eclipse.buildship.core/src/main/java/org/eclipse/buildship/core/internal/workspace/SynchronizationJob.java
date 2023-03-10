/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;


import org.gradle.tooling.CancellationTokenSource;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.SynchronizationResult;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.DefaultGradleBuild;
import org.eclipse.buildship.core.internal.operation.ToolingApiJob;

public final class SynchronizationJob extends ToolingApiJob<Void> {

    private final Iterable<GradleBuild> gradleBuilds;
    private final NewProjectHandler newProjectHandler;

    public SynchronizationJob(GradleBuild gradleBuild) {
        this(NewProjectHandler.NO_OP, ImmutableList.of(gradleBuild));
    }

    public SynchronizationJob(NewProjectHandler newProjectHandler, GradleBuild gradleBuild) {
        this(newProjectHandler, ImmutableList.of(gradleBuild));
    }

    public SynchronizationJob(NewProjectHandler newProjectHandler, Iterable<GradleBuild> gradleBuilds) {
        super("Synchronize Gradle projects with workspace");
        this.newProjectHandler = newProjectHandler;
        this.gradleBuilds = ImmutableSet.copyOf(gradleBuilds);

        // explicitly show a dialog with the progress while the project synchronization is in process
        setUser(true);
    }

    public Iterable<GradleBuild> getGradleBuilds() {
        return this.gradleBuilds;
    }

    @Override
    public Void runInToolingApi(CancellationTokenSource tokenSource, IProgressMonitor monitor) throws Exception {
        final SubMonitor progress = SubMonitor.convert(monitor, ImmutableSet.copyOf(SynchronizationJob.this.gradleBuilds).size() + 1);

        for (GradleBuild build : SynchronizationJob.this.gradleBuilds) {
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
            SynchronizationResult result = ((DefaultGradleBuild)build).synchronize(SynchronizationJob.this.newProjectHandler, tokenSource, progress.newChild(1));
            if (result.getStatus().getException() instanceof Exception) {
                throw (Exception) result.getStatus().getException();
            }
        }

        return null;
    }

    /**
     * A {@link SynchronizationJob} is only scheduled if there is not already another one that
     * fully covers it.
     * <p/>
     * A job A fully covers a job B if all of these conditions are met:
     * <ul>
     * <li>A synchronizes the same Gradle builds as B</li>
     * <li>A and B have the same {@link NewProjectHandler} or B's {@link NewProjectHandler} is a
     * no-op</li>
     * </ul>
     */
    @Override
    public boolean shouldSchedule() {
        for (Job job : Job.getJobManager().find(CorePlugin.GRADLE_JOB_FAMILY)) {
            if (job instanceof SynchronizationJob && isCoveredBy((SynchronizationJob) job)) {
                return false;
            }
        }
        return true;
    }

    private boolean isCoveredBy(SynchronizationJob other) {
        return Objects.equal(this.gradleBuilds, other.gradleBuilds) && (this.newProjectHandler == NewProjectHandler.NO_OP || Objects.equal(this.newProjectHandler, other.newProjectHandler));
    }
}
