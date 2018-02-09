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

import java.io.File;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.omnimodel.OmniEclipseProject;
import org.eclipse.buildship.core.omnimodel.OmniGradleProject;
import org.eclipse.buildship.core.omnimodel.OmniProjectTask;
import org.eclipse.buildship.core.omnimodel.OmniTaskSelector;

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
            List<OmniEclipseProject> projects = taskViewContent.getProjects();
            List<IProject> faultyProjects = taskViewContent.getFaultyProjects();
            result.addAll(createTopLevelProjectNodes(projects, faultyProjects));
        }
        return result.build().toArray();
    }

    private List<BaseProjectNode> createTopLevelProjectNodes(List<OmniEclipseProject> projects, List<IProject> faultyProjects) {
        // flatten the tree of Gradle projects to a list, similar
        // to how Eclipse projects look in the Eclipse Project explorer
        List<BaseProjectNode> allProjectNodes = Lists.newArrayList();
        for (OmniEclipseProject project : projects) {
            if (project.getParent() == null) {
                collectProjectNodesRecursively(project, null, allProjectNodes);
            }
        }
        for (IProject faultyProject : faultyProjects) {
            allProjectNodes.add(new FaultyProjectNode(faultyProject));
        }

        return allProjectNodes;
    }

    private void collectProjectNodesRecursively(OmniEclipseProject eclipseProject, ProjectNode parentProjectNode, List<BaseProjectNode> allProjectNodes) {
        OmniGradleProject gradleProject = eclipseProject.getGradleProject();

        // find the corresponding Eclipse project in the workspace
        // (find by location rather than by name since the Eclipse project name does not always correspond to the Gradle project name)
        Optional<IProject> workspaceProject = CorePlugin.workspaceOperations().findProjectByName(eclipseProject.getName());

        // create a new node for the given Eclipse project and then recurse into the children
        ProjectNode projectNode = new ProjectNode(parentProjectNode, eclipseProject, gradleProject, workspaceProject, isIncludedProject(workspaceProject, eclipseProject));
        allProjectNodes.add(projectNode);
        for (OmniEclipseProject childProject : eclipseProject.getChildren()) {
            collectProjectNodesRecursively(childProject, projectNode, allProjectNodes);
        }
    }

    private static boolean isIncludedProject(Optional<IProject> workspaceProject, OmniEclipseProject modelProject) {
        if (!workspaceProject.isPresent()) {
            return false;
        }

        IProject project = workspaceProject.get();
        if (!GradleProjectNature.isPresentOn(project)) {
            return false;
        }

        ProjectConfiguration projectConfig = CorePlugin.configurationManager().loadProjectConfiguration(project);
        File configRootDir = projectConfig.getBuildConfiguration().getRootProjectDirectory();
        File modelRootDir = modelProject.getProjectIdentifier().getBuildIdentifier().getRootDir();
        return !modelRootDir.equals(configRootDir);
    }

    @Override
    public boolean hasChildren(Object element) {
        return element instanceof ProjectNode || element instanceof TaskGroupNode;
    }

    @Override
    public Object[] getChildren(Object parent) {
        if (parent instanceof ProjectNode) {
            return childrenOf((ProjectNode) parent);
        } else if (parent instanceof TaskGroupNode){
            return childrenOf((TaskGroupNode) parent);
        } else {
            return NO_CHILDREN;
        }
    }

    private Object[] childrenOf(ProjectNode projectNode) {
        if (this.taskView.getState().isGroupTasks()) {
            return groupNodesFor(projectNode).toArray();
        } else {
            return taskNodesFor(projectNode).toArray();
        }
    }

    private Set<TaskGroupNode> groupNodesFor(ProjectNode projectNode) {
        Set<TaskGroupNode> result = Sets.newHashSet();
        result.add(TaskGroupNode.getDefault(projectNode));
        for (OmniProjectTask projectTask : projectNode.getGradleProject().getProjectTasks()) {
            result.add(TaskGroupNode.forName(projectNode, projectTask.getGroup()));
        }
        for (OmniTaskSelector taskSelector : projectNode.getGradleProject().getTaskSelectors()) {
            result.add(TaskGroupNode.forName(projectNode, taskSelector.getGroup()));
        }
        return result;
    }

    private List<TaskNode> taskNodesFor(ProjectNode projectNode) {
        List<TaskNode> taskNodes = Lists.newArrayList();
        for (OmniProjectTask projectTask : projectNode.getGradleProject().getProjectTasks()) {
            taskNodes.add(new ProjectTaskNode(projectNode, projectTask));
        }
        for (OmniTaskSelector taskSelector : projectNode.getGradleProject().getTaskSelectors()) {
            taskNodes.add(new TaskSelectorNode(projectNode, taskSelector));
        }
        return taskNodes;
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
