/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.util.progress;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A operation that runs inside a {@link ToolingApiJob}.
 *
 * @author Donat Csikos
 */
public interface ToolingApiOperation {

    /**
     * Runs a Tooling API operation.
     *
     * @param monitor the monitor to report progress on
     * @throws Exception thrown if running the operation fails
     */
    void run(IProgressMonitor monitor) throws CoreException;

}
