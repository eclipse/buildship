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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filters {@link TaskNode} instances based on their type and visibility.
 */
public final class TaskNodeViewerFilter extends ViewerFilter {

    private final Predicate<TaskNode> predicate;

    private TaskNodeViewerFilter(Predicate<TaskNode> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        // we filter only tasks
        if (element instanceof TaskNode) {
            TaskNode taskNode = (TaskNode) element;
            return this.predicate.apply(taskNode);
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
        Predicate<TaskNode> predicate = createCompositeFilter(state);
        return new TaskNodeViewerFilter(predicate);
    }

    private static Predicate<TaskNode> createCompositeFilter(final TaskViewState state) {
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

}
