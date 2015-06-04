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

import com.google.common.base.Preconditions;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.generic.NodeSelection;

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
        NodeSelection selection = NodeSelection.from(this.treeViewer.getSelection());
        if (isEnabledFor(selection)) {
            run();
        }
    }

    private boolean isEnabledFor(NodeSelection node) {
        return TaskViewActionStateRules.taskScopedTaskExecutionActionsEnabledFor(node) ||
                TaskViewActionStateRules.projectScopedTaskExecutionActionsEnabledFor(node);
    }

    private void run() {
        try {
            getHandlerService().executeCommand(this.commandId, null);
        } catch (Exception e) {
            String message = String.format("Cannot execute command '%s'.", this.commandId);
            UiPlugin.logger().error(message, e);
            throw new GradlePluginsRuntimeException(message, e);
        }
    }

    @SuppressWarnings("cast")
    private IHandlerService getHandlerService() {
        return (IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IHandlerService.class);
    }

}
