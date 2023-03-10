/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import java.util.Map;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.model.eclipse.EclipseProject;

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
    <T>  Map<String, T> fetchModels(Class<T> model, FetchStrategy strategy, CancellationTokenSource tokenSource, IProgressMonitor monitor);

    /**
     * Queries the {@link EclipseProject} model and executes the synchronization tasks in the same Tooling API query.
     *
     * @return the returned models
     */
    Map<String, EclipseProject> fetchEclipseProjectAndRunSyncTasks(CancellationTokenSource tokenSource, IProgressMonitor monitor);
}
