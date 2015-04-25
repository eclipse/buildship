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

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.core.util.variable.ExpressionUtils;
import org.eclipse.buildship.ui.domain.ProjectNode;
import org.eclipse.buildship.ui.domain.ProjectTaskNode;
import org.eclipse.buildship.ui.domain.TaskNode;
import org.eclipse.buildship.ui.domain.TaskSelectorNode;
import org.eclipse.buildship.ui.generic.NodeSelection;

/**
 * Base class for handlers that operate on the selected tasks.
 */
public abstract class BaseRunConfigurationHandler extends SelectionDependentHandler {

    @Override
    protected boolean isEnabledFor(NodeSelection selection) {
        return TaskViewActionStateRules.taskScopedTaskExecutionActionsEnabledFor(selection) ||
                TaskViewActionStateRules.projectScopedTaskExecutionActionsEnabledFor(selection);
    }

    protected GradleRunConfigurationAttributes getRunConfigurationAttributes(ExecutionEvent event) {
        // validate handler can run for current selection history before executing anything
        NodeSelection selectionHistory = getSelectionHistory(event);

        // read the project configuration from the workspace project of the selected nodes (if accessible)
        Optional<FixedRequestAttributes> requestAttributes = getFixedRequestAttributes(selectionHistory);

        // determine the string representation of the tasks to run, relative to the project directory
        // (we currently work with Strings since Launchable is only available as of 1.12)
        ImmutableList<String> tasks = getTaskPathStrings(selectionHistory);

        // determine the project directory from the selected nodes
        // (we build on the invariant that all selected tasks have the same parent directory and
        // they are all either of type ProjectTaskNode or TaskSelectorNode)
        String projectDirectoryExpression = getProjectDirectoryExpression(selectionHistory);

        // determine the Gradle distribution
        GradleDistribution gradleDistribution = requestAttributes.isPresent() ? requestAttributes.get().getGradleDistribution() : GradleDistribution.fromBuild();

        // determine the advanced options
        String gradleUserHome = requestAttributes.isPresent() ? FileUtils.getAbsolutePath(requestAttributes.get().getGradleUserHome()).orNull() : null;
        String javaHome = requestAttributes.isPresent() ? FileUtils.getAbsolutePath(requestAttributes.get().getJavaHome()).orNull() : null;
        List<String> jvmArguments = requestAttributes.isPresent() ? requestAttributes.get().getJvmArguments() : ImmutableList.<String> of();
        List<String> arguments = requestAttributes.isPresent() ? requestAttributes.get().getArguments() : ImmutableList.<String> of();

        // create the run configuration with test progress visualization enabled by default
        return GradleRunConfigurationAttributes.with(tasks, projectDirectoryExpression, gradleDistribution, gradleUserHome, javaHome, jvmArguments, arguments, true);
    }

    private Optional<FixedRequestAttributes> getFixedRequestAttributes(NodeSelection selection) {
        if (TaskViewActionStateRules.taskScopedTaskExecutionActionsEnabledFor(selection)) {
            TaskNode taskNode = selection.getFirstNode(TaskNode.class);
            return getFixedRequestAttributes(taskNode.getParentProjectNode());
        } else if (TaskViewActionStateRules.projectScopedTaskExecutionActionsEnabledFor(selection)) {
            ProjectNode projectNode = selection.getFirstNode(ProjectNode.class);
            return getFixedRequestAttributes(projectNode);
        } else {
            throw new IllegalStateException("Unsupported selection: " + selection);
        }
    }

    private Optional<FixedRequestAttributes> getFixedRequestAttributes(ProjectNode projectNode) {
        Optional<IProject> workspaceProject = projectNode.getWorkspaceProject();
        if (workspaceProject.isPresent() && workspaceProject.get().isOpen() && GradleProjectNature.INSTANCE.isPresentOn(workspaceProject.get())) {
            ProjectConfiguration projectConfiguration = CorePlugin.projectConfigurationManager().readProjectConfiguration(workspaceProject.get());
            return Optional.of(projectConfiguration.getRequestAttributes());
        } else {
            return Optional.absent();
        }
    }

    private ImmutableList<String> getTaskPathStrings(NodeSelection selection) {
        if (TaskViewActionStateRules.taskScopedTaskExecutionActionsEnabledFor(selection)) {
            // running the set of project tasks and task selectors
            ImmutableList.Builder<String> taskStrings = ImmutableList.builder();
            for (TaskNode node : selection.getNodes(TaskNode.class)) {
                TaskNode.TaskNodeType type = node.getType();
                switch (type) {
                    case PROJECT_TASK_NODE:
                        taskStrings.add(((ProjectTaskNode) node).getProjectTask().getPath().getPath());
                        break;
                    case TASK_SELECTOR_NODE:
                        taskStrings.add(((TaskSelectorNode) node).getTaskSelector().getName());
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

    private String getProjectDirectoryExpression(NodeSelection selection) {
        if (TaskViewActionStateRules.taskScopedTaskExecutionActionsEnabledFor(selection)) {
            // task selectors need to be run from the direct parent directory, project tasks can be run
            // from any directory as long as the tasks are fully qualified
            TaskNode taskNode = selection.getFirstNode(TaskNode.class);
            return getProjectDirectoryExpression(taskNode.getParentProjectNode());
        } else if (TaskViewActionStateRules.projectScopedTaskExecutionActionsEnabledFor(selection)) {
            // the default tasks of a project must be run from the direct parent directory (obviously)
            ProjectNode projectNode = selection.getFirstNode(ProjectNode.class);
            return  getProjectDirectoryExpression(projectNode);
        } else {
            throw new IllegalStateException("Unsupported selection: " + selection);
        }
    }

    private String getProjectDirectoryExpression(ProjectNode projectNode) {
        // return the directory as an expression if the project is part of the workspace, otherwise
        // return the absolute path of the project directory available on the Eclipse project model
        Optional<IProject> workspaceProject = projectNode.getWorkspaceProject();
        if (workspaceProject.isPresent()) {
            return ExpressionUtils.encodeWorkspaceLocation(workspaceProject.get());
        } else {
            return projectNode.getEclipseProject().getProjectDirectory().getAbsolutePath();
        }
    }

}
