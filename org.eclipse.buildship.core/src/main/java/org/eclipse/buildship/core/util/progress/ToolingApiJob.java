/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.util.progress;

import java.util.concurrent.TimeUnit;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Job that belongs to the Gradle job family.
 *
 * @param <T> the result type of the operation the job executes
 */
public abstract class ToolingApiJob<T> extends Job {

    // TODO (donat) rename package to org.eclipse.buildship.core.operation

    private final CancellationTokenSource tokenSource = GradleConnector.newCancellationTokenSource();

    public ToolingApiJob(String name) {
        super(name);
    }

    @Override
    public final IStatus run(final IProgressMonitor monitor) {
        final IProgressMonitor efficientMonitor = new RateLimitingProgressMonitor(monitor, 500, TimeUnit.MILLISECONDS);
        ToolingApiOperation<T> operation = getOperation();
        ToolingApiOperationResultHandler<T> resultHandler = getResultHandler();

        try {
            T result = operation.run(efficientMonitor);
            resultHandler.onSuccess(result);
        } catch (Exception e) {
            resultHandler.onFailure(ToolingApiStatus.from(getName(), e));
        }
        return Status.OK_STATUS;
    }

    public abstract ToolingApiOperation<T> getOperation();

    public ToolingApiOperationResultHandler<T> getResultHandler() {
        return new DefaultResultHandler<T>(getName());
    }

    protected CancellationTokenSource getTokenSource() {
        return this.tokenSource;
    }

    protected CancellationToken getToken() {
        return this.tokenSource.token();
    }

    @Override
    public boolean belongsTo(Object family) {
        return CorePlugin.GRADLE_JOB_FAMILY.equals(family);
    }

    @Override
    protected void canceling() {
        this.tokenSource.cancel();
    }

    /**
     * Default handler for the target operation.
     *
     * @param <T> the result type
     */
    private static final class DefaultResultHandler<T> implements ToolingApiOperationResultHandler<T> {

        private final String name;

        public DefaultResultHandler(String name) {
            this.name = name;
        }

        @Override
        public void onSuccess(T result) {
            // do nothing
        }

        @Override
        public void onFailure(ToolingApiStatus status) {
            // TODO (donat) do we need the IStatus implementation? Seems like an unnecessary indirection
            // maybe we can export the whole default error handling to this class
            ToolingApiStatus.handleDefault(this.name, status);
        }
    }
}
