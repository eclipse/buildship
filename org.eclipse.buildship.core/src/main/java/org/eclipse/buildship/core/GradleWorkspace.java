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
 */
public interface GradleWorkspace {

    /**
     * Returns a reference to a Gradle build containing the target project.
     * <p>
     * If the target project is not accessible or not part of a Gradle build then the method returns
     * {@code Optional#empty()}.
     *
     * @param project the target project
     * @return the Gradle build or {@code Optional#empty()} for non-Gradle projects.
     * @throws NullPointerException if project is null
     */
    Optional<GradleBuild> getBuild(IProject project);
}
