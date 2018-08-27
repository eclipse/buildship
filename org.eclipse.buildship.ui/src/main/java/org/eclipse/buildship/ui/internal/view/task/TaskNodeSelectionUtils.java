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

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.internal.util.gradle.HierarchicalElementUtils;
import org.eclipse.buildship.core.internal.util.variable.ExpressionUtils;
import org.eclipse.buildship.ui.internal.util.nodeselection.NodeSelection;

/**
 * Utility class related to the node selection in the {@link TaskView}.
 */
public final class TaskNodeSelectionUtils {

    private TaskNodeSelectionUtils() {
    }

    /**
     * Checks whether the given selection can be mapped to a Gradle run configuration.
     *
     * @param selection the selection to investigate
     * @return {@code true} if the the selection can be mapped to a run configuration
     */
    public static boolean isValidRunConfiguration(NodeSelection selection) {
        return TaskViewActionStateRules.taskScopedTaskExecutionActionsEnablement(selection).asBoolean() ||
                TaskViewActionStateRules.projectScopedTaskExecutionActionsEnabledFor(selection);
    }

    /**
     * Tries to map the given selection to a Gradle run configuration.
     *
     * @param selection the selection to map
     * @return the mapped run configuration, if possible
     */
    public static Optional<GradleRunConfigurationAttributes> tryGetRunConfigurationAttributes(NodeSelection selection) {
        if (isValidRunConfiguration(selection)) {
            return Optional.of(getRunConfigurationAttributes(selection));
        } else {
            return Optional.absent();
        }
    }

    /**
     * Maps the given selection to a Gradle run configuration.
     *
     * @param selection the selection to map
     * @return the mapped run configuration
     */
    @SuppressWarnings("ConstantConditions")
    public static GradleRunConfigurationAttributes getRunConfigurationAttributes(NodeSelection selection) {
        Preconditions.checkNotNull(selection);
        List<String> tasks = getTaskPathStrings(selection);

        if (TaskViewActionStateRules.taskScopedTaskExecutionActionsEnablement(selection).asBoolean()) {
            return runConfigAttributesForTask(selection, tasks);
        } else if (TaskViewActionStateRules.projectScopedTaskExecutionActionsEnabledFor(selection)) {
            return runConfigAttributesForProject(selection, tasks);
        } else {
            throw new IllegalStateException("Unsupported selection: " + selection);
        }
    }

    private static GradleRunConfigurationAttributes runConfigAttributesForTask(NodeSelection selection, List<String> tasks) {
        TaskNode taskNode = selection.getFirstElement(TaskNode.class);
        File rootDir = HierarchicalElementUtils.getRoot(taskNode.getParentProjectNode().getEclipseProject()).getProjectDirectory();
        File workingDir = workingDirForTask(taskNode, rootDir);
        return createARunConfigAttributes(rootDir, workingDir, tasks);
    }

    private static File workingDirForTask(TaskNode taskNode, File rootDir) {
        if (taskNode instanceof ProjectTaskNode) {
            return rootDir;
        } else if (taskNode instanceof TaskSelectorNode) {
            return taskNode.getParentProjectNode().getEclipseProject().getProjectDirectory();
        } else {
            throw new GradlePluginsRuntimeException("Unrecognized task type " + taskNode.getClass().getName());
        }
    }

    private static GradleRunConfigurationAttributes runConfigAttributesForProject(NodeSelection selection, List<String> tasks) {
        ProjectNode projectNode = selection.getFirstElement(ProjectNode.class);
        File rootDir = HierarchicalElementUtils.getRoot(projectNode.getEclipseProject()).getProjectDirectory();
        return createARunConfigAttributes(rootDir, rootDir, tasks);
    }

    private static GradleRunConfigurationAttributes createARunConfigAttributes(File rootDir, File workingDir, List<String> tasks) {
        BuildConfiguration buildConfig = CorePlugin.configurationManager().loadBuildConfiguration(rootDir);
        return new GradleRunConfigurationAttributes(tasks,
                                                    projectDirectoryExpression(workingDir),
                                                    buildConfig.getGradleDistribution().serializeToString(),
                                                    gradleUserHomeExpression(buildConfig.getGradleUserHome()),
                                                    null,
                                                    Collections.<String>emptyList(),
                                                    Collections.<String>emptyList(),
                                                    true,
                                                    true,
                                                    buildConfig.isOverrideWorkspaceSettings(),
                                                    buildConfig.isOfflineMode(),
                                                    buildConfig.isBuildScansEnabled());
    }

    private static String projectDirectoryExpression(File rootProjectDir) {
        // return the directory as an expression if the project is part of the workspace, otherwise
        // return the absolute path of the project directory available on the Eclipse project model
        Optional<IProject> project = CorePlugin.workspaceOperations().findProjectByLocation(rootProjectDir);
        if (project.isPresent()) {
            return ExpressionUtils.encodeWorkspaceLocation(project.get());
        } else {
            return rootProjectDir.getAbsolutePath();
        }
    }

    private static String gradleUserHomeExpression(File gradleUserHome) {
        return gradleUserHome == null ? "" : gradleUserHome.getAbsolutePath();
    }

    private static ImmutableList<String> getTaskPathStrings(NodeSelection selection) {
        if (TaskViewActionStateRules.taskScopedTaskExecutionActionsEnablement(selection).asBoolean()) {
            // running the set of project tasks and task selectors
            ImmutableList.Builder<String> taskStrings = ImmutableList.builder();
            for (TaskNode node : selection.toList(TaskNode.class)) {
                TaskNode.TaskNodeType type = node.getType();
                switch (type) {
                    case PROJECT_TASK_NODE:
                        taskStrings.add(((ProjectTaskNode) node).getPath());
                        break;
                    case TASK_SELECTOR_NODE:
                        taskStrings.add(((TaskSelectorNode) node).getName());
                        break;
                    default:
                        throw new IllegalStateException("Unsupported Task node type: " + type);
                }
            }
            return taskStrings.build();
        } else if (TaskViewActionStateRules.projectScopedTaskExecutionActionsEnabledFor(selection)) {
            // running the project default tasks means running an empty list of tasks
            return ImmutableList.of();
        } else {
            throw new IllegalStateException("Unsupported selection: " + selection);
        }
    }
}
