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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filters {@link TaskNode} instances based on their type and visibility.
 */
public final class TaskNodeViewerFilter extends ViewerFilter {

    private final Predicate<TaskNode> taskNodePredicate;
    private final Predicate<TaskGroupNode> taskGroupNodePredicate;

    private TaskNodeViewerFilter(Predicate<TaskNode> taskNodePredicate, Predicate<TaskGroupNode> taskGroupNodePredicate) {
        this.taskNodePredicate = taskNodePredicate;
        this.taskGroupNodePredicate = taskGroupNodePredicate;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        // we filter only tasks
        if (element instanceof TaskNode) {
            TaskNode taskNode = (TaskNode) element;
            return this.taskNodePredicate.apply(taskNode);
        } else if (element instanceof TaskGroupNode) {
            TaskGroupNode taskGroupNode = (TaskGroupNode) element;
            return this.taskGroupNodePredicate.apply(taskGroupNode);
        } else {
            return true;
        }
    }

    /**
     * Creates a new instance.
     * <p>
     * The arguments define all of the the criteria of the filtering that must match.
     *
     * @param state the state from which to derive the nodes to include
     * @return the new filter instance
     */
    public static ViewerFilter createFor(TaskViewState state) {
        Predicate<TaskNode> taskNodeFilter = createTaskNodeFilter(state);
        Predicate<TaskGroupNode> taskGroupNodeFilter = createGroupTaskNodeFiter(taskNodeFilter);
        return new TaskNodeViewerFilter(taskNodeFilter, taskGroupNodeFilter);
    }

    private static Predicate<TaskNode> createTaskNodeFilter(final TaskViewState state) {
        Predicate<TaskNode> projectTasks = new Predicate<TaskNode>() {

            @Override
            public boolean apply(TaskNode taskNode) {
                return state.isProjectTasksVisible() && taskNode.getType() == TaskNode.TaskNodeType.PROJECT_TASK_NODE;
            }
        };

        Predicate<TaskNode> taskSelectors = new Predicate<TaskNode>() {

            @Override
            public boolean apply(TaskNode taskNode) {
                return state.isTaskSelectorsVisible() && taskNode.getType() == TaskNode.TaskNodeType.TASK_SELECTOR_NODE;
            }
        };

        Predicate<TaskNode> privateTasks = new Predicate<TaskNode>() {

            @Override
            public boolean apply(TaskNode taskNode) {
                return state.isPrivateTasksVisible() || taskNode.isPublic();
            }
        };

        return Predicates.and(Predicates.or(projectTasks, taskSelectors), privateTasks);
    }

    private static Predicate<TaskGroupNode> createGroupTaskNodeFiter(final Predicate<TaskNode> taskNodeFilter) {
        return new Predicate<TaskGroupNode>() {

            @Override
            public boolean apply(TaskGroupNode taskGroupNode) {
                return !FluentIterable.from(taskGroupNode.getTaskNodes()).filter(taskNodeFilter).toList().isEmpty();
            }
        };
    }

}
