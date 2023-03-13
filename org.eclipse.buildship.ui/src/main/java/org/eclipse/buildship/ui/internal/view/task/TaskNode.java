/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
