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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.buildship.ui.part.ViewerProvider;

public class ExpandHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
        if (activePart instanceof ViewerProvider) {
            ViewerProvider viewerPart = (ViewerProvider) activePart;
            Viewer viewer = viewerPart.getViewer();
            if (viewer instanceof TreeViewer) {

                TreeViewer treeViewer = (TreeViewer) viewer;

                ISelection selection = HandlerUtil.getCurrentSelection(event);
                if (!selection.isEmpty() && selection instanceof TreeSelection) {
                    TreeSelection treeSelection = (TreeSelection) selection;
                    List<?> elements = treeSelection.toList();
                    for (Object element : elements) {
                        treeViewer.expandToLevel(element, TreeViewer.ALL_LEVELS);
                    }
                } else {
                    treeViewer.expandAll();
                }
            }
        }

        return Status.OK_STATUS;
    }
}
