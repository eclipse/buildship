/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.operation;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.CancellationTokenSource;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

/**
 * Utility class to create {@link ToolingApiOperation} instances.
 *
 * @author Donat Csikos
 */
public final class ToolingApiOperations {

    private ToolingApiOperations() {
    }

    /**
     * Combines two operations into a composite that executes the elements sequentially.
     * <p/>
     * If the first operation is cancelled then the second operation is not executed.
     *
     * @param op1 the operation to execute first
     * @param op2 the operation to execute last
     * @return the composite operation
     */
    public static ToolingApiOperation concat(final ToolingApiOperation op1, final ToolingApiOperation op2) {

        return new ToolingApiOperation() {

            @Override
            public String getName() {
                return op1.getName() + ", " + op2.getName();
            }

            @Override
            public void runInToolingApi(CancellationTokenSource tokenSource, IProgressMonitor monitor) throws Exception {
                SubMonitor progress = SubMonitor.convert(monitor);
                progress.setWorkRemaining(2);
                CancellationToken token = tokenSource.token();

                op1.runInToolingApi(tokenSource, progress.newChild(1));
                if (monitor.isCanceled() && token.isCancellationRequested()) {
                    throw new OperationCanceledException();
                }

                op2.runInToolingApi(tokenSource, progress.newChild(1));
            }

            @Override
            public ISchedulingRule getRule() {
                return new MultiRule(new ISchedulingRule[]{ op1.getRule(), op2.getRule() });
            }
        };
    }
}
