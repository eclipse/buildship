/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - refactoring and integration
*/

package org.eclipse.buildship.ui.handler;

import org.eclipse.buildship.ui.part.ViewerProvider;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import java.util.List;

/**
 * Collapse the selected tree items or all tree items in a {@link ViewerProvider} part.
 */
public final class CollapseHandler extends AbstractHandler {

    @Override
    public void setEnabled(Object evaluationContext) {
        boolean enabled = false;
        if (evaluationContext instanceof IEvaluationContext) {
            Object activePart = ((IEvaluationContext) evaluationContext).getVariable(ISources.ACTIVE_PART_NAME);
            if (activePart instanceof ViewerProvider) {
                ViewerProvider viewerProvider = (ViewerProvider) activePart;
                Viewer viewer = viewerProvider.getViewer();
                enabled = viewer instanceof AbstractTreeViewer;
            }
        }
        setBaseEnabled(enabled);
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
        if (activePart instanceof ViewerProvider) {
            ViewerProvider viewerProvider = (ViewerProvider) activePart;
            Viewer viewer = viewerProvider.getViewer();
            if (viewer instanceof AbstractTreeViewer) {
                AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;
                ISelection selection = HandlerUtil.getCurrentSelection(event);
                if (selection instanceof ITreeSelection && !selection.isEmpty()) {
                    ITreeSelection treeSelection = (ITreeSelection) selection;
                    List<?> elements = treeSelection.toList();
                    for (Object element : elements) {
                        treeViewer.collapseToLevel(element, TreeViewer.ALL_LEVELS);
                    }
                } else {
                    treeViewer.collapseAll();
                }
            }
        }
        return null;
    }

}
