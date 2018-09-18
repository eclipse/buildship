/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

import java.util.Optional;

import org.eclipse.core.resources.IProject;

/**
 * Manages the Gradle builds contained in the current Eclipse workspace.
 *
 * @author Donat Csikos
 * @since 3.0
 * @noimplement this interface is not intended to be implemented by clients
 */
public interface GradleWorkspace {

    /**
     * Returns a reference to a Gradle build containing the target project.
     * <p>
     * If the target project is not accessible or not part of a Gradle build then the method returns
     * {@code Optional#empty()}.
     *
     * @param project the target project
     * @return the Gradle build or {@code Optional#empty()} on {@code null} or non-Gradle projects.
     */
    Optional<GradleBuild> getBuild(IProject project);

    /**
     * Creates a new build with the target configuration. When synchronized, the build configuration
     * will be associated with all workspace projects from the build.
     *
     * @param configuration the build configuration
     * @return the created Gradle build
     */
    GradleBuild createBuild(BuildConfiguration configuration);
}
