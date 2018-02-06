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

package org.eclipse.buildship.ui.view.task;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Tree node in the {@link TaskView} representing a project task.
 */
public final class ProjectTaskNode implements TaskNode {

    private final ProjectNode parentProjectNode;
    private String name;
    private String description;
    private boolean isPublic;
    private String path;

    public ProjectTaskNode(ProjectNode parentProjectNode, String name, String description, boolean isPublic, String path) {
        this.parentProjectNode = Preconditions.checkNotNull(parentProjectNode);
        this.name = Preconditions.checkNotNull(name);
        this.description = Preconditions.checkNotNull(description);
        this.isPublic = Preconditions.checkNotNull(isPublic);
        this.path = Preconditions.checkNotNull(path);
    }

    @Override
    public ProjectNode getParentProjectNode() {
        return this.parentProjectNode;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public TaskNodeType getType() {
        return TaskNodeType.PROJECT_TASK_NODE;
    }

    @Override
    public boolean isPublic() {
        return this.isPublic;
    }

    @Override
    public String toString() {
        return this.name;
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
                && Objects.equal(this.name, that.name)
                && Objects.equal(this.description, that.description)
                && Objects.equal(this.isPublic, that.isPublic)
                && Objects.equal(this.path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.parentProjectNode, this.name, this.description, this.isPublic, this.path);
    }

    public String getDescription() {
        return this.description;
    }

    public String getPath() {
        return this.path;
    }
}
