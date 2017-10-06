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

package org.eclipse.buildship.ui.util.action;

import com.google.common.base.Preconditions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.ui.UiPlugin;

/**
 * Base class for {@link Action} instances that invoke a {@link org.eclipse.core.commands.Command}
 * instance via its command id when the action is triggered.
 */
public abstract class CommandBackedAction extends Action {

    private final String commandId;

    protected CommandBackedAction(String commandId) {
        this(commandId, IAction.AS_UNSPECIFIED);
    }

    protected CommandBackedAction(String commandId, int style) {
        super(null, style);
        this.commandId = Preconditions.checkNotNull(commandId);
    }

    @Override
    public void runWithEvent(Event event) {
        try {
            getHandlerService().executeCommand(this.commandId, event);
        } catch (Exception e) {
            String message = String.format("Cannot execute command for action '%s'.", getText());
            UiPlugin.logger().error(message, e);
            throw new GradlePluginsRuntimeException(message, e);
        }
    }

    @SuppressWarnings({"cast", "RedundantCast"})
    private IHandlerService getHandlerService() {
        return (IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IHandlerService.class);
    }

}
