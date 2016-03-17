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

package org.eclipse.buildship.core.gradle;

import java.util.List;

import org.gradle.tooling.ProgressListener;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.ModelRepositoryProvider;
import com.gradleware.tooling.toolingmodel.repository.SimpleModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.console.ProcessStreamsProvider;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;

/**
 * Loads the {@link OmniEclipseGradleBuild} model for a given {@link org.eclipse.buildship.core.configuration.ProjectConfiguration} instance.
 */
public final class LoadEclipseGradleBuildJob extends ToolingApiJob {

    private final ModelRepositoryProvider modelRepositoryProvider;
    private final ProcessStreamsProvider processStreamsProvider;
    private final FetchStrategy modelFetchStrategy;
    private final ProjectConfiguration configuration;

    private OmniEclipseGradleBuild result;

    public LoadEclipseGradleBuildJob(ModelRepositoryProvider modelRepositoryProvider, ProcessStreamsProvider processStreamsProvider, FetchStrategy modelFetchStrategy,
                                     ProjectConfiguration configuration, final FutureCallback<OmniEclipseGradleBuild> resultHandler) {
        super(String.format("Loading tasks of project located at %s", configuration.getRequestAttributes().getProjectDir().getName()));

        this.modelRepositoryProvider = Preconditions.checkNotNull(modelRepositoryProvider);
        this.processStreamsProvider = Preconditions.checkNotNull(processStreamsProvider);
        this.modelFetchStrategy = Preconditions.checkNotNull(modelFetchStrategy);
        this.configuration = Preconditions.checkNotNull(configuration);

        this.result = null;

        addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                if (event.getResult().isOK()) {
                    resultHandler.onSuccess(LoadEclipseGradleBuildJob.this.result);
                } else {
                    resultHandler.onFailure(event.getResult().getException());
                }
            }
        });
    }

    @Override
    protected void runToolingApiJob(IProgressMonitor monitor) throws Exception {
        monitor.beginTask(String.format("Load tasks of project located at %s", this.configuration.getRequestAttributes().getProjectDir().getName()), 1);
        this.result = fetchEclipseGradleBuild(new SubProgressMonitor(monitor, 1));
    }

    private OmniEclipseGradleBuild fetchEclipseGradleBuild(IProgressMonitor monitor) {
        monitor.beginTask("Load Eclipse Project", IProgressMonitor.UNKNOWN);
        try {
            ProcessStreams stream = this.processStreamsProvider.getBackgroundJobProcessStreams();
            List<ProgressListener> listeners = ImmutableList.<ProgressListener>of(DelegatingProgressListener.withoutDuplicateLifecycleEvents(monitor));
            TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, stream.getOutput(), stream.getError(), null, listeners,
                    ImmutableList.<org.gradle.tooling.events.ProgressListener>of(), getToken());
            SimpleModelRepository repository = this.modelRepositoryProvider.getModelRepository(this.configuration.getRequestAttributes());
            return repository.fetchEclipseGradleBuild(transientAttributes, this.modelFetchStrategy);
        } finally {
            monitor.done();
        }
    }

    @Override
    public boolean belongsTo(Object family) {
        // associate with a family so we can cancel all builds of
        // this type at once through the Eclipse progress manager
        return LoadEclipseGradleBuildJob.class.getName().equals(family);
    }

}
