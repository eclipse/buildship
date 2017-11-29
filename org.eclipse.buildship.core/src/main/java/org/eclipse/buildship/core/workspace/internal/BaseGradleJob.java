/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.util.concurrent.TimeUnit;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnector;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.buildship.core.util.progress.GradleJob;
import org.eclipse.buildship.core.util.progress.RateLimitingProgressMonitor;
import org.eclipse.buildship.core.util.progress.ToolingApiStatus;

/**
 * Temporary replacement class for the
 * {@link org.eclipse.buildship.core.util.progress.ToolingApiJob} class.
 *
 * @author Donat Csikos
 */
public abstract class BaseGradleJob extends GradleJob {

    private final CancellationTokenSource tokenSource;
    private final String workName;

    protected BaseGradleJob(String name) {
        super(name);
        this.tokenSource = GradleConnector.newCancellationTokenSource();
        this.workName = name;
    }

    protected CancellationToken getToken() {
        return this.tokenSource.token();
    }

    @Override
    public final IStatus run(final IProgressMonitor monitor) {

        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                try {
                    runInJob(monitor);
                } catch (Exception e) {
                    throw new CoreException(ToolingApiStatus.from(BaseGradleJob.this.workName, e));
                }
            }
        };

        final IProgressMonitor efficientMonitor = new RateLimitingProgressMonitor(monitor, 500, TimeUnit.MILLISECONDS);

        try {
            ResourcesPlugin.getWorkspace().run(runnable, efficientMonitor);
            return Status.OK_STATUS;
        } catch (CoreException e) {
            return e.getStatus();
        }
    }

    protected abstract void runInJob(IProgressMonitor monitor) throws Exception;

    @Override
    protected void canceling() {
        this.tokenSource.cancel();
    }
}
