/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ian Stewart-Binks (Red Hat Inc.) - Bug 473862 - F5 key shortcut doesn't refresh project folder contents
 */

package org.eclipse.buildship.ui.workspace;

import org.eclipse.buildship.ui.UiPluginConstants;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;

/**
 * Listens for the default Eclipse Refresh command, and triggers a refresh of the project configuration in case
 * the command is triggered in the context of a Gradle project.
 */
public final class RefreshProjectCommandExecutionListener implements IExecutionListener {

    private ExecutionEvent event;

    @Override
    public void preExecute(String commandId, ExecutionEvent event) {
        this.event = event;
    }

    @Override
    public void postExecuteSuccess(String commandId, Object returnValue) {
        // if applicable, call the Gradle project refresh after file refresh
        if (isFileRefreshCommand(commandId) && isGradleNatureContextEnabled()) {
            refreshGradleProject(this.event);
        }
    }

    @Override
    public void postExecuteFailure(String commandId, ExecutionException exception) {
        // if applicable, call the Gradle project refresh even if the file refresh failed
        if (isFileRefreshCommand(commandId) && isGradleNatureContextEnabled()) {
            refreshGradleProject(this.event);
        }
    }

    @Override
    public void notHandled(String commandId, NotHandledException exception) {
        // do nothing
    }

    private boolean isFileRefreshCommand(String commandId) {
        return commandId.equals(IWorkbenchCommandConstants.FILE_REFRESH);
    }

    @SuppressWarnings({"cast", "RedundantCast"})
    private boolean isGradleNatureContextEnabled() {
        IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
        return contextService.getActiveContextIds().contains(UiPluginConstants.GRADLE_NATURE_CONTEXT_ID);
    }

    private void refreshGradleProject(ExecutionEvent event) {
        ProjectSynchronizer.execute(event);
    }

}
