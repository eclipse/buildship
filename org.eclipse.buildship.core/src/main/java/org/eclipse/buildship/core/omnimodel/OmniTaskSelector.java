/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

import java.util.SortedSet;

import org.eclipse.buildship.core.util.gradle.Maybe;
import org.eclipse.buildship.core.util.gradle.Path;

/**
 * Represents a task selector which is executable by Gradle. A task selector requests to execute all project tasks with a given name in the context of some project and all its
 * subprojects.
 *
 * @author Etienne Studer
 */
public interface OmniTaskSelector {

    /**
     * Returns the name of this task selector. All project tasks selected by this task selector have the same name as this task selector.
     *
     * @return the name of this task selector
     */
    String getName();

    /**
     * Returns the description of this task selector, or {@code null} if it has no description.
     *
     * @return the description of this task selector, or {@code null} if it has no description
     */
    String getDescription();

    /**
     * Returns the path of the project and all its subprojects in which the task selector operates.
     *
     * @return the path of the project in which this task selector operates
     */
    Path getProjectPath();

    /**
     * Returns whether this task selector is public or not. Public task selectors are those that select at least one public project task.
     *
     * @return {@code true} if this task selector is public, {@code false} otherwise
     */
    boolean isPublic();

    /**
     * Returns the group that the tasks selected by this selector belong to.
     *
     * @return the group or {@link Maybe#absent()} if grouping is not supported by the supplying Gradle version.
     */
    Maybe<String> getGroup();

    /**
     * Returns the tasks selected by this task selector, identified by their unique path.
     *
     * @return the selected tasks, identified by their unique path
     */
    SortedSet<Path> getSelectedTaskPaths();

}
