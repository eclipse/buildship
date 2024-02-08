/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface to hook into Buildship's project synchronization.
 *
 * <p>
 * The primary intent of this interface is to let external plugins set up project configuration for
 * tools that are unknown by Buildship. An implementation can be registered via the
 * {@code projectconfigurators} extension point. An implementation is free to use any APIs to change
 * the project. It is recommended that configurators only add additional configuration and not
 * remove anything previously added (e.g. don't delete projects or dependencies).
 *
 * <p>
 * The synchronization makes use of the project configurators the following way. The algorithm calls
 * all configurators synchronously and sequentially. Before the synchronization starts, the
 * algorithm creates new configurator instances and calls their {@code init()} method. Then, during
 * the synchronization, the {@code configure()} methods are called at the end of each workspace
 * project configuration. If a project gets dissociated with the Gradle build, then the
 * {@code unconfigure()} method is called first, followed by the Buildship internal configuration
 * removal.
 *
 * <p>
 * The configurator ordering can be influenced via the {@code runsBefore} and {@code runsAfter}
 * attribute in the extension. If the extension doesn't define the attributes then the ordering is
 * unspecified.
 *
 * <p>
 * Clients can report errors with the {@link SynchronizationContext#error(String, Exception)} and
 * {@link SynchronizationContext#warning(String, Exception)} methods.
 *
 * @author Donat Csikos
 * @since 3.0
 */
public interface ProjectConfigurator {

    /**
     * Called once before the synchronization.
     *
     * @param context describes the build being synchronized
     * @param monitor the monitor to report progress on
     */
    void init(InitializationContext context, IProgressMonitor monitor);

    /**
     * Called once for each workspace project being synchronized.
     *
     * @param context describes the project being synchronized
     * @param monitor the monitor to report progress on
     */
    void configure(ProjectContext context, IProgressMonitor monitor);

    /**
     * Called once for each workspace project that is removed from the Gradle build.
     *
     * @param context describes the project being removed
     * @param monitor the monitor to report progress on
     */
    void unconfigure(ProjectContext context, IProgressMonitor monitor);
}
