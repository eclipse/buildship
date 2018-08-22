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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.ui.internal.util.selection.SelectionUtils;

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
            ImmutableList<IProject> projects = collectProjectNodesToSelect(structuredSelection.toList());
            updateViewSelection(projects);
        }
    }

    private ImmutableList<IProject> collectProjectNodesToSelect(List<?> selectedNodes) {
        ImmutableList.Builder<IProject> projects = ImmutableList.builder();
        for (Object selectedNode : selectedNodes) {
            if (selectedNode instanceof BaseProjectNode) {
                Optional<IProject> project = ((BaseProjectNode) selectedNode).getWorkspaceProject();
                if (project.isPresent()) {
                    projects.add(project.get());
                }
            } else if (selectedNode instanceof TaskNode) {
                Optional<IProject> project = ((TaskNode) selectedNode).getParentProjectNode().getWorkspaceProject();
                if (project.isPresent()) {
                    projects.add(project.get());
                }
            }
        }
        return projects.build();
    }

    private void updateViewSelection(List<IProject> projects) {
        SelectionUtils.selectAndReveal(projects, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
    }

}
