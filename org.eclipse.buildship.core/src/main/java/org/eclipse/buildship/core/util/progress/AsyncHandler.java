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

/**
 * Implements logic that is intended to be called asynchronously.
 */
public interface AsyncHandler {

    /**
     * No-op handler.
     */
    AsyncHandler NO_OP = new AsyncHandler() {
        @Override
        public void run(IProgressMonitor monitor) {
        }
    };

    /**
     * Runs the implementation logic and reports its progress on the given monitor.
     *
     * @param monitor the monitor to report progress on
     */
    void run(IProgressMonitor monitor);

}
