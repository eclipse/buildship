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

package org.eclipse.buildship.ui.taskview;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

import org.eclipse.buildship.ui.generic.NodeSelection;

/**
 * Contains the logic that determines the state of the actions of the {@link TaskView} for a given
 * selection.
 */
public final class TaskViewActionStateRules {

    private TaskViewActionStateRules() {
    }

    /**
     * Determines whether actions related to task execution should be visible or hidden.
     *
     * @param nodeSelection the node selection based on which to make the decision
     * @return {@code true} if actions related to task execution should be visible
     */
    public static boolean taskScopedTaskExecutionActionsVisibleFor(NodeSelection nodeSelection) {
        // short-circuit in case the selection is empty
        if (nodeSelection.isEmpty()) {
            return false;
        }

        // show action if all selected nodes are task nodes (even if they are task nodes of
        // different types)
        return nodeSelection.hasAllNodesOfType(TaskNode.class);
    }

    /**
     * Determines whether the actions related to task execution should be enabled or disabled.
     *
     * @param nodeSelection the node selection based on which to make the decision
     * @return {@code true} if actions related to task execution should be enabled
     */
    public static boolean taskScopedTaskExecutionActionsEnabledFor(NodeSelection nodeSelection) {
        // short-circuit in case the selection is empty
        if (nodeSelection.isEmpty()) {
            return false;
        }

        // enable action if all selected nodes are task nodes of the same concrete type and they
        // belong to the same parent
        return (nodeSelection.hasAllNodesOfType(ProjectTaskNode.class) || nodeSelection.hasAllNodesOfType(TaskSelectorNode.class))
                && taskNodesBelongToSameParentProjectNode(nodeSelection);
    }

    private static boolean taskNodesBelongToSameParentProjectNode(NodeSelection nodeSelection) {
        Preconditions.checkArgument(!nodeSelection.isEmpty());
        final TaskNode firstNode = (TaskNode) nodeSelection.getFirstNode();
        return nodeSelection.allMatch(new Predicate<Object>() {

            @Override
            public boolean apply(Object input) {
                return ((TaskNode) input).getParentProjectNode() == firstNode.getParentProjectNode();
            }
        });
    }

    /**
     * Determines whether the project-scoped actions related to task execution should be visible or hidden.
     *
     * @param nodeSelection the node selection based on which to make the decision
     * @return {@code true} if project-scoped actions related to task execution should be visible
     */
    @SuppressWarnings("SimplifiableIfStatement")
    public static boolean projectScopedTaskExecutionActionsVisibleFor(NodeSelection nodeSelection) {
        // short-circuit in case the selection is empty
        if (nodeSelection.isEmpty()) {
            return false;
        }

        return nodeSelection.hasAllNodesOfType(ProjectNode.class);
    }

    /**
     * Determines whether the project-scoped actions related to task execution should be enabled or disabled.
     *
     * @param nodeSelection the node selection based on which to make the decision
     * @return {@code true} if project-scoped actions related to task execution should be enabled
     */
    @SuppressWarnings("SimplifiableIfStatement")
    public static boolean projectScopedTaskExecutionActionsEnabledFor(NodeSelection nodeSelection) {
        // short-circuit in case the selection is empty
        if (nodeSelection.isEmpty()) {
            return false;
        }

        return nodeSelection.hasAllNodesOfType(ProjectNode.class) && nodeSelection.isSingleSelection();
    }

}
