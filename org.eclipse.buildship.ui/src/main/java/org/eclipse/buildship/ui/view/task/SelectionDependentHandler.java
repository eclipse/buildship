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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.buildship.ui.generic.NodeSelection;

/**
 * Base class for all {@code AbstractHandler} classes that contain execution logic that depends on the current selection.
 */
 abstract class SelectionDependentHandler extends AbstractHandler {

    @Override
    public void setEnabled(Object evaluationContext) {
        boolean enabled;
        TaskView taskView = getTaskView();
        if (taskView != null) {
            NodeSelection selectionHistory = taskView.getSelection();
            enabled = isEnabledFor(selectionHistory);
        } else {
            enabled = false;
        }
        setBaseEnabled(enabled);
    }

    protected NodeSelection getSelectionHistory(ExecutionEvent event) {
        TaskView taskView = getTaskView(event);
        if (taskView == null) {
            throw new IllegalStateException(String.format("Cannot execute command '%s' in current window.", getCommandName(event)));
        }

        NodeSelection selectionHistory = taskView.getSelection();
        if (!isEnabledFor(selectionHistory)) {
            throw new IllegalStateException(String.format("Cannot execute command '%s' for current selection.", getCommandName(event)));
        }

        return selectionHistory;
    }

    protected abstract boolean isEnabledFor(NodeSelection selection);

    protected TaskView getTaskView() {
        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (workbenchWindow != null) {
            return (TaskView) workbenchWindow.getActivePage().findView(TaskView.ID);
        }
        return null;
    }

    protected TaskView getTaskView(ExecutionEvent event) {
        IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
        if (workbenchWindow != null) {
            return (TaskView) workbenchWindow.getActivePage().findView(TaskView.ID);
        }
        return null;
    }

    protected String getCommandName(ExecutionEvent event) {
        Command command = event.getCommand();
        try {
            return command.getName();
        } catch (NotDefinedException e) {
            return "Unknown";
        }
    }

}
