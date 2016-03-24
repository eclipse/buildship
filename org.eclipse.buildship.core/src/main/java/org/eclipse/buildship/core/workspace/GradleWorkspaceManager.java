/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace;

import java.util.Set;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.util.progress.AsyncHandler;

/*
 * TODO The methods in this class highlight some problems with our current job-centric design: -
 * callers can't decide for themselves how they want to handle errors - callers cannot embed any of
 * the operations in another operation (providing cancellation etc.) - callers do not know if and
 * when the operations finish This API should evolve into a set of synchronous operations that
 * provide progress, cancellation and throw CoreExceptions with detailed IStatus, which will allow
 * any problems to be displayed to the user in the most accurate fashion.
 */
/**
 * Manages the Gradle builds that are contained in the current Eclipse workspace.
 *
 * @author Stefan Oehme
 */
public interface GradleWorkspaceManager {

    /**
     * Attempts to synchronize the given Gradle build.
     * <p/>
     * The synchronization happens asynchronously. In case of a failure, the user will be notified.
     *
     * @param attributes the configuration of the build to synchronize
     * @param newProjectHandler how to handle newly added projects
     */
    public void synchronizeGradleBuild(FixedRequestAttributes attributes, NewProjectHandler newProjectHandler);

    /**
     * Attempts to create and import the given Gradle project.
     *
     * <p/>
     * The import happens asynchronously. In case of a failure, the user will be notified.
     *
     * @param attributes the configuration of the project to create
     * @param newProjectHandler how to handle newly added projects
     * @param initializer the initializer to run on the project before importing it
     */
    public void createGradleBuild(FixedRequestAttributes attributes, NewProjectHandler newProjectHandler, AsyncHandler initializer);

    /**
     * Attempts to synchronize the given workspace projects with their Gradle counterpart.
     * <p/>
     * Determines the Gradle builds that the given projects belong to and resynchronizes these
     * builds with the workspace. The synchronization happens asynchronously. In case of a failure,
     * the user will be notified.
     *
     * @param projects the projects to refresh
     * @param newProjectHandler how to handle newly added sub-projects
     */
    public void synchronizeProjects(Set<IProject> projects, NewProjectHandler newProjectHandler);

}
