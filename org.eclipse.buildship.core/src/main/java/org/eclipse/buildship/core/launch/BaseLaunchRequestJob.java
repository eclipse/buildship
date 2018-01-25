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

package org.eclipse.buildship.core.launch;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.eclipse.buildship.core.util.gradle.TransientRequestAttributes;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.RunConfiguration;
import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.launch.internal.BuildExecutionParticipants;
import org.eclipse.buildship.core.launch.internal.DefaultExecuteLaunchRequestEvent;
import org.eclipse.buildship.core.operation.ToolingApiJob;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.workspace.GradleBuild;

/**
 * Base class to execute Gradle builds in a job.
 *
 * @param <T> the operation type the subclasses can create and execute
 */
public abstract class BaseLaunchRequestJob<T extends LongRunningOperation> extends ToolingApiJob<Void> {

    protected BaseLaunchRequestJob(String name) {
        super(name);
    }

    @Override
    public Void runInToolingApi(CancellationTokenSource tokenSource, IProgressMonitor monitor) throws Exception {
        executeLaunch(tokenSource, monitor);
        return null;
    }

    protected final void executeLaunch(CancellationTokenSource tokenSource, final IProgressMonitor monitor) throws Exception {
        // todo (etst) close streams when done

        BuildExecutionParticipants.activateParticipantPlugins();
        monitor.beginTask(getJobTaskName(), IProgressMonitor.UNKNOWN);

        ProcessDescription processDescription = createProcessDescription();
        ProcessStreams processStreams = CorePlugin.processStreamsProvider().createProcessStreams(processDescription);

        RunConfiguration runConfig = getRunConfig();
        List<ProgressListener> listeners = ImmutableList.<ProgressListener>of(DelegatingProgressListener.withFullOutput(monitor));
        TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, processStreams.getOutput(), processStreams.getError(), processStreams.getInput(),
                listeners, Collections.<org.gradle.tooling.events.ProgressListener>emptyList(), tokenSource.token());

        // TODO (donat) configWriter and TransientRequestAttributes should be merged into a new type
        OutputStreamWriter configWriter = new OutputStreamWriter(processStreams.getConfiguration());

        GradleBuild gradleBuild = CorePlugin.gradleWorkspaceManager().getGradleBuild(runConfig.getProjectConfiguration().getBuildConfiguration());
        T launcher = createLaunch(gradleBuild, runConfig, transientAttributes, configWriter, processDescription);

        writeExtraConfigInfo(configWriter);
        configWriter.write('\n');
        configWriter.flush();

        Event event = new DefaultExecuteLaunchRequestEvent(processDescription, launcher);
        CorePlugin.listenerRegistry().dispatch(event);

        executeLaunch(launcher);
    }

    /**
     * The name of the job to display in the progress view.
     *
     * @return the name of the job
     */
    protected abstract String getJobTaskName();

    /**
     * The run configuration attributes to apply when executing the request.
     *
     * @return the run configuration attributes
     */
    protected abstract RunConfiguration getRunConfig();

    /**
     * The process description.
     *
     * @return the process description
     */
    protected abstract ProcessDescription createProcessDescription();

    /**
     * Creates a new launcher object to execute in the job.
     *
     * @return the new launcher
     */
    protected abstract T createLaunch(GradleBuild gradleBuild, RunConfiguration runConfiguration, TransientRequestAttributes transientAttributes, Writer configWriter, ProcessDescription processDescription);

    /**
     * Execute the launcher created by {@code #createLaunch()}.
     *
     * @param launcher the launcher to execute
     */
    protected abstract void executeLaunch(T launcher);

    /**
     * Writes extra information on the configuration console.
     *
     * @param writer the writer to print messages with
     * @throws IOException if an exception happens when writing a message
     */
    protected abstract void writeExtraConfigInfo(OutputStreamWriter writer) throws IOException;

    /**
     * Convenience implementation of the ProcessDescription interface.
     */
    protected abstract static class BaseProcessDescription implements ProcessDescription {

        private final String name;
        private final Job job;
        private final RunConfiguration runConfig;

        protected BaseProcessDescription(String name, Job job, RunConfiguration runConfig) {
            this.name = Preconditions.checkNotNull(name);
            this.job = Preconditions.checkNotNull(job);
            this.runConfig = Preconditions.checkNotNull(runConfig);
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public Job getJob() {
            return this.job;
        }

        @Override
        public RunConfiguration getRunConfig() {
            return this.runConfig;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BaseProcessDescription) {
                BaseProcessDescription that = (BaseProcessDescription) obj;
                return Objects.equal(this.name, that.name)
                    && Objects.equal(this.job, that.job)
                    && Objects.equal(this.runConfig, that.runConfig);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.name, this.job, this.runConfig);
        }

    }
}
