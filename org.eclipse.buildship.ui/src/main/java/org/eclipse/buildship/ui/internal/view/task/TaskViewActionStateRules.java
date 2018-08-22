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

import java.util.List;

import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.buildship.core.internal.util.gradle.HierarchicalElementUtils;
import org.eclipse.buildship.ui.internal.util.nodeselection.NodeSelection;

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
     * @return a classification whether and why the action should be disabled or enabled
     */
    public static TaskScopedActionEnablement taskScopedTaskExecutionActionsEnablement(NodeSelection nodeSelection) {
        // short-circuit in case the selection is empty
        if (nodeSelection.isEmpty()) {
            return TaskScopedActionEnablement.DISABLED_DEFAULT;
        }

        // execution is enabled only if no tasks from included builds are selected and each task is from the same project
        List<?> elements = nodeSelection.toList();
        List<TaskNode> taskNodes = FluentIterable.from(elements).filter(TaskNode.class).toList();
        if (elements.size() != taskNodes.size()) {
            return TaskScopedActionEnablement.DISABLED_DEFAULT;
        }

        if (hasMultipleOrIncludedParentProject(taskNodes)) {
            return TaskScopedActionEnablement.DISABLED_INCLUDED_BUILD;
        }

        // if project tasks are selected only then the execution should be permitted
        List<ProjectTaskNode> projectNodes = FluentIterable.from(elements).filter(ProjectTaskNode.class).toList();
        if (projectNodes.size() == taskNodes.size()) {
            return TaskScopedActionEnablement.ENABLED;
        }

        // if task selectors are selected only then the execution should be permitted if the root project can be found
        List<TaskSelectorNode> taskSelectorNodes = FluentIterable.from(elements).filter(TaskSelectorNode.class).toList();
        if (taskSelectorNodes.size() == taskNodes.size()) {
            boolean hasRootProjects = canFindRootProjects(taskSelectorNodes);
            return hasRootProjects ? TaskScopedActionEnablement.ENABLED : TaskScopedActionEnablement.DISABLED_NO_ROOT_PROJECT;
        }

        // as a default disable the execution
        return  TaskScopedActionEnablement.DISABLED_DEFAULT;
    }

    private static boolean hasMultipleOrIncludedParentProject(List<TaskNode> nodes) {
        Preconditions.checkArgument(!nodes.isEmpty());
        final ProjectNode firstParent = nodes.get(0).getParentProjectNode();
        if (firstParent.isIncludedProject()) {
            return true;
        }
        return Iterables.any(nodes, new Predicate<TaskNode>() {

            @Override
            public boolean apply(TaskNode node) {
                return !node.getParentProjectNode().equals(firstParent);
            }
        });
    }

    //see https://docs.gradle.org/current/userguide/build_lifecycle.html#sec:initialization
    private static boolean canFindRootProjects(List<TaskSelectorNode> nodes) {
        return Iterables.all(nodes, new Predicate<TaskNode>() {

            @Override
            public boolean apply(TaskNode node) {
                EclipseProject project = node.getParentProjectNode().getEclipseProject();
                Path projectPath = new Path(project.getProjectDirectory().getPath());
                IPath masterPath = projectPath.removeLastSegments(1).append("master");
                Path rootPath = new Path(HierarchicalElementUtils.getRoot(project).getProjectDirectory().getPath());
                return rootPath.isPrefixOf(projectPath) || rootPath.equals(masterPath);
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

    /**
     * Possible statuses that {@link #taskScopedTaskExecutionActionsEnablement(NodeSelection)} can return.
     */
    public enum TaskScopedActionEnablement {
        ENABLED, DISABLED_DEFAULT, DISABLED_INCLUDED_BUILD, DISABLED_NO_ROOT_PROJECT;

        public boolean asBoolean() {
            return this == ENABLED;
        }
    }
}
