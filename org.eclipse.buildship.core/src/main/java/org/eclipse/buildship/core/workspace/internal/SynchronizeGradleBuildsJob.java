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

package org.eclipse.buildship.core.workspace.internal;

import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.AggregateException;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Synchronizes each of the given Gradle builds using {@link SynchronizeGradleBuildJob} and reports
 * problems to the user in bulk.
 */
final class SynchronizeGradleBuildsJob extends ToolingApiJob {

    private final ImmutableSet<FixedRequestAttributes> builds;
    private final NewProjectHandler newProjectHandler;
    private final ImmutableSet<SynchronizeGradleBuildJob> jobs;

    public SynchronizeGradleBuildsJob(Set<FixedRequestAttributes> builds, NewProjectHandler newProjectHandler) {
        super("Synchronize Gradle builds", true);
        this.builds = ImmutableSet.copyOf(builds);
        this.newProjectHandler = Preconditions.checkNotNull(newProjectHandler);
        ImmutableSet.Builder<SynchronizeGradleBuildJob> jobs = ImmutableSet.builder();
        for (FixedRequestAttributes build : builds) {
            jobs.add(new SynchronizeGradleBuildJob(build, newProjectHandler, AsyncHandler.NO_OP));
        }
        this.jobs = jobs.build();
    }

    @Override
    protected void runToolingApiJob(IProgressMonitor monitor) throws Exception {
        scheduleJobs();
        waitForJobsToFinish();
        handleResults();
    }

    private void scheduleJobs() {
        for (SynchronizeGradleBuildJob job : this.jobs) {
            job.schedule();
        }
    }


    private void waitForJobsToFinish() throws InterruptedException {
        for (SynchronizeGradleBuildJob job : this.jobs) {
            job.join();
        }
    }

    /*
     * TODO this is a poor man's version of what CoreException + MultiStatus already provide out of
     * the box We should refactor to remove this completely
     */
    private void handleResults() {
        List<Throwable> errors = Lists.newArrayList();
        for (SynchronizeGradleBuildJob job : this.jobs) {
            IStatus status = job.getResult();
            if (status != null) {
                if (status.matches(IStatus.CANCEL)) {
                    throw new OperationCanceledException();
                }
                if (!status.isOK()) {
                    Throwable error = status.getException();
                    if (error != null) {
                        errors.add(error);
                    }
                }
            }
        }
        propagate(errors);
    }

    private void propagate(List<Throwable> errors) {
        if (errors.size() == 1) {
            Throwable e = errors.get(0);
            Throwables.propagateIfPossible(e);
            throw new GradlePluginsRuntimeException(e);
        } else if (errors.size() > 1) {
            throw new AggregateException(errors);
        }
    }

    @Override
    protected void canceling() {
        for (SynchronizeGradleBuildJob job : this.jobs) {
            job.cancel();
        }
    }

    /**
     * A {@link SynchronizeGradleBuildsJob} is only scheduled if there is not already another one that fully covers it.
     * <p/>
     * A job A fully covers a job B if all of these conditions are met:
     * <ul>
     *  <li> A synchronizes the same Gradle builds as B </li>
     *  <li> A and B have the same {@link NewProjectHandler} or B's {@link NewProjectHandler} is a no-op </li>
     * </ul>
     */
    @Override
    public boolean shouldSchedule() {
        for (Job job : Job.getJobManager().find(CorePlugin.GRADLE_JOB_FAMILY)) {
            if (job instanceof SynchronizeGradleBuildsJob && isCoveredBy((SynchronizeGradleBuildsJob) job)) {
                return false;
            }
        }
        return true;
    }

    private boolean isCoveredBy(SynchronizeGradleBuildsJob other) {
        return Objects.equal(this.builds, other.builds)
            && (this.newProjectHandler == NewProjectHandler.NO_OP || Objects.equal(this.newProjectHandler, other.newProjectHandler));
    }

}
