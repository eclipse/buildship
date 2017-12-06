/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace;

import java.util.Collection;
import java.util.Set;

import org.gradle.tooling.CancellationTokenSource;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Provides models in the scope of the build.
 *
 * @author Stefan Oehme
 */
public interface ModelProvider {

    /**
     * Synchronously queries a target model from this build.
     *
     * @param model the model to query
     * @param strategy the fetch strategy
     * @param tokenSource the cancellation token source
     * @param monitor the monitor to report the progress on
     * @return the returned model
     */
    <T> T fetchModel(Class<T> model, FetchStrategy strategy, CancellationTokenSource tokenSource, IProgressMonitor monitor);

    /**
     * Synchronously queries a target model from this build and from all included builds.
     *
     * @param model the model to query
     * @param strategy the fetch strategy
     * @param tokenSource the cancellation token source
     * @param monitor the monitor to report the progress on
     * @return the returned models
     */
    <T> Collection<T> fetchModels(Class<T> model, FetchStrategy strategy, CancellationTokenSource tokenSource, IProgressMonitor monitor);

    /**
     * Synchronously queries The {@link OmniBuildEnvironment} model from this build.
     *
     * @param strategy the fetch strategy
     * @param tokenSource the cancellation token source
     * @param monitor the monitor to report the progress on
     * @return the returned model
     */
    OmniBuildEnvironment fetchBuildEnvironment(FetchStrategy strategy, CancellationTokenSource tokenSource, IProgressMonitor monitor);

    /**
     * Synchronously queries The {@link OmniGradleBuild} model from this build.
     *
     * @param strategy the fetch strategy
     * @param tokenSource the cancellation token source
     * @param monitor the monitor to report the progress on
     * @return the returned model
     */
    OmniGradleBuild fetchGradleBuild(FetchStrategy strategy, CancellationTokenSource tokenSource, IProgressMonitor monitor);

    /**
     * Synchronously queries The {@link OmniEclipseProject} models from this build.
     *
     * @param strategy the fetch strategy
     * @param tokenSource the cancellation token source
     * @param monitor the monitor to report the progress on
     * @return the returned model
     */
    Set<OmniEclipseProject> fetchEclipseGradleProjects(FetchStrategy strategy, CancellationTokenSource tokenSource, IProgressMonitor monitor);
}