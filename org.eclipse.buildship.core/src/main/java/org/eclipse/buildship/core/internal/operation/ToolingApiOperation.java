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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Basic operation to execute synchronously in the context of the Tooling API.
 * <p/>
 * The {@link ToolingApiOperationManager} service is responsible to execute operations.
 *
 * @author Donat Csikos
 */
public interface ToolingApiOperation {

    /**
     * Returns the human-readable name of the operation that is presented on the UI.
     *
     * @return the operation name
     */
    public String getName();

    /**
     * Returns the scheduling rule that is applied when the operation is executed. Returns
     * <code>null</code> if this operation has no scheduling rule.
     *
     * @return the operation's scheduling rule
     */
    public ISchedulingRule getRule();

    /**
     * Executes the operation.
     *
     * @param tokenSource the cancellation token source to be used in the operation
     * @param monitor the monitor to report the progress on
     * @throws Exception if any exception happens during the execution
     */
    public abstract void runInToolingApi(CancellationTokenSource tokenSource, IProgressMonitor monitor) throws Exception;
}
