/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.ui.view.task;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingmodel.OmniProjectTask;
import com.gradleware.tooling.toolingmodel.OmniTaskSelector;
import com.gradleware.tooling.toolingmodel.util.Maybe;

/**
 * Tree node in the {@link TaskView} representing a task group.
 */
public final class TaskGroupNode {

    private static final String DEFAULT_NAME = "other";

    private final ProjectNode projectNode;
    private final String name;

    public TaskGroupNode(ProjectNode projectNode, String name) {
        this.projectNode = Preconditions.checkNotNull(projectNode);
        this.name = name != null ? name : DEFAULT_NAME;
    }

    public ProjectNode getProjectNode() {
        return this.projectNode;
    }

    public String getName() {
        return this.name;
    }

    public boolean contains(OmniProjectTask projectTask) {
        return matches(projectTask.getGroup());
    }

    public boolean contains(OmniTaskSelector taskSelector) {
        return matches(taskSelector.getGroup());
    }

    private boolean matches(Maybe<String> group) {
        if (!group.isPresent()) {
            return false;
        }
        String name = group.get();
        if (name == null) {
            return isDefault();
        } else {
            return name.equals(this.name);
        }
    }

    private boolean isDefault() {
        return DEFAULT_NAME.equals(this.name);
    }

    @Override
    public String toString() {
        return "Task group '" + this.name + "'";
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
        return Objects.equal(this.projectNode, that.projectNode) && Objects.equal(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.projectNode, this.name);
    }

}
