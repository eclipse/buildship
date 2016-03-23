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
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobGroup;

import org.eclipse.buildship.core.AggregateException;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.util.progress.ToolingApiCommand;
import org.eclipse.buildship.core.util.progress.ToolingApiInvoker;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Finds the Gradle root projects for the given set of Eclipse projects and then synchronizes
 * each Gradle root project with the Eclipse workspace via {@link SynchronizeGradleProjectJob}.
 */
final class SynchronizeGradleProjectsJob extends Job {

    private final Set<IProject> projects;
    private final NewProjectHandler newProjectHandler;

    public SynchronizeGradleProjectsJob(Set<IProject> projects, NewProjectHandler newProjectHandler) {
        super("Synchronize workspace projects with Gradle counterparts");
        this.projects = ImmutableSet.copyOf(projects);
        this.newProjectHandler = Preconditions.checkNotNull(newProjectHandler);
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        ToolingApiInvoker invoker = new ToolingApiInvoker(getName(), true);
        return invoker.invoke(new ToolingApiCommand() {
            @Override
            public void run() throws Throwable {
                scheduleSynchronizeJobs(monitor);
            }
        }, monitor);
    }

    private void scheduleSynchronizeJobs(final IProgressMonitor monitor) throws Throwable {
        // find all the unique root projects for the given list of projects and
        // reload the workspace project configuration for each of them (incl. their respective child projects)
        Set<FixedRequestAttributes> rootRequestAttributes = getUniqueRootAttributes(this.projects);
        JobGroup group = new JobGroup(getName(), 0, rootRequestAttributes.size());
        for (FixedRequestAttributes requestAttributes : rootRequestAttributes) {
            Job synchronizeJob = new SynchronizeGradleProjectJob(requestAttributes, this.newProjectHandler, AsyncHandler.NO_OP);
            synchronizeJob.setJobGroup(group);
            synchronizeJob.schedule();
        }
        group.join(0, monitor);
        handleAggregateResult(group);
    }

    private void handleAggregateResult(JobGroup group) throws Throwable {
        if (group.getResult() != null) {
            final List<Throwable> errors = new CopyOnWriteArrayList<Throwable>();
            for (IStatus status : group.getResult().getChildren()) {
                if (!status.isOK()) {
                    Throwable error = status.getException();
                    if (error != null) {
                        errors.add(error);
                    }
                }
            }
            rethrowExceptionsIfAny(errors);
        }
    }

    private Set<FixedRequestAttributes> getUniqueRootAttributes(Set<IProject> projects) {
        return FluentIterable.from(projects).filter(GradleProjectNature.isPresentOn()).transform(new Function<IProject, FixedRequestAttributes>() {

            @Override
            public FixedRequestAttributes apply(IProject project) {
                return CorePlugin.projectConfigurationManager().readProjectConfiguration(project).getRequestAttributes();
            }
        }).toSet();
    }

    private void rethrowExceptionsIfAny(List<Throwable> errors) throws Throwable {
        if (errors.size() == 1) {
            throw errors.get(0);
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
        // cancel all running SynchronizeGradleProjectJob instances
        Job.getJobManager().cancel(SynchronizeGradleProjectJob.class.getName());
    }

}
