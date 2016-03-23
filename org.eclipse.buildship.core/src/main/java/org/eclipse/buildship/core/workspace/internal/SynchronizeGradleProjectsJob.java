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

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobGroup;

import org.eclipse.buildship.core.AggregateException;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Synchronizes each of the given Gradle Builds using {@link SynchronizeGradleProjectJob} and
 * reports problems to the user in bulk.
 */
final class SynchronizeGradleProjectsJob extends ToolingApiJob {

    private final NewProjectHandler newProjectHandler;
    private final ImmutableSet<FixedRequestAttributes> builds;
    private final JobGroup jobGroup;

    public SynchronizeGradleProjectsJob(Set<FixedRequestAttributes> builds, NewProjectHandler newProjectHandler) {
        super("Synchronize workspace projects with Gradle counterparts", true);
        this.builds = ImmutableSet.copyOf(builds);
        this.newProjectHandler = Preconditions.checkNotNull(newProjectHandler);
        this.jobGroup = new JobGroup(getName(), 0, this.builds.size());
    }

    @Override
    protected void runToolingApiJob(IProgressMonitor monitor) throws Exception {
        for (FixedRequestAttributes build : this.builds) {
            Job synchronizeJob = new SynchronizeGradleProjectJob(build, this.newProjectHandler, AsyncHandler.NO_OP);
            synchronizeJob.setJobGroup(this.jobGroup);
            synchronizeJob.schedule();
        }
        this.jobGroup.join(0, monitor);
        handleResult(this.jobGroup);
    }

    /*
     * TODO this is a poor man's version of what CoreException + MultiStatus already provide out of
     * the box We should refactor to remove this completely
     */
    private void handleResult(JobGroup group) {
        MultiStatus result = group.getResult();
        if (result != null) {
            if (result.matches(IStatus.CANCEL)) {
                throw new OperationCanceledException();
            }
            List<Throwable> errors = Lists.newArrayList();
            for (IStatus status : result.getChildren()) {
                if (!status.isOK()) {
                    Throwable error = status.getException();
                    if (error != null) {
                        errors.add(error);
                    }
                }
            }
            propagate(errors);
        }
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
    public boolean belongsTo(Object family) {
        return CorePlugin.GRADLE_JOB_FAMILY.equals(family);
    }

    @Override
    protected void canceling() {
        this.jobGroup.cancel();
    }

}
