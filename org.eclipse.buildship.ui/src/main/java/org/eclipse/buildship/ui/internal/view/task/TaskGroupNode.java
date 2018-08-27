/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.ui.internal.view.task;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Tree node in the {@link TaskView} representing a task group.
 */
public final class TaskGroupNode {

    private static final String DEFAULT_NAME = "other";

    private final List<TaskNode> taskNodes;
    private final ProjectNode projectNode;
    private final String name;

    private TaskGroupNode(ProjectNode projectNode, String name) {
        this.projectNode = Preconditions.checkNotNull(projectNode);
        this.name = Preconditions.checkNotNull(name);
        this.taskNodes = createTaskNodes(projectNode);
    }

    private List<TaskNode> createTaskNodes(ProjectNode projectNode) {
        List<TaskNode> taskNodes = Lists.newArrayList();
        for (ProjectTask projectTask : getProjectTasks()) {
            taskNodes.add(new ProjectTaskNode(projectNode, projectTask));
        }
        for (TaskSelector taskSelector : getTaskSelectors()) {
            taskNodes.add(new TaskSelectorNode(projectNode, taskSelector));
        }
        return taskNodes;
    }

    private List<ProjectTask> getProjectTasks() {
        List<ProjectTask> projectTasks = Lists.newArrayList();
        for (ProjectTask projectTask : this.getProjectNode().getInvocations().getProjectTasks()) {
            if (this.contains(projectTask)) {
                projectTasks.add(projectTask);
            }
        }
        return projectTasks;
    }

    private List<TaskSelector> getTaskSelectors() {
        List<TaskSelector> taskSelectors = Lists.newArrayList();
        for (TaskSelector taskSelector : this.getProjectNode().getInvocations().getTaskSelectors()) {
            if (this.contains(taskSelector)) {
                taskSelectors.add(taskSelector);
            }
        }
        return taskSelectors;
    }

    public ProjectNode getProjectNode() {
        return this.projectNode;
    }

    public String getName() {
        return this.name;
    }

    public boolean contains(ProjectTask projectTask) {
        return matches(projectTask.getGroup());
    }

    public boolean contains(TaskSelector taskSelector) {
        return matches(taskSelector.getGroup());
    }

    private boolean matches(String group) {
        return normalizeGroupName(group).equals(this.name);
    }

    public List<TaskNode> getTaskNodes() {
        return this.taskNodes;
    };

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

    public static TaskGroupNode getDefault(ProjectNode projectNode) {
        return new TaskGroupNode(projectNode, DEFAULT_NAME);
    }

    public static TaskGroupNode forName(ProjectNode projectNode, String groupName) {
        Preconditions.checkNotNull(groupName);
        return new TaskGroupNode(projectNode, normalizeGroupName(groupName));
    }

    private static String normalizeGroupName(String groupName) {
        //see https://issues.gradle.org/browse/GRADLE-3429
        return groupName.toLowerCase();
    }
}
