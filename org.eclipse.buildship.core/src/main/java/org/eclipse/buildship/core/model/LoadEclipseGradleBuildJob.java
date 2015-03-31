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

import java.util.List;

import org.gradle.tooling.BuildCancelledException;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.TestProgressListener;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingclient.Consumer;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.ModelRepositoryProvider;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;
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
    private final Consumer<Optional<OmniEclipseGradleBuild>> postProcessor;

    public LoadEclipseGradleBuildJob(ModelRepositoryProvider modelRepositoryProvider, ProcessStreamsProvider processStreamsProvider, FetchStrategy modelFetchStrategy,
            ProjectConfiguration configuration, Consumer<Optional<OmniEclipseGradleBuild>> postProcessor) {
        super(String.format("Loading tasks of project located at %s", configuration.getProjectDir().getAbsolutePath()));
        this.modelRepositoryProvider = Preconditions.checkNotNull(modelRepositoryProvider);
        this.processStreamsProvider = Preconditions.checkNotNull(processStreamsProvider);
        this.modelFetchStrategy = Preconditions.checkNotNull(modelFetchStrategy);
        this.configuration = Preconditions.checkNotNull(configuration);
        this.postProcessor = Preconditions.checkNotNull(postProcessor);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
            // load model
            try {
                OmniEclipseGradleBuild eclipseGradleBuild = loadTasksOfEclipseProject(monitor);
                this.postProcessor.accept(Optional.of(eclipseGradleBuild));
                return Status.OK_STATUS;
            } catch (BuildCancelledException e) {
                // if the job was cancelled by the user, do not show an error dialog
                CorePlugin.logger().info(e.getMessage());
                this.postProcessor.accept(Optional.<OmniEclipseGradleBuild> absent());
                return Status.CANCEL_STATUS;
            } catch (Exception e) {
                this.postProcessor.accept(Optional.<OmniEclipseGradleBuild> absent());
                return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
                        String.format("Loading the tasks of the project located at %s failed.", this.configuration.getProjectDir().getName()), e);
            }
        } finally {
            monitor.done();
        }
    }

    public OmniEclipseGradleBuild loadTasksOfEclipseProject(IProgressMonitor monitor) {
        monitor.beginTask(String.format("Load tasks of project located at %s", this.configuration.getProjectDir().getName()), 1);
        return fetchEclipseGradleBuild(new SubProgressMonitor(monitor, 1));
    }

    private OmniEclipseGradleBuild fetchEclipseGradleBuild(IProgressMonitor monitor) {
        monitor.beginTask("Load Eclipse Project", IProgressMonitor.UNKNOWN);
        try {
            ProcessStreams stream = this.processStreamsProvider.getBackgroundJobProcessStreams();
            List<ProgressListener> listeners = ImmutableList.<ProgressListener> of(new DelegatingProgressListener(monitor));
            TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, stream.getOutput(), stream.getError(), null, listeners, ImmutableList.<TestProgressListener> of(), getToken());
            ModelRepository repository = this.modelRepositoryProvider.getModelRepository(this.configuration.getRequestAttributes());
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
