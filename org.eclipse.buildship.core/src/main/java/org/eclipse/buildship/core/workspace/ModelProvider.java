/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace;

import java.util.Set;

import org.gradle.tooling.CancellationToken;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Provides models in the scope of the build.
 *
 * @author Stefan Oehme
 */
//TODO we should refactor this and tooling-commons to allow arbitrary models instead of a fixed set
public interface ModelProvider {

    /**
     * Fetches the {@link OmniBuildEnvironment}.
     *
     * @param fetchStrategy the caching strategy
     * @param token the cancellation token or null if cancellation is not required
     * @param monitor the monitor to report progress on or null if progress reporting is not required
     * @return the model or null if caching was disabled and no value was cached
     */
    OmniBuildEnvironment fetchBuildEnvironment(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor);

    /**
     * Fetches the {@link OmniGradleBuild}.
     *
     * @param fetchStrategy the caching strategy
     * @param token the cancellation token or null if cancellation is not required
     * @param monitor the monitor to report progress on or null if progress reporting is not required
     * @return the model or null if caching was disabled and no value was cached
     */
    OmniGradleBuild fetchGradleBuild(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor);

    /**
     * Fetches the {@link OmniGradleProject} models for all projects in the build.
     *
     * @param fetchStrategy the caching strategy
     * @param token the cancellation token or null if cancellation is not required
     * @param monitor the monitor to report progress on or null if progress reporting is not required
     * @return the models or null if caching was disabled and no value was cached
     */
    Set<OmniGradleProject> fetchGradleProjects(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor);

    /**
     * Fetches the {@link OmniEclipseProject} models for all projects in the build.
     *
     * @param fetchStrategy the caching strategy
     * @param token the cancellation token or null if cancellation is not required
     * @param monitor the monitor to report progress on or null if progress reporting is not required
     * @return the models or null if caching was disabled and no value was cached
     */
    Set<OmniEclipseProject> fetchEclipseGradleProjects(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor);
}
