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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;

/**
 * Listens for the default Eclipse refresh command.
 */
public class RefreshListener implements IExecutionListener {

    @Override
    public void notHandled(String commandId, NotHandledException exception) {
    }

    @Override
    public void postExecuteFailure(String commandId, ExecutionException exception) {
    }

    @Override
    public void postExecuteSuccess(String commandId, Object returnValue) {
    }

    @Override
    public void preExecute(String commandId, ExecutionEvent event) {
        if (commandId.equals("org.eclipse.ui.file.refresh")
                && ((IContextService) PlatformUI.getWorkbench().getService(IContextService.class)).getActiveContextIds().contains(UiPluginConstants.GRADLE_NATURE_CONTEXT_ID)) {
            GradleClasspathContainerRefresher.refresh(event);
        }
    }

}
