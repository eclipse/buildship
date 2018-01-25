/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

import org.eclipse.buildship.core.util.gradle.Maybe;
import org.eclipse.buildship.core.util.gradle.Path;

/**
 * Represents a project task which is executable by Gradle.
 *
 * @author Etienne Studer
 */
public interface OmniProjectTask {

    /**
     * Returns the name of this task. Note that the name is not a unique identifier for the task.
     *
     * @return the name of this task
     */
    String getName();

    /**
     * Returns the description of this task, or {@code null} if it has no description.
     *
     * @return the description of this task, or {@code null} if it has no description
     */
    String getDescription();

    /**
     * Returns the path of this task. The path can be used as a unique identifier for the task within a given build.
     *
     * @return the path of this task
     */
    Path getPath();

    /**
     * Returns whether this task is public or not. Public tasks are those that have a non-empty {@code group} property.
     *
     * @return {@code true} if this task is public, {@code false} otherwise
     */
    boolean isPublic();

    /**
     * Returns the group this task belongs to.
     *
     * @return the group this task belongs to
     */
    Maybe<String> getGroup();

}
