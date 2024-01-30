/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
import org.eclipse.buildship.core.internal.CoreTraceScopes;
import org.eclipse.buildship.core.internal.console.ProcessDescription;
import org.eclipse.buildship.core.internal.console.ProcessStreams;
import org.eclipse.buildship.core.internal.console.ProcessStreamsProvider;
import org.eclipse.buildship.core.internal.util.progress.CancellationForwardingListener;
import org.eclipse.buildship.core.internal.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.internal.util.progress.ProblemsReportingProgressListener;
import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;

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
    private final boolean isInteractive;

    private GradleProgressAttributes(ProcessStreams streams, CancellationToken cancellationToken, List<ProgressListener> progressListeners,
            List<org.gradle.tooling.events.ProgressListener> progressEventListeners, boolean isInteractive) {
        this.streams = Preconditions.checkNotNull(streams);
        this.cancellationToken = Preconditions.checkNotNull(cancellationToken);
        this.progressListeners = ImmutableList.copyOf(progressListeners);
        this.progressEventListeners = ImmutableList.copyOf(progressEventListeners);
        this.isInteractive = isInteractive;
    }

    /**
     * Sets the attributes on the target operation.
     *
     * @param operation the operation to configure
     */
    public void applyTo(LongRunningOperation operation) {
        operation.setStandardOutput(this.streams.getOutput());
        operation.setStandardError(this.streams.getError());
        if (this.isInteractive) {
            operation.setStandardInput(this.streams.getInput());
        }
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
            CorePlugin.logger().trace(CoreTraceScopes.PREFERENCES, String.format("Failed to write configuration %s to stream", line), e);
        }
    }

    /**
     * Closes all input and output streams.
     */
    public void close() {
        this.streams.close();
    }

    public static final GradleProgressAttributesBuilder builder(CancellationTokenSource tokenSource, InternalGradleBuild gradleBuild, IProgressMonitor monitor) {
        return new GradleProgressAttributesBuilder(tokenSource, gradleBuild, monitor);
    }

    /**
     * Builds {@link GradleProgressAttributes} instances.
     *
     */
    public static class GradleProgressAttributesBuilder {

        private final CancellationTokenSource tokenSource;
        private final IProgressMonitor monitor;

        private ProcessDescription processDescription = null;
        private boolean isInteractive = true;
        private ProgressListener delegatingListener = null;
        private final InternalGradleBuild gradleBuild;

        public GradleProgressAttributesBuilder(CancellationTokenSource tokenSource, InternalGradleBuild gradleBuild, IProgressMonitor monitor) {
            this.tokenSource = tokenSource;
            this.gradleBuild = gradleBuild;
            this.monitor = monitor;
        }

        public GradleProgressAttributesBuilder forBackgroundProcess() {
            this.processDescription = null;
            return this;
        }

        public GradleProgressAttributesBuilder forNonInteractiveBackgroundProcess() {
            this.processDescription = null;
            this.isInteractive = false;
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
            progressEventListeners.add(new ProblemsReportingProgressListener(this.gradleBuild));

            return new GradleProgressAttributes(streams, this.tokenSource.token(), progressListeners.build(), progressEventListeners.build(), this.isInteractive);
        }
    }
}
