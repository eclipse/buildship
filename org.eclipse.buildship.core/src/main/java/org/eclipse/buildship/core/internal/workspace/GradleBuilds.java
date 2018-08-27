/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import java.util.Set;

import org.gradle.tooling.CancellationTokenSource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An aggregate of Gradle builds.
 *
 * @author Donat Csikos
 */
public interface GradleBuilds extends Iterable<GradleBuild> {

    /**
     * Attempts to synchronize all contained builds with the workspace.
     * <p/>
     * If the synchronization fails on one Gradle build, the process stops and subsequent builds
     * won't be synchronized.
     * <p/>
     * The synchronization happens synchronously. In case of a failure, the method throws a
     * {@link CoreException} which contains the necessary status and error message about the
     * failure.
     *
     * @param newProjectHandler how to handle newly added projects
     * @param tokenSource the cancellation token source
     * @throws CoreException if the synchronization fails
     * @see org.eclipse.buildship.core.internal.util.progress.ToolingApiStatus
     */
    void synchronize(NewProjectHandler newProjectHandler, CancellationTokenSource tokenSource, IProgressMonitor monitor) throws CoreException;

    /**
     * Returns the contained {@link GradleBuild} instances.
     *
     * @return the contained Gradle builds
     */
    Set<GradleBuild> getGradleBuilds();
}
