/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.buildship.core.internal.CorePlugin;

/**
 * Content provider for the {@link TaskView}.
 * <p/>
 * The 'UI-model' behind the task view provided by this class are nodes; {@link ProjectNode},
 * {@link ProjectTaskNode} and {@link TaskSelectorNode}. With this we can connect the mode and the
 * UI elements.
 */
public final class TaskViewContentProvider implements ITreeContentProvider {

    private static final Object[] NO_CHILDREN = new Object[0];

    private final TaskView taskView;

    public TaskViewContentProvider(TaskView taskView) {
        this.taskView = Preconditions.checkNotNull(taskView);
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Object[] getElements(Object input) {
        ImmutableList.Builder<Object> result = ImmutableList.builder();
        if (input instanceof TaskViewContent) {
            TaskViewContent taskViewContent = (TaskViewContent) input;
            result.addAll(createTopLevelProjectNodes(taskViewContent));
            result.addAll(taskViewContent.getFaultyBuilds());
        }
        return result.build().toArray();
    }

    private List<BaseProjectNode> createTopLevelProjectNodes(TaskViewContent taskViewContent) {
        // flatten the tree of Gradle projects to a list, similar
        // to how Eclipse projects look in the Eclipse Project explorer
        List<BaseProjectNode> allProjectNodes = Lists.newArrayList();
        Collection<BuildNode> builds = taskViewContent.getBuilds();

        if (this.taskView.getState().isProjectHierarchyFlattened()) {
            // flatten the tree of Gradle projects to a list, similar
            // to how Eclipse projects look in the Project explorer
            for (BuildNode build : builds) {
                collectProjectNodesRecursively(build.getRootEclipseProject(), null, allProjectNodes, build);
            }
        } else {
            // put all subprojects into the parent project's folder, similar
            // to how a Java class look in the Eclipse Type Hierarchy
            for (BuildNode build : builds) {
                EclipseProject rootEclipseProject = build.getRootEclipseProject();
                Optional<IProject> workspaceProject = CorePlugin.workspaceOperations().findProjectByName(rootEclipseProject.getName());
                allProjectNodes.add(new ProjectNode(null, build, workspaceProject, rootEclipseProject));
            }
        }

        return allProjectNodes;
    }

    private void collectProjectNodesRecursively(EclipseProject eclipseProject, ProjectNode parentProjectNode, List<BaseProjectNode> acc, BuildNode buildNode) {
        // find the corresponding Eclipse project in the workspace
        // (find by location rather than by name since the Eclipse project name does not always
        // correspond to the Gradle project name)
        Optional<IProject> workspaceProject = CorePlugin.workspaceOperations().findProjectByName(eclipseProject.getName());

        // create a new node for the given Eclipse project and then recurse into the children
        ProjectNode projectNode = new ProjectNode(parentProjectNode, buildNode, workspaceProject, eclipseProject);
        acc.add(projectNode);
        for (EclipseProject childProject : eclipseProject.getChildren()) {
            collectProjectNodesRecursively(childProject, projectNode, acc, buildNode);
        }
    }

    @Override
    public boolean hasChildren(Object element) {
        return element instanceof ProjectNode || element instanceof TaskGroupNode;
    }

    @Override
    public Object[] getChildren(Object parent) {
        if (parent instanceof ProjectNode) {
            return childrenOf((ProjectNode) parent);
        } else if (parent instanceof TaskGroupNode) {
            return childrenOf((TaskGroupNode) parent);
        } else {
            return NO_CHILDREN;
        }
    }

    private Object[] childrenOf(ProjectNode projectNode) {
        Set<Object> result = Sets.newHashSet();

        if (!this.taskView.getState().isProjectHierarchyFlattened()) {
            result.addAll(projectNodesFor(projectNode));
        }

        if (this.taskView.getState().isGroupTasks()) {
            result.addAll(groupNodesFor(projectNode));
        } else {
            result.addAll(taskNodesFor(projectNode));
        }

        return result.toArray();
    }

    private Set<ProjectNode> projectNodesFor(ProjectNode projectNode) {
        Set<ProjectNode> result = Sets.newHashSet();
        EclipseProject eclipseProject = projectNode.getEclipseProject();

        for (EclipseProject childProject : eclipseProject.getChildren()) {
            Optional<IProject> workspaceProject = CorePlugin.workspaceOperations().findProjectByLocation(childProject.getProjectDirectory());
            ProjectNode childProjectNode = new ProjectNode(projectNode, projectNode.getBuildNode(), workspaceProject, childProject);
            result.add(childProjectNode);
        }
        return result;
    }

    public static Set<TaskGroupNode> groupNodesFor(ProjectNode projectNode) {
        Set<TaskGroupNode> result = Sets.newHashSet();
        result.add(TaskGroupNode.getDefault(projectNode));
        for (ProjectTask projectTask : projectNode.getInvocations().getProjectTasks()) {
            result.add(TaskGroupNode.forName(projectNode, projectTask.getGroup()));
        }
        for (TaskSelector taskSelector : projectNode.getInvocations().getTaskSelectors()) {
            result.add(TaskGroupNode.forName(projectNode, taskSelector.getGroup()));
        }
        return result;
    }

    public static List<TaskNode> taskNodesFor(ProjectNode projectNode) {
        List<TaskNode> taskNodes = Lists.newArrayList();

        for (ProjectTask projectTask : projectNode.getInvocations().getProjectTasks()) {
            taskNodes.add(new ProjectTaskNode(projectNode, projectTask));
        }

        if (shouldContainTaskSelectors(projectNode)) {
            for (TaskSelector taskSelector : projectNode.getInvocations().getTaskSelectors()) {
                taskNodes.add(new TaskSelectorNode(projectNode, taskSelector));
            }
        }
        return taskNodes;
    }

    private static boolean shouldContainTaskSelectors(ProjectNode projectNode) {
        return !projectNode.getBuildNode().isIncludedBuild();
    }

    private Object[] childrenOf(TaskGroupNode groupNode) {
        return groupNode.getTaskNodes().toArray();
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof ProjectNode) {
            return ((ProjectNode) element).getParentProjectNode();
        } else if (element instanceof TaskNode) {
            return ((TaskNode) element).getParentProjectNode();
        } else if (element instanceof TaskGroupNode) {
            return ((TaskGroupNode) element).getProjectNode();
        } else {
            return null;
        }
    }

    @Override
    public void dispose() {
    }
}
