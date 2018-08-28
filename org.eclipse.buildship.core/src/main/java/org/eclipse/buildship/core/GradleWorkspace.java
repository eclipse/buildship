/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

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
     *
     * @param project the target project
     * @return the Gradle build
     * @throws NullPointerException if project is null
     * @throws IllegalArgumentException if the project is closed or not a Gradle project
     */
    GradleBuild getBuild(IProject project);
}
