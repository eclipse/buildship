/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.operation;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnector;

import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.internal.CorePlugin;

/**
 * Job that belongs to the Gradle job family.
 *
 * @param <T> the result type of the operation the job executes
 */
public abstract class ToolingApiJob<T> extends Job {

    // TODO (donat) rename package to org.eclipse.buildship.core.operation

    private final CancellationTokenSource tokenSource = GradleConnector.newCancellationTokenSource();

    private ToolingApiJobResultHandler<T> resultHandler = new DefaultResultHandler<T>();

    public ToolingApiJob(String name) {
        super(name);
    }

    public void setResultHandler(ToolingApiJobResultHandler<T> resultHandler) {
        this.resultHandler = Preconditions.checkNotNull(resultHandler);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

        ToolingApiOperation operation = new BaseToolingApiOperation(getName()) {

            @Override
            public void runInToolingApi(CancellationTokenSource tokenSource, IProgressMonitor monitor) throws Exception {
                T result = ToolingApiJob.this.runInToolingApi(tokenSource, monitor);
                ToolingApiJob.this.resultHandler.onSuccess(result);
            }

            @Override
            public ISchedulingRule getRule() {
                return ToolingApiJob.this.getRule();
            }
        };

        try {
            CorePlugin.operationManager().run(operation, this.tokenSource, monitor);
        } catch (CoreException e) {
            IStatus status = e.getStatus();
            if (status instanceof ToolingApiStatus) {
                this.resultHandler.onFailure((ToolingApiStatus) status);
            } else {
                return status;
            }
        }

        return Status.OK_STATUS;
    }

    /**
     * Method to be executed in {@link Job#run(IProgressMonitor)}.
     *
     * @param tokenSource the Tooling API cancellation token source
     * @param monitor the monitor to report progress on
     * @return the job result passed to the {@link ToolingApiJobResultHandler}.
     * @throws Exception if any error occurs
     */
    public abstract T runInToolingApi(CancellationTokenSource tokenSource, IProgressMonitor monitor) throws Exception;

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
    private static final class DefaultResultHandler<T> implements ToolingApiJobResultHandler<T> {

        public DefaultResultHandler() {
        }

        @Override
        public void onSuccess(T result) {
            // do nothing
        }

        @Override
        public void onFailure(ToolingApiStatus status) {
            status.log();
        }
    }
}
