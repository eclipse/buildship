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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Tree node in the {@link TaskView} representing a task selector.
 */
public final class TaskSelectorNode implements TaskNode {

    private final ProjectNode parentProjectNode;
    private final TaskSelector taskSelector;

    public TaskSelectorNode(ProjectNode parentProjectNode, TaskSelector taskSelector) {
        this.parentProjectNode = Preconditions.checkNotNull(parentProjectNode);
        this.taskSelector = Preconditions.checkNotNull(taskSelector);
    }

    @Override
    public ProjectNode getParentProjectNode() {
        return this.parentProjectNode;
    }

    @Override
    public String getName() {
        return this.taskSelector.getName();
    }

    public String getDescription() {
        return this.taskSelector.getDescription();
    }

    @Override
    public TaskNodeType getType() {
        return TaskNodeType.TASK_SELECTOR_NODE;
    }

    @Override
    public boolean isPublic() {
        return this.taskSelector.isPublic();
    }

    @Override
    public String toString() {
        return this.taskSelector.getName();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        TaskSelectorNode that = (TaskSelectorNode) other;
        return Objects.equal(this.parentProjectNode, that.parentProjectNode) && Objects.equal(this.taskSelector, that.taskSelector);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.parentProjectNode, this.taskSelector, this.taskSelector);
    }

    public String getProjectPath() {
        return this.taskSelector.getProjectPath().getPath();
    }

}
