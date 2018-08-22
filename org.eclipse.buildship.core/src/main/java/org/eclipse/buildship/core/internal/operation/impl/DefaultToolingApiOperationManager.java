/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.operation.impl;

import java.util.concurrent.TimeUnit;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnector;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.internal.operation.ToolingApiOperation;
import org.eclipse.buildship.core.internal.operation.ToolingApiOperationManager;
import org.eclipse.buildship.core.internal.operation.ToolingApiStatus;
import org.eclipse.buildship.core.internal.util.progress.RateLimitingProgressMonitor;

/**
 * Default {@link ToolingApiOperationManager} implementation.
 *
 * @author Donat Csikos
 */
public final class DefaultToolingApiOperationManager implements ToolingApiOperationManager {

    @Override
    public void run(ToolingApiOperation runnable, IProgressMonitor monitor) throws CoreException {
        run(runnable, GradleConnector.newCancellationTokenSource(), monitor);
    }

    @Override
    public void run(ToolingApiOperation runnable, CancellationTokenSource tokenSource, IProgressMonitor monitor) throws CoreException {
        IProgressMonitor efficientMonitor = new RateLimitingProgressMonitor(monitor, 500, TimeUnit.MILLISECONDS);
        ResourcesPlugin.getWorkspace().run(new WorkspaceRunnableAdapter(runnable, tokenSource), runnable.getRule(), 0, efficientMonitor);
    }

    /**
     * Adapts the target operation into a {@code IWorkspaceRunnable}.
     */
    private static class WorkspaceRunnableAdapter implements IWorkspaceRunnable {

        private final ToolingApiOperation runnable;
        private final CancellationTokenSource tokenSource;

        private WorkspaceRunnableAdapter(ToolingApiOperation runnable, CancellationTokenSource tokenSource) {
            this.runnable = runnable;
            this.tokenSource = tokenSource;
        }

        @Override
        public void run(IProgressMonitor monitor) throws CoreException {
            try {
                this.runnable.runInToolingApi(this.tokenSource, monitor);
            } catch (Exception e) {
                throw new CoreException(ToolingApiStatus.from(this.runnable.getName(), e));
            }
        }
    }
}
