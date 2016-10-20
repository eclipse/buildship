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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.buildship.ui.util.nodeselection.NodeSelection;

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

        // enable action if all selected nodes are task nodes of the same concrete type, they
        // belong to the same parent and none of them come from an included build
        if (!nodeSelection.hasAllNodesOfType(TaskNode.class) || parentProjectIsIncluded(nodeSelection)) {
            return false;
        } else {
            return (nodeSelection.hasAllNodesOfType(ProjectTaskNode.class) || nodeSelection.hasAllNodesOfType(TaskSelectorNode.class) && gradleCanFindTheRootProject(nodeSelection))
                    && taskNodesBelongToSameParentProjectNode(nodeSelection);
        }
    }

    //see https://docs.gradle.org/current/userguide/build_lifecycle.html#sec:initialization
    private static boolean gradleCanFindTheRootProject(NodeSelection nodeSelection) {
        return nodeSelection.allMatch(new Predicate<Object>() {

            @Override
            public boolean apply(Object input) {
                TaskNode node = (TaskNode) input;
                OmniEclipseProject project = node.getParentProjectNode().getEclipseProject();
                Path projectPath = new Path(project.getProjectDirectory().getPath());
                IPath masterPath = projectPath.removeLastSegments(1).append("master");
                Path rootPath = new Path(project.getRoot().getProjectDirectory().getPath());
                return rootPath.isPrefixOf(projectPath) || rootPath.equals(masterPath);
            }
        });
    }

    private static boolean parentProjectIsIncluded(NodeSelection nodeSelection) {
        return nodeSelection.allMatch(new Predicate<Object>() {

            @Override
            public boolean apply(Object input) {
                TaskNode node = (TaskNode) input;
                return node.getParentProjectNode().isIncludedProject();
            }
        });
    }

    private static boolean taskNodesBelongToSameParentProjectNode(NodeSelection nodeSelection) {
        Preconditions.checkArgument(!nodeSelection.isEmpty());
        final TaskNode firstNode = (TaskNode) nodeSelection.getFirstElement();
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

        return nodeSelection.hasAllNodesOfType(ProjectNode.class) && nodeSelection.isSingleSelection() && !nodeSelection.getFirstElement(ProjectNode.class).isIncludedProject();
    }

}
