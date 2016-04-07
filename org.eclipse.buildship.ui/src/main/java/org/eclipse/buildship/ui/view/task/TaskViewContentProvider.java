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

import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.OmniProjectTask;
import com.gradleware.tooling.toolingmodel.OmniTaskSelector;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.util.Maybe;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.gradle.LoadEclipseGradleBuildsJob;
import org.eclipse.buildship.core.workspace.ModelProvider;
import org.eclipse.buildship.core.workspace.WorkspaceOperations;

/**
 * Content provider for the {@link TaskView}.
 * <p/>
 * The 'UI-model' behind the task view provided by this class are nodes; {@link ProjectNode},
 * {@link ProjectTaskNode} and {@link TaskSelectorNode}. With this we can connect the mode and the
 * UI elements.
 */
public final class TaskViewContentProvider implements ITreeContentProvider {

    private static final Object[] NO_CHILDREN = new Object[0];
    private static final String GROUP_NAME_FOR_GROUPLESS_TASKS = "Other tasks";

    private final TaskView taskView;
    private final WorkspaceOperations workspaceOperations;

    public TaskViewContentProvider(TaskView taskView, WorkspaceOperations workspaceOperations) {
        this.taskView = Preconditions.checkNotNull(taskView);
        this.workspaceOperations = Preconditions.checkNotNull(workspaceOperations);
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // handle the case where the new input is null
        // (this happens when the ContentViewer gets disposed)
        if (newInput == null) {
            return;
        }

        // the only way to set the input is
        // through TaskView#setInput(TaskViewContent)
        TaskViewContent content = TaskViewContent.class.cast(newInput);
        LoadEclipseGradleBuildsJob loadEclipseGradleBuildsJob = new LoadEclipseGradleBuildsJob(content.getModelFetchStrategy(), content.getRootProjectConfigurations());
        loadEclipseGradleBuildsJob.addJobChangeListener(new RefreshTasksViewAfterJobDone(this.taskView));
        loadEclipseGradleBuildsJob.schedule();
    }

    @Override
    public Object[] getElements(Object input) {
        ImmutableList.Builder<Object> result = ImmutableList.builder();
        if (input instanceof TaskViewContent) {
            TaskViewContent content = (TaskViewContent) input;
            for (ProjectConfiguration projectConfiguration : content.getRootProjectConfigurations()) {
                result.addAll(createTopLevelProjectNodes(projectConfiguration));
            }
        }
        return result.build().toArray();
    }

    private List<ProjectNode> createTopLevelProjectNodes(ProjectConfiguration projectConfiguration) {
        OmniEclipseGradleBuild gradleBuild = fetchCachedEclipseGradleBuild(projectConfiguration.getRequestAttributes());
        if (gradleBuild == null) {
            // no Gradle projects are cached yet, meaning the async job
            // to load the projects is still running, thus nothing to show
            return ImmutableList.of();
        } else {
            // flatten the tree of Gradle projects to a list, similar
            // to how Eclipse projects look in the Eclipse Project explorer
            List<ProjectNode> allProjectNodes = Lists.newArrayList();
            collectProjectNodesRecursively(gradleBuild.getRootEclipseProject(), null, allProjectNodes);
            return allProjectNodes;
        }
    }

    private OmniEclipseGradleBuild fetchCachedEclipseGradleBuild(FixedRequestAttributes fixedRequestAttributes) {
        ModelProvider modelProvider = CorePlugin.gradleWorkspaceManager().getGradleBuild(fixedRequestAttributes).getModelProvider();
        return modelProvider.fetchEclipseGradleBuild(FetchStrategy.FROM_CACHE_ONLY, null, null);
    }

    private void collectProjectNodesRecursively(OmniEclipseProject eclipseProject, ProjectNode parentProjectNode, List<ProjectNode> allProjectNodes) {
        OmniGradleProject gradleProject = eclipseProject.getGradleProject();

        // find the corresponding Eclipse project in the workspace
        // (find by location rather than by name since the Eclipse project name does not always correspond to the Gradle project name)
        Optional<IProject> workspaceProject = TaskViewContentProvider.this.workspaceOperations.findProjectByLocation(eclipseProject.getProjectDirectory());

        // create a new node for the given Eclipse project and then recurse into the children
        ProjectNode projectNode = new ProjectNode(parentProjectNode, eclipseProject, gradleProject, workspaceProject);
        allProjectNodes.add(projectNode);
        for (OmniEclipseProject childProject : eclipseProject.getChildren()) {
            collectProjectNodesRecursively(childProject, projectNode, allProjectNodes);
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
        } else if (parent instanceof TaskGroupNode){
            return childrenOf((TaskGroupNode) parent);
        } else {
            return NO_CHILDREN;
        }
    }

