/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.gradle;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Preconditions;
import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.console.ProcessDescription;
import org.eclipse.buildship.core.internal.console.ProcessStreams;
import org.eclipse.buildship.core.internal.console.ProcessStreamsProvider;
import org.eclipse.buildship.core.internal.util.progress.CancellationForwardingListener;
import org.eclipse.buildship.core.internal.util.progress.DelegatingProgressListener;

/**
 * Holds attributes that are commonly used to handle progress in each Gradle invocation.
 *
 * @author Donat Csikos
 */
public final class GradleProgressAttributes {

    private final ProcessStreams streams;
    private final CancellationToken cancellationToken;
    private final ImmutableList<ProgressListener> progressListeners;
    private final ImmutableList<org.gradle.tooling.events.ProgressListener> progressEventListeners;

    private GradleProgressAttributes(ProcessStreams streams, CancellationToken cancellationToken, List<ProgressListener> progressListeners,
            List<org.gradle.tooling.events.ProgressListener> progressEventListeners) {
        this.streams = Preconditions.checkNotNull(streams);
        this.cancellationToken = Preconditions.checkNotNull(cancellationToken);
        this.progressListeners = ImmutableList.copyOf(progressListeners);
        this.progressEventListeners = ImmutableList.copyOf(progressEventListeners);
    }

    /**
     * Sets the attributes on the target operation.
     *
     * @param operation the operation to configure
     */
    public void applyTo(LongRunningOperation operation) {
        operation.setStandardOutput(this.streams.getOutput());
        operation.setStandardError(this.streams.getError());
        operation.setStandardInput(this.streams.getInput());
        for (ProgressListener listener : this.progressListeners) {
            operation.addProgressListener(listener);
        }
        for (org.gradle.tooling.events.ProgressListener listener : this.progressEventListeners) {
            operation.addProgressListener(listener);
        }
        operation.withCancellationToken(this.cancellationToken);
    }

    /**
     * Displays the argument in the configuration stream.
     *
     * @param line the string to display
     * @see ProcessStreams#getConfiguration()
     */
    public void writeConfig(String line) {
        try {
            OutputStream configStream = this.streams.getConfiguration();
            configStream.write(line.getBytes());
            configStream.write(StandardSystemProperty.LINE_SEPARATOR.value().getBytes());
        } catch (IOException e) {
            CorePlugin.logger().debug(String.format("Failed to write configuration %s to stream", line), e);
        }
    }

    /**
     * Closes all input and output streams.
     */
    public void close() {
        this.streams.close();
    }

    public static final GradleProgressAttributesBuilder builder(CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        return new GradleProgressAttributesBuilder(tokenSource, monitor);
    }

    /**
     * Builds {@link GradleProgressAttributes} instances.
     *
     */
    public static class GradleProgressAttributesBuilder {

        private final CancellationTokenSource tokenSource;
        private final IProgressMonitor monitor;

        private ProcessDescription processDescription = null;
        private ProgressListener delegatingListener = null;

        public GradleProgressAttributesBuilder(CancellationTokenSource tokenSource, IProgressMonitor monitor) {
            this.tokenSource = tokenSource;
            this.monitor = monitor;
        }

        public GradleProgressAttributesBuilder forBackgroundProcess() {
            this.processDescription = null;
            return this;
        }

        public GradleProgressAttributesBuilder forDedicatedProcess(ProcessDescription processDescription) {
            this.processDescription = processDescription;
            return this;
        }

        public GradleProgressAttributesBuilder withFullProgress() {
            this.delegatingListener = DelegatingProgressListener.withFullOutput(this.monitor);
            return this;
        }

        public GradleProgressAttributesBuilder withFilteredProgress() {
            this.delegatingListener = DelegatingProgressListener.withoutDuplicateLifecycleEvents(this.monitor);
            return this;
        }

        public GradleProgressAttributes build() {
            ProcessStreamsProvider streamsProvider = CorePlugin.processStreamsProvider();
            ProcessStreams streams = (this.processDescription == null) ? streamsProvider.getBackgroundJobProcessStreams()
                    : streamsProvider.createProcessStreams(this.processDescription);

            Builder<ProgressListener> progressListeners = ImmutableList.builder();
            Builder<org.gradle.tooling.events.ProgressListener> progressEventListeners = ImmutableList.builder();
            progressListeners.add(this.delegatingListener);

            CancellationForwardingListener cancellationListener = new CancellationForwardingListener(this.monitor, this.tokenSource);
            progressListeners.add(cancellationListener);
            progressEventListeners.add(cancellationListener);

            return new GradleProgressAttributes(streams, this.tokenSource.token(), progressListeners.build(), progressEventListeners.build());
        }
    }
}
