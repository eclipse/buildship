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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Entry point to synchronously execute {@link ToolingApiOperation} instances.
 *
 * @author Donat Csikos
 */
public interface ToolingApiOperationManager {

    /**
     * Synchronously executes the target {@link ToolingApiOperation}.
     *
     * Calls {@code run(runnable, GradleConnector.newCancellationTokenSource(), monitor)}
     * internally.
     *
     * @param operation the target Tooling API operation
     * @param monitor the monitor to report progress on
     * @throws CoreException if the execution fails
     *
     * @see #run(ToolingApiOperation, CancellationTokenSource, IProgressMonitor)
     */
    void run(ToolingApiOperation operation, IProgressMonitor monitor) throws CoreException;

    /**
     * Synchronously executes the target {@code ToolingApiOperation}.
     * <p/>
     * The operation is executed as as {@code IWorkspaceRunnable} to avoid workbench concurrency
     * conflicts.
     * <p/>
     * If the target operation fails then the exception is caught internally and a new
     * {@code CoreException} is thrown containing a custom
     * {@link org.eclipse.buildship.core.internal.operation.ToolingApiStatus}.
     *
     * @param operation the target Tooling API operation
     * @param tokenSource the cancellation token source to be used in the operation
     * @param monitor the raw monitor the operation will report its progress on
     * @throws CoreException if the operation fails
     */
    void run(ToolingApiOperation operation, CancellationTokenSource tokenSource, IProgressMonitor monitor) throws CoreException;
}
