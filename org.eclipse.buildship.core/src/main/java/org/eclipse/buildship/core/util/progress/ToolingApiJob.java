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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Job that belongs to the Gradle job family.
 */
public abstract class ToolingApiJob extends Job {

    // TODO (donat) rename package to org.eclipse.buildship.core.operation

    private final CancellationTokenSource tokenSource = GradleConnector.newCancellationTokenSource();

    public ToolingApiJob(String name) {
        super(name);
    }

    @Override
    public final IStatus run(final IProgressMonitor monitor) {
        final IProgressMonitor efficientMonitor = new RateLimitingProgressMonitor(monitor, 500, TimeUnit.MILLISECONDS);
        try {
            getOperation().run(efficientMonitor);
        } catch (CoreException e) {
            handleStatus(e.getStatus());
        }
        return Status.OK_STATUS;
    }

    public abstract ToolingApiOperation getOperation();

    /**
     * Callback to handle synchronization result. Clients might override this method to provide custom error handling.
     *
     * @param status the result status to handle
     * @see ToolingApiStatus
     */
    protected void handleStatus(IStatus status) {
        ToolingApiStatus.handleDefault("Project synchronization", status);
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
}
