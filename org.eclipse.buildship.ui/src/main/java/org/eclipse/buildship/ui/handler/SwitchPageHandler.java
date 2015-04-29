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

import com.google.common.primitives.Ints;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.buildship.ui.view.executionview.AbstractPagePart;
import org.eclipse.buildship.ui.view.pages.IPage;

public class SwitchPageHandler extends AbstractHandler {

    public static final String SWITCH_PAGE_COMMAND_ID = "org.eclipse.buildship.ui.commands.switchpage"; //$NON-NLS-1$
    public static final String PAGE_ID_PARAM = "org.eclipse.buildship.ui.commandParameters.pageid"; //$NON-NLS-1$

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
        if (activePart instanceof AbstractPagePart) {
            AbstractPagePart pagePart = (AbstractPagePart) activePart;
            List<IPage> pages = pagePart.getPages();
            int size = pages.size();

            // handle given page parameter
            String pageIdParameter = event.getParameter(PAGE_ID_PARAM);
            if (pageIdParameter != null) {
                Integer pageId = Ints.tryParse(pageIdParameter);
                if (pageId != null && pageId.intValue() < size) {
                    pagePart.setCurrentPage(pages.get(pageId.intValue()));
                    return Status.OK_STATUS;
                } else {
                    throw new ExecutionException("The PAGE_ID_PARAM does not match for any attached page.");
                }
            }

            // handle without page parameter
            if (size > 0) {
                IPage currentPage = pagePart.getCurrentPage();
                int indexOf = pages.indexOf(currentPage);
                if (indexOf < pages.size() - 1) {
                    pagePart.setCurrentPage(pages.get(indexOf + 1));
                } else {
                    pagePart.setCurrentPage(pages.get(0));
                }
            }
        }

        return Status.OK_STATUS;
    }

}
