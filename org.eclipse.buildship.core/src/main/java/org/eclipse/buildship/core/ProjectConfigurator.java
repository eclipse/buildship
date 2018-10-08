/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface to hook into the Gradle project synchronization.
 * <p>
 * Clients can register project configurators via the {@code projectconfigurator} extension point.
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