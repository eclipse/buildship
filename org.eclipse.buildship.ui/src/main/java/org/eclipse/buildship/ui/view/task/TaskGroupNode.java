/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package org.eclipse.buildship.ui.view.task;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Tree node in the {@link TaskView} representing a task group.
 */
public final class TaskGroupNode {

    private final ProjectNode projectNode;
    private final String group;

    public TaskGroupNode(ProjectNode projectNode, String group) {
        this.projectNode = Preconditions.checkNotNull(projectNode);
        this.group = group;
    }

    public ProjectNode getProjectNode() {
        return this.projectNode;
    }

    public String getGroup() {
        return this.group;
    }

    @Override
    public String toString() {
        return "Task group '" + this.group + "'";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        TaskGroupNode that = (TaskGroupNode) other;
        return Objects.equal(this.projectNode, that.projectNode) && Objects.equal(this.group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.projectNode, this.group);
    }

}
