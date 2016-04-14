/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.buildship.core.util.progress.AsyncHandler;

/*
 * TODO The methods in this class highlight some problems with our current job-centric design:
 * - callers can't decide for themselves how they want to handle errors
 * - callers cannot embed any of the operations in another operation (providing cancellation etc.)
 * - callers do not know if and when the operations finish
 *
 * This API should evolve into a set of synchronous operations that provide progress,
 * cancellation and throw CoreExceptions with detailed IStatus, which will allow
 * any problems to be displayed to the user in the most accurate fashion.
 */

/**
 * A set of {@link GradleBuild}s that are built together.
 *
 * @author Stefan Oehme
 *
 */
public interface CompositeGradleBuild {

    /**
     * Attempts to synchronize all contained builds with the workspace.
     * <p/>
     * The synchronization happens asynchronously. In case of a failure, the user will be notified
     * once after all builds have finished.
     * <p/>
     * This is equivalent to calling
     * {@code synchronize(NewProjectHandler.NO_OP)}
     *
     */
    void synchronize();

    /**
     * Attempts to synchronize all contained builds with the workspace.
     * <p/>
     * The synchronization happens asynchronously. In case of a failure, the user will be notified
     * once after all builds have finished.
     * <p/>
     * This is equivalent to calling {@code synchronize(newProjectHandler, AsyncHandler.NO_OP)}
     *
     *
     * @param newProjectHandler how to handle newly added projects
     */
    void synchronize(NewProjectHandler newProjectHandler);

    /**
     * Attempts to synchronize all contained builds with the workspace.
     * <p/>
     * The synchronization happens asynchronously. In case of a failure, the user will be notified
     * once after all builds have finished.
     *
     * @param newProjectHandler how to handle newly added projects
     * @param initializer an initializer to run before synchronization, e.g. to create a new project
     */
    void synchronize(NewProjectHandler newProjectHandler, AsyncHandler initializer);

    /**
     * Returns the model provider for this composite build.
     *
     * @return the model provider, never null
     */
    CompositeModelProvider getModelProvider();

    /**
     * Returns a new composite build containing all the builds from this one and the given build.
     * @param build the build to add to the new composite
     * @return the new composite build, never null
     */
    CompositeGradleBuild withBuild(FixedRequestAttributes build);
}
