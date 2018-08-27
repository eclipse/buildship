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

import com.google.common.base.Preconditions;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.ui.internal.util.nodeselection.NodeSelection;

/**
 * Runs the selected Gradle tasks.
 * <p>
 * Note: double-click events are generated both when double-clicking on a node and when pressing the <i>Enter</i> key.
 */
public final class TreeViewerDoubleClickListener implements IDoubleClickListener {

    private final String commandId;
    private final TreeViewer treeViewer;

    public TreeViewerDoubleClickListener(String commandId, TreeViewer treeViewer) {
        this.commandId = Preconditions.checkNotNull(commandId);
        this.treeViewer = Preconditions.checkNotNull(treeViewer);
    }

    @Override
    public void doubleClick(DoubleClickEvent event) {
        NodeSelection nodeSelection = NodeSelection.from(this.treeViewer.getSelection());
        if (isEnabledFor(nodeSelection)) {
            run();
        } else if (nodeSelection.isSingleSelection()) {
            Object selected = nodeSelection.toList().get(0);
            IContentProvider provider = this.treeViewer.getContentProvider();
            if (provider instanceof ITreeContentProvider && ((ITreeContentProvider) provider).hasChildren(selected)) {
                if (this.treeViewer.getExpandedState(selected)) {
                    this.treeViewer.collapseToLevel(selected, AbstractTreeViewer.ALL_LEVELS);
                } else {
                    this.treeViewer.expandToLevel(selected, 1);
                }
            }
        }
    }

    private boolean isEnabledFor(NodeSelection node) {
        return TaskViewActionStateRules.taskScopedTaskExecutionActionsEnablement(node).asBoolean();
    }

    private void run() {
        try {
            getHandlerService().executeCommand(this.commandId, null);
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot execute command '%s'.", this.commandId), e);
        }
    }

    @SuppressWarnings({"cast", "RedundantCast"})
    private IHandlerService getHandlerService() {
        return (IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IHandlerService.class);
    }

}
