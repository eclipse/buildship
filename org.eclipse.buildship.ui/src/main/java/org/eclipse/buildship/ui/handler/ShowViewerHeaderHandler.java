/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.buildship.ui.util.viewer.ViewerUtils;
import org.eclipse.buildship.ui.view.ViewerPart;
import org.eclipse.buildship.ui.view.executionview.ExecutionPartPreferences;

public class ShowViewerHeaderHandler extends AbstractToogleStateHandler {

    ExecutionPartPreferences prefs = new ExecutionPartPreferences();

    @Override
    protected boolean getToggleState() {
        return prefs.getHeaderVisibile();
    }

    @Override
    protected Object doExecute(ExecutionEvent event) {

        // invert current header visible state respectively the togglestate
        prefs.setHeaderVisibile(!getToggleState());

        IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
        if (activePart instanceof ViewerPart) {
            ViewerPart viewerPart = (ViewerPart) activePart;
            ViewerUtils.setHeaderVisible(viewerPart.getViewer(), getToggleState());
        }

        return Status.OK_STATUS;
    }

}
