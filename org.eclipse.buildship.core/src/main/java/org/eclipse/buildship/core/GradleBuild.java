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
 * Represents a Gradle build.
 *
 * @author Donat Csikos
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface GradleBuild {

    /**
     * Synchronizes the workspace with this Gradle build.
     *
     * <p>
     * The method loads the Gradle build configuration and updates the workspace based on the
     * retrieved information.The algorithm is as follows:
     * <ul>
     *   <li>Synchronize all Gradle projects of the Gradle build with the Eclipse workspace
     *       project counterparts. If there are no projects in the workspace at the location then a
     *       new project is created. Then, based on the workspace project state, the synchronization
     *       is as follows:
     *     <ul>
     *       <li>If the workspace project is closed, the project is left unchanged.</li>
     *       <li>If the workspace project is open, the project configuration (name, source
     *           directories, dependencies, etc.) is updated.</li>
     *     </ul>
     *   </li>
     *   <li>Uncouple all open workspace projects for which there is no corresponding Gradle project
     *       in the Gradle build anymore. This includes removing the Gradle project natures and the
     *       corresponding settings file.</li>
     * </ul>
     *
     * <p>
     * This is a long-running operation which blocks the current thread until completion. Progress
     * and cancellation are provided via the monitor. Also, since the synchronization might modify
     * more than one project, the workspace root scheduling rule is acquired for the current thread
     * internally.
     * <p>
     *
     * The result of the synchronization - let it be a success or a failure - is described by the
     * returned {@link SynchronizationResult} instance.
     *
     * @param monitor the monitor to report progress on, or {@code null} if progress reporting is not desired
     * @return the synchronization result
     */
    SynchronizationResult synchronize(IProgressMonitor monitor);
}
