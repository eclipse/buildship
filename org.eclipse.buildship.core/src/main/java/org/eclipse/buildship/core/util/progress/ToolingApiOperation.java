/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.util.progress;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A operation that runs inside a {@link ToolingApiJob}.
 *
 * @param <T> the result type this operation produces
 * @author Donat Csikos
 */
public abstract class ToolingApiOperation<T> {

    /**
     * Runs a Tooling API operation.
     *
     * @param monitor the monitor to report progress on
     * @throws Exception thrown if running the operation fails
     */
    public abstract T run(IProgressMonitor monitor) throws Exception;
}
