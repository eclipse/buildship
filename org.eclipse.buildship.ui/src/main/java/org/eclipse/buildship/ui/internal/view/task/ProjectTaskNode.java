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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Tree node in the {@link TaskView} representing a project task.
 */
public final class ProjectTaskNode implements TaskNode {

    private final ProjectNode parentProjectNode;
    private final ProjectTask projectTask;

    public ProjectTaskNode(ProjectNode parentProjectNode, ProjectTask projectTask) {
        this.parentProjectNode = Preconditions.checkNotNull(parentProjectNode);
        this.projectTask = Preconditions.checkNotNull(projectTask);
    }

    @Override
    public ProjectNode getParentProjectNode() {
        return this.parentProjectNode;
    }

    @Override
    public String getName() {
        return this.projectTask.getName();
    }

    @Override
    public TaskNodeType getType() {
        return TaskNodeType.PROJECT_TASK_NODE;
    }

    @Override
    public boolean isPublic() {
        return this.projectTask.isPublic();
    }

    @Override
    public String toString() {
        return this.projectTask.getName();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ProjectTaskNode that = (ProjectTaskNode) other;
        return Objects.equal(this.parentProjectNode, that.parentProjectNode)
                && Objects.equal(this.projectTask, that.projectTask);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.parentProjectNode, this.projectTask);
    }

    public String getDescription() {
        return this.projectTask.getDescription();
    }

    public String getPath() {
        return this.projectTask.getPath().getPath();
    }
}
