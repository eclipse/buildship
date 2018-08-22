/*
 * Copyright (c) 2015 the original author or authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Wayne Beaton (The Eclipse Foundation) - Bug 463693
 */

package org.eclipse.buildship.ui.internal.view.task;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Links the selection from the workspace to the {@link TaskView}.
 * <p>
 * If an {@link IProject} object is selected anywhere in the UI - like in the project explorer - the
 * corresponding node is selected in the task view.
 */
public final class WorkbenchSelectionListener implements ISelectionListener {

    private final TaskView taskView;

    public WorkbenchSelectionListener(TaskView taskView) {
        this.taskView = Preconditions.checkNotNull(taskView);
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (this.taskView.getState().isLinkToSelection() && part != this.taskView) {
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                if (!structuredSelection.isEmpty()) {
                    selectionChanged(structuredSelection);
                }
            }
        }
    }

    private void selectionChanged(IStructuredSelection structuredSelection) {
        ImmutableList<IProject> projects = convertToProjects(ImmutableList.copyOf(structuredSelection.toArray()));
        selectProjectsInTree(projects);
    }

    private ImmutableList<IProject> convertToProjects(List<Object> selections) {
        return FluentIterable.from(selections).transform(new Function<Object, IProject>() {

            @Override
            public IProject apply(Object input) {
                if (input instanceof IProject) {
                    return (IProject) input;
                } else if (input instanceof IJavaElement) {
                    return ((IJavaElement) input).getJavaProject().getProject();
                } else if (input instanceof IResource) {
                    return ((IResource) input).getProject();
                } else {
                    return null;
                }
            }
        }).filter(Predicates.notNull()).toList();
    }

    private void selectProjectsInTree(List<IProject> projects) {
        Builder<TreeItem> selection = ImmutableList.builder();

        Tree tree = this.taskView.getTreeViewer().getTree();
        for (TreeItem treeItem : tree.getItems()) {
            Object data = treeItem.getData();
            if (data instanceof BaseProjectNode) {
                BaseProjectNode selectedNode = (BaseProjectNode) data;
                Optional<IProject> workspaceProject = selectedNode.getWorkspaceProject();
                if (workspaceProject.isPresent() && projects.contains(workspaceProject.get())) {
                    selection.add(treeItem);
                }
            }
        }

        ImmutableList<TreeItem> treeSelection = selection.build();
        tree.setSelection(treeSelection.toArray(new TreeItem[treeSelection.size()]));
    }

}
