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

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.buildship.ui.part.execution.AbstractPagePart;
import org.eclipse.buildship.ui.part.execution.ExecutionPartPreferences;
import org.eclipse.buildship.ui.part.pages.IPage;
import org.eclipse.buildship.ui.util.viewer.ViewerUtils;

public class ShowTreeFilterHandler extends AbstractToogleStateHandler {

    ExecutionPartPreferences prefs = new ExecutionPartPreferences();

    @Override
    protected boolean getToggleState() {
        return prefs.getFilterVisibile();
    }

    @Override
    protected Object doExecute(ExecutionEvent event) {

        // invert current filter visible state respectively the togglestate
        prefs.setFilterVisibile(!getToggleState());

        IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
        ViewerUtils.setShowFilterControls(activePart, getToggleState());

        if (activePart instanceof AbstractPagePart) {
            AbstractPagePart pagePart = (AbstractPagePart) activePart;
            List<IPage> pages = pagePart.getPages();
            for (IPage page : pages) {
                ViewerUtils.setShowFilterControls(page, getToggleState());
            }
        }

        return Status.OK_STATUS;
    }

}
