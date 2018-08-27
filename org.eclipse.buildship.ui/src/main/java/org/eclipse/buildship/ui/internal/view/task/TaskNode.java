/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.internal.view.task;

/**
 * Describes the common features of all task nodes.
 */
public interface TaskNode {

    /**
     * Enumerates the different types of task nodes.
     */
    enum TaskNodeType {
        PROJECT_TASK_NODE, TASK_SELECTOR_NODE
    }

    /**
     * Returns the parent project node of the node.
     *
     * @return the parent project node
     */
    ProjectNode getParentProjectNode();

    /**
     * Returns the name of the node.
     *
     * @return the task name
     */
    String getName();

    /**
     * Returns the type of the node.
     *
     * @return the node type
     */
    TaskNodeType getType();

    /**
     * Returns whether the node is public.
     *
     * @return {@code true} if the node is public
     */
    boolean isPublic();

}
