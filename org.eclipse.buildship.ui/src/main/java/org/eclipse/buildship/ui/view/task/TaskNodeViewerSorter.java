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

import com.google.common.collect.Ordering;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import org.eclipse.buildship.core.omnimodel.OmniEclipseProject;
import org.eclipse.buildship.ui.view.task.TaskNode.TaskNodeType;

/**
 * Sorts {@link TaskNode} instances based on their type and/or visibility.
 */
public final class TaskNodeViewerSorter extends ViewerComparator {

    private final Ordering<ProjectNode> projectNodeOrdering;
    private final Ordering<TaskNode> taskNodeOrdering;
    private final Ordering<FaultyProjectNode> faultyProjectOrdering;

    private TaskNodeViewerSorter(Ordering<ProjectNode> projectNodeOrdering, Ordering<TaskNode> taskNodeOrdering) {
        this.projectNodeOrdering = projectNodeOrdering;
        this.taskNodeOrdering = taskNodeOrdering;
        this.faultyProjectOrdering = createLexicographicalFaultyProjectOrdering();
    }

    @Override
    public int compare(Viewer viewer, Object leftNode, Object rightNode) {
        if (leftNode instanceof ProjectNode && rightNode instanceof ProjectNode) {
            ProjectNode left = (ProjectNode) leftNode;
            ProjectNode right = (ProjectNode) rightNode;
            return this.projectNodeOrdering.compare(left, right);
        } else if (leftNode instanceof TaskNode && rightNode instanceof TaskNode) {
            TaskNode left = (TaskNode) leftNode;
            TaskNode right = (TaskNode) rightNode;
            return this.taskNodeOrdering.compare(left, right);
        } else if (leftNode instanceof FaultyProjectNode && rightNode instanceof ProjectNode) {
            return 1;
        } else if (leftNode instanceof ProjectNode && rightNode instanceof FaultyProjectNode) {
            return -1;
        } else  if (leftNode instanceof FaultyProjectNode && rightNode instanceof FaultyProjectNode) {
            FaultyProjectNode left = (FaultyProjectNode) leftNode;
            FaultyProjectNode right = (FaultyProjectNode) rightNode;
            return this.faultyProjectOrdering.compare(left, right);
        } else {
            return super.compare(viewer, leftNode, rightNode);
        }
    }

    /**
     * Creates a new instance.
     * <p>
     * The arguments define the properties of the sorting. If both
     * {@code TaskViewState#isSortByType} and {@code TaskViewState#isSortByVisibility} are true,
     * then first we order the nodes by type, then by visibility. If exactly one argument is true,
     * than that will be the sorting strategy.
     * <p>
     * If there is a tie in the sorting or both criteria are false, then the alphabetical ordering
     * is applied.
     *
     * @param state the state from which to derive how to sort the nodes
     * @return the new sorter instance
     */
    public static TaskNodeViewerSorter createFor(TaskViewState state) {
        Ordering<ProjectNode> projectOrdering = createProjectNodeOrdering();
        Ordering<TaskNode> taskOrdering = createTaskNodeOrdering(state.isSortByType(), state.isSortByVisibility());
        return new TaskNodeViewerSorter(projectOrdering, taskOrdering);
    }

    private static Ordering<ProjectNode> createProjectNodeOrdering() {
        return new Ordering<ProjectNode>() {

            @Override
            public int compare(ProjectNode left, ProjectNode right) {
                OmniEclipseProject leftRoot = left.getEclipseProject().getRoot();
                OmniEclipseProject rightRoot = right.getEclipseProject().getRoot();
                if (leftRoot == rightRoot) {
                    // do not change sorting of projects that belong to the same root
                    return 0;
                } else {
                    // projects that do not belong to the same root should be grouped by the name of
                    // their root projects
                    return leftRoot.getName().compareTo(rightRoot.getName());
                }
            }
        };
    }

    private static Ordering<TaskNode> createTaskNodeOrdering(boolean byType, boolean byVisibility) {
        // sort (optionally) by type, then (optionally) by visibility and
        // at the end (always) lexicographically
        Ordering<TaskNode> ord = createLexicographicalTaskOrdering();
        if (byVisibility) {
            ord = createByVisibilityOrdering().compound(ord);
        }
        if (byType) {
            ord = createByTypeOrdering().compound(ord);
        }
        return ord;
    }

    private static Ordering<TaskNode> createLexicographicalTaskOrdering() {
        return new Ordering<TaskNode>() {

            @Override
            public int compare(TaskNode left, TaskNode right) {
                return left.getName().compareTo(right.getName());
            }
        };
    }

    private static Ordering<FaultyProjectNode> createLexicographicalFaultyProjectOrdering() {
        return new Ordering<FaultyProjectNode>() {

            @Override
            public int compare(FaultyProjectNode left, FaultyProjectNode right) {
                return left.getWorkspaceProject().get().getName().compareTo(right.getWorkspaceProject().get().getName());
            }
        };
    }

    private static Ordering<TaskNode> createByVisibilityOrdering() {
        return new Ordering<TaskNode>() {

            @Override
            public int compare(TaskNode left, TaskNode right) {
                int leftOrdinal = toOrdinal(left);
                int rightOrdinal = toOrdinal(right);
                return (leftOrdinal < rightOrdinal ? -1 : (leftOrdinal == rightOrdinal ? 0 : 1));
            }

            private int toOrdinal(TaskNode node) {
                // public tasks to be shown first
                return node.isPublic() ? 1 : 2;
            }
        };
    }

    private static Ordering<TaskNode> createByTypeOrdering() {
        return new Ordering<TaskNode>() {

            @Override
            public int compare(TaskNode left, TaskNode right) {
                int leftOrdinal = toOrdinal(left.getType());
                int rightOrdinal = toOrdinal(right.getType());
                return (leftOrdinal < rightOrdinal ? -1 : (leftOrdinal == rightOrdinal ? 0 : 1));
            }

            private int toOrdinal(TaskNodeType type) {
                // project tasks to be shown first
                return type == TaskNodeType.PROJECT_TASK_NODE ? 1 : 2;
            }
        };
    }

}
