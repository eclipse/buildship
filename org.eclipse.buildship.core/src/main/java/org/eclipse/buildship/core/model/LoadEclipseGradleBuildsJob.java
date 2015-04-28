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

package org.eclipse.buildship.core.model;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingclient.Consumer;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.ModelRepositoryProvider;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.console.ProcessStreamsProvider;
import org.eclipse.buildship.core.i18n.CoreMessages;

/**
 * Loads the {@link OmniEclipseGradleBuild} models for all given {@link org.eclipse.buildship.core.configuration.ProjectConfiguration}
 * instances. Each model is loaded in parallel in a separate job. Canceling this job will cancel all
 * these jobs.
 *
 * It is ensured that only one instance of this job can run at any given time.
 */
public final class LoadEclipseGradleBuildsJob extends Job {

    private final ModelRepositoryProvider modelRepositoryProvider;
    private final ProcessStreamsProvider processStreamsProvider;
    private final FetchStrategy modelFetchStrategy;
    private final ImmutableSet<ProjectConfiguration> configurations;
    private final Consumer<Optional<OmniEclipseGradleBuild>> postProcessor;

    public LoadEclipseGradleBuildsJob(ModelRepositoryProvider modelRepositoryProvider, ProcessStreamsProvider processStreamsProvider, FetchStrategy modelFetchStrategy,
            Set<ProjectConfiguration> configurations, Consumer<Optional<OmniEclipseGradleBuild>> postProcessor) {
        super(CoreMessages.LoadEclipseGradleBuildsJob_LoadingTasksOfAllProjects);
        this.modelRepositoryProvider = Preconditions.checkNotNull(modelRepositoryProvider);
        this.processStreamsProvider = Preconditions.checkNotNull(processStreamsProvider);
        this.modelFetchStrategy = Preconditions.checkNotNull(modelFetchStrategy);
        this.configurations = ImmutableSet.copyOf(configurations);
        this.postProcessor = Preconditions.checkNotNull(postProcessor);
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        monitor.beginTask(CoreMessages.LoadEclipseGradleBuildsJob_LoadTasksOfAllProjects, this.configurations.size());
        try {
            // prepare a job listener that is notified for each job that has
            // finished and then counts down the latch
            final CountDownLatch latch = new CountDownLatch(this.configurations.size());
            JobChangeAdapter jobListener = new JobChangeAdapter() {

                @Override
                public void done(IJobChangeEvent event) {
                    latch.countDown();
                    monitor.worked(1);
                }
            };

            // in parallel, run a job for each project configuration to load
            for (ProjectConfiguration configuration : this.configurations) {
                LoadEclipseGradleBuildJob loadProjectJob = new LoadEclipseGradleBuildJob(this.modelRepositoryProvider, this.processStreamsProvider, this.modelFetchStrategy,
                        configuration, this.postProcessor);
                loadProjectJob.addJobChangeListener(jobListener);
                loadProjectJob.schedule();
            }

            // block until all project load jobs have finished successfully or failed,
            // canceling this job will trigger the cancellation of all jobs scheduled by this job
            try {
                latch.await();
            } catch (InterruptedException e) {
                return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, CoreMessages.LoadEclipseGradleBuildsJob_ErrorMessage_LoadingTasksFailed, e);
            }

            // everything went well
            return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
        } finally {
            monitor.done();
        }
    }

    @Override
    protected void canceling() {
        // cancel all running LoadEclipseGradleBuildJob instances,
        // assuming all these 'child' jobs have been scheduled by this 'parent' job
        Job.getJobManager().cancel(LoadEclipseGradleBuildJob.class.getName());
    }

    @Override
    public boolean belongsTo(Object family) {
        return getJobFamilyName().equals(family);
    }

    @Override
    public boolean shouldRun() {
        // if another job of this type is already scheduled, then
        // we see 2 jobs by that name in the job manager
        // (the current job gets registered before shouldRun() is called)
        return Job.getJobManager().find(getJobFamilyName()).length <= 1;
    }

    private String getJobFamilyName() {
        return LoadEclipseGradleBuildsJob.class.getName();
    }

}
