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

package org.eclipse.buildship.core.util.progress;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnector;

import org.eclipse.core.runtime.jobs.Job;

/**
 * Base class for cancellable jobs that invoke the Gradle Tooling API.
 */
public abstract class ToolingApiJob extends Job {

    private final CancellationTokenSource tokenSource;
    private final String workName;

    /**
     * Creates a new job with the specified name. The job name is a human-readable value that is
     * displayed to users. The name does not need to be unique, but it must not be {@code null}. A
     * token for Gradle build cancellation is created.
     *
     * @param name the name of the job
     */
    protected ToolingApiJob(String name) {
        super(name);
        this.tokenSource = GradleConnector.newCancellationTokenSource();
        this.workName = name;
    }

    protected CancellationToken getToken() {
        return this.tokenSource.token();
    }

    @Override
    public final IStatus run(final IProgressMonitor monitor) {
        ToolingApiInvoker invoker = new ToolingApiInvoker(this.workName);
        return invoker.invoke(new ToolingApiCommand() {
            @Override
            public void run() throws Exception {
                runToolingApiJob(monitor);
            }
        }, monitor);
    }

    /**
     * Template method that executes the actual Tooling API-related work.
     * <p/>
     * If an exception is thrown in this method, the exception is reported via the
     * {@link org.eclipse.buildship.core.notification.UserNotification} service. The
     * notification content and its severity depend on the type of the thrown exception.
     * <p/>
     * If no exception is thrown in the template method, the job's return status is
     * {@link org.eclipse.core.runtime.Status#OK_STATUS}. If an exception occurs, a non-error, non-ok status
     * is returned. This disables the platform UI to show the built-in (and rather basic)
     * exception dialog.
     *
     * @param monitor the monitor to report the progress
     * @throws Exception thrown when an error happens during the execution
     */
    protected abstract void runToolingApiJob(IProgressMonitor monitor) throws Exception;

    @Override
    protected void canceling() {
        this.tokenSource.cancel();
    }

}
