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

import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;
import org.eclipse.buildship.ui.internal.i18n.UiMessages;
import org.eclipse.buildship.ui.internal.util.nodeselection.NodeSelection;
import org.eclipse.buildship.ui.internal.util.nodeselection.SelectionSpecificAction;
import org.eclipse.buildship.ui.internal.util.widget.FilteredTree;

/**
 * Toggles the filter widget in the {@link FilteredTree}.
 */
public final class ShowFilterAction extends Action implements SelectionSpecificAction {

    private final FilteredTree filteredTree;

    public ShowFilterAction(FilteredTree filteredTree) {
        super(null, AS_CHECK_BOX);
        this.filteredTree = Preconditions.checkNotNull(filteredTree);

        setToolTipText(UiMessages.Action_ShowFilter_Tooltip);
        setImageDescriptor(PluginImages.FILTER_EXECUTION.withState(ImageState.ENABLED).getImageDescriptor());
        setChecked(false);
    }

    @Override
    public void run() {
        // toggle filter
        this.filteredTree.setShowFilterControls(!this.filteredTree.isShowFilterControls());

        // if the filter has become visible set the focus on it
        // if the filter has disappeared clear the filter text to display all nodes
        if (this.filteredTree.isShowFilterControls()) {
            this.filteredTree.getFilterControl().setFocus();
        } else {
            this.filteredTree.getFilterControl().setText(""); //$NON-NLS-1$
        }
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
