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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.PlatformUI;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.eclipse.buildship.ui.util.selection.SelectionUtils;

/**
 * Links the selection from the {@link TaskView} to the workspace.
 * <p>
 * If a task or project node is selected in the task view, the corresponding {@link IProject} is
 * selected everywhere in the UI.
 */
public final class TreeViewerSelectionChangeListener implements ISelectionChangedListener {

    private final TaskView taskView;

    public TreeViewerSelectionChangeListener(TaskView taskView) {
        this.taskView = Preconditions.checkNotNull(taskView);
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        // update the viewers if 'linked to selection' is enabled
        if (this.taskView.getState().isLinkToSelection()) {
            findAndUpdateViewSelections(event.getSelection());
        }
    }

    private void findAndUpdateViewSelections(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            ImmutableList<ProjectNode> projectNodes = collectProjectNodesToSelect(structuredSelection.toList());
            ImmutableList<IProject> projects = collectProjectsToSelect(projectNodes);
            updateViewSelection(projects);
        }
    }

    private ImmutableList<ProjectNode> collectProjectNodesToSelect(List<?> selectedNodes) {
        ImmutableList.Builder<ProjectNode> projectNodes = ImmutableList.builder();
        for (Object selectedNode : selectedNodes) {
            if (selectedNode instanceof ProjectNode) {
                ProjectNode projectNode = (ProjectNode) selectedNode;
                projectNodes.add(projectNode);
            } else if (selectedNode instanceof TaskNode) {
                TaskNode taskNode = (TaskNode) selectedNode;
                projectNodes.add(taskNode.getParentProjectNode());
            }
        }
        return projectNodes.build();
    }

    private ImmutableList<IProject> collectProjectsToSelect(List<ProjectNode> projectNodes) {
        ImmutableList.Builder<IProject> projects = ImmutableList.builder();
        for (ProjectNode projectNode : projectNodes) {
            Optional<IProject> workspaceProject = projectNode.getWorkspaceProject();
            if (workspaceProject.isPresent()) {
                projects.add(workspaceProject.get());
            }
        }
        return projects.build();
    }

    private void updateViewSelection(List<IProject> projects) {
        SelectionUtils.selectAndReveal(projects, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
    }

}
