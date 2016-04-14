/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace;

import org.gradle.tooling.CancellationToken;

import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Provides models in the scope of the composite build.
 * @author Stefan Oehme
 */
public interface CompositeModelProvider {

    /**
     * Fetches the {@link OmniEclipseWorkspace}.
     *
     * @param fetchStrategy the caching strategy
     * @param token the cancellation token or null if cancellation is not required
     * @param monitor the monitor to report progress on or null if progress reporting is not required
     * @return the model or null if caching was disabled and no value was cached
     */
    OmniEclipseWorkspace fetchEclipseWorkspace(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor);
}
