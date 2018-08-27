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


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.buildship.core.internal.workspace.FetchStrategy;

/**
 * A command handler on the {@link TaskView} to reload/refresh the content of the task view.
 */
public final class RefreshViewHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) {
        IViewPart taskView = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().findView(TaskView.ID);
        if (taskView != null) {
            ((TaskView) taskView).reload(FetchStrategy.FORCE_RELOAD);
        }

        // todo (etst) disable the Refresh button while the model updates are in progress
        return null;
    }

}
