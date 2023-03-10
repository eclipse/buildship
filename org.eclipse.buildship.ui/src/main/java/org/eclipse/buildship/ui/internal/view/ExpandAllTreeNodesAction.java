/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
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

import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;
import org.eclipse.buildship.ui.internal.i18n.UiMessages;
import org.eclipse.buildship.ui.internal.util.nodeselection.NodeSelection;
import org.eclipse.buildship.ui.internal.util.nodeselection.SelectionSpecificAction;

/**
 * Expands all the nodes under the selected node or the entire tree if no node is selected.
 */
public final class ExpandAllTreeNodesAction extends Action implements SelectionSpecificAction {

    private final AbstractTreeViewer treeViewer;

    public ExpandAllTreeNodesAction(AbstractTreeViewer treeViewer) {
        this.treeViewer = Preconditions.checkNotNull(treeViewer);

        setToolTipText(UiMessages.Action_ExpandNodes_Tooltip);
        setImageDescriptor(PluginImages.EXPAND_ALL.withState(ImageState.ENABLED).getImageDescriptor());
    }

    @Override
    public void run() {
        this.treeViewer.expandAll();
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