    private Object[] childrenOf(ProjectNode projectNode) {
        if (this.taskView.getState().isGroupTasksByTaskGroup()) {
            return groupNodesFor(projectNode).toArray();
        } else {
            return taskNodesFor(projectNode).toArray();
        }
    }

    private List<TaskGroupNode> groupNodesFor(ProjectNode projectNode) {
        List<TaskGroupNode> result = Lists.newArrayList();
        // create a group node for each entries
        Set<String> groupNames = getTaskGroupNames(projectNode.getGradleProject());
        for (String groupName : groupNames) {
            result.add(new TaskGroupNode(projectNode, groupName));
        }
        // add an extra group node for groupless tasks
        if(!groupNames.contains(GROUP_NAME_FOR_GROUPLESS_TASKS)) {
            result.add(new TaskGroupNode(projectNode, GROUP_NAME_FOR_GROUPLESS_TASKS));
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

    private Set<String> getTaskGroupNames(OmniGradleProject project) {
        return FluentIterable.from(project.getProjectTasks()).transform(new Function<OmniProjectTask, String>() {

            @Override
            public String apply(OmniProjectTask task) {
                return task.getGroup().isPresent() ? task.getGroup().get() : null;
            }
        }).filter(Predicates.notNull()).toSet();
    }

    private Object[] childrenOf(TaskGroupNode groupNode) {
        ProjectNode projectNode = groupNode.getProjectNode();
        List<TaskNode> taskNodes = Lists.newArrayList();
        for (OmniProjectTask projectTask : projectNode.getGradleProject().getProjectTasks()) {
            if (projectTaskBelongsToGroupNode(projectTask, groupNode)) {
                taskNodes.add(new ProjectTaskNode(projectNode, projectTask));
            }
        }

        for (OmniTaskSelector taskSelector : projectNode.getGradleProject().getTaskSelectors()) {
            if (taskSelectorBelongsToGroupNode(taskSelector, groupNode)) {
                taskNodes.add(new TaskSelectorNode(projectNode, taskSelector));
            }
        }
        return taskNodes.toArray();
    }

    private boolean projectTaskBelongsToGroupNode(OmniProjectTask task, TaskGroupNode node) {
        return taskBelongsToGroupNode(task.getGroup(), node);
    }

    private boolean taskSelectorBelongsToGroupNode(OmniTaskSelector task, TaskGroupNode node) {
        return taskBelongsToGroupNode(task.getGroup(), node);
    }

    private boolean taskBelongsToGroupNode(Maybe<String> taskGroupName, TaskGroupNode node) {
        return isDefaultNode(taskGroupName, node) || hasSameName(taskGroupName, node);
    }

    private boolean isDefaultNode(Maybe<String> taskGroupName, TaskGroupNode node) {
        return node.getGroup().equals(GROUP_NAME_FOR_GROUPLESS_TASKS) && (!taskGroupName.isPresent() || taskGroupName.get() == null);
    }

    private boolean hasSameName(Maybe<String> taskGroupName, TaskGroupNode node) {
        return taskGroupName.isPresent() && taskGroupName.get() != null && taskGroupName.get().equals(node.getGroup());
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

    /**
     * Refreshes the task view when invoked, regardless of whether the underlying operation was successful or not.
     */
    private static final class RefreshTasksViewAfterJobDone extends JobChangeAdapter {

        private final TaskView taskView;

        private RefreshTasksViewAfterJobDone(TaskView taskView) {
            this.taskView = Preconditions.checkNotNull(taskView);
        }

        @Override
        public void done(IJobChangeEvent event) {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    RefreshTasksViewAfterJobDone.this.taskView.refresh();
                }
            });
        }
    }

}
