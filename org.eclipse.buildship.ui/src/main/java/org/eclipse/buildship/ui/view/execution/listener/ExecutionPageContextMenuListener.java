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

package org.eclipse.buildship.ui.view.execution.listener;

import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.test.JvmTestOperationDescriptor;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.buildship.ui.generic.CollapseTreeNodesAction;
import org.eclipse.buildship.ui.generic.ExpandTreeNodesAction;
import org.eclipse.buildship.ui.generic.GotoTestElementAction;
import org.eclipse.buildship.ui.view.execution.OperationItem;

/**
 * IMenuListener for generating the context menu of an ExecutionPage.
 *
 */
public class ExecutionPageContextMenuListener implements IMenuListener {

    private AbstractTreeViewer treeViewer;

    public ExecutionPageContextMenuListener(AbstractTreeViewer treeViewer) {
        this.treeViewer = treeViewer;
    }

    @Override
    public void menuAboutToShow(IMenuManager manager) {
        manager.add(new Separator("NavigationActions"));

        if (shouldAddGotoTestElementAction()) {
            manager.add(new GotoTestElementAction(treeViewer, treeViewer.getControl().getDisplay()));
        }

        manager.add(new Separator("TreeActions"));

        manager.add(new ExpandTreeNodesAction(treeViewer));
        manager.add(new CollapseTreeNodesAction(treeViewer));
    }

    private boolean shouldAddGotoTestElementAction() {
        ISelection selection = treeViewer.getSelection();
        if (selection instanceof IStructuredSelection) {
            Object firstElement = ((IStructuredSelection) selection).getFirstElement();
            if (firstElement instanceof OperationItem) {
                OperationDescriptor adapter = (OperationDescriptor) ((OperationItem) firstElement).getAdapter(OperationDescriptor.class);
                return adapter instanceof JvmTestOperationDescriptor && ((JvmTestOperationDescriptor) adapter).getClassName() != null;
            }
        }
        return false;
    }

}
