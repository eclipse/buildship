/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view;

import com.google.common.base.Preconditions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.ui.internal.i18n.UiMessages;
import org.eclipse.buildship.ui.internal.util.nodeselection.NodeSelection;
import org.eclipse.buildship.ui.internal.util.nodeselection.SelectionSpecificAction;

/**
 * Collapses all the nodes under the selected node or the entire tree if no node is selected.
 */
public final class CollapseAllTreeNodesAction extends Action implements SelectionSpecificAction {

    private final AbstractTreeViewer treeViewer;

    public CollapseAllTreeNodesAction(AbstractTreeViewer treeViewer) {
        this.treeViewer = Preconditions.checkNotNull(treeViewer);

        setToolTipText(UiMessages.Action_CollapseNodes_Tooltip);
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_COLLAPSEALL));
    }

    @Override
    public void run() {
        this.treeViewer.collapseAll();
    }

    @Override
    public boolean isVisibleFor(NodeSelection selection) {
        return true;
    }

    @Override
    public boolean isEnabledFor(NodeSelection selection) {
        return true;
    }

    @Override
    public void setEnabledFor(NodeSelection selection) {
        setEnabled(true);
    }

}
