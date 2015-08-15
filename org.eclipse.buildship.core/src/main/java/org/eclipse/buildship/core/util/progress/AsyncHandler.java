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
import org.gradle.tooling.CancellationToken;

/**
 * Implements Tooling API logic that is intended to be called asynchronously.
 */
public interface AsyncHandler {

    /**
     * No-op handler.
     */
    AsyncHandler NO_OP = new AsyncHandler() {
        @Override
        public void run(IProgressMonitor monitor, CancellationToken token) {
        }
    };

    /**
     * Runs the implementation logic and reports its progress on the given monitor. The
     * given cancellation token should be used when the implementation executes requests
     * to the Tooling API.
     *
     * @param monitor the monitor to report progress on
     * @param token the cancellation token to apply to any Tooling API request
     */
    void run(IProgressMonitor monitor, CancellationToken token);

}
