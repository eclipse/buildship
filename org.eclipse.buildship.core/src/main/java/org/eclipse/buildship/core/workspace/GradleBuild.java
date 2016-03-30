/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace;

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
//TODO this should eventually also contain the methods to launch tasks etc.
//TODO for composite builds, we'll want to have a dedicated interface and then slowly move methods over there
/**
 * A Gradle build.
 *
 * @author Stefan Oehme
 */
public interface GradleBuild {

    /**
     * Attempts to create and import this build into the workspace.
     *
     * <p/>
     * The import happens asynchronously. In case of a failure, the user will be notified.
     *
     * @param newProjectHandler how to handle newly added projects
     * @param initializer the initializer to run on the project before importing it
     */
    void create(NewProjectHandler newProjectHandler, AsyncHandler initializer);

    /**
     * Attempts to synchronize this build with the workspace.
     * <p/>
     * The synchronization happens asynchronously. In case of a failure, the user will be notified.
     *
     * @param newProjectHandler how to handle newly added projects
     */
    void synchronize(NewProjectHandler newProjectHandler);

    /**
     * Returns the {@link ModelProvider} for this build.
     *
     * @return the model provider, never null
     */
    ModelProvider getModelProvider();
}
