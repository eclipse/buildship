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

package org.eclipse.buildship.ui.view;

import com.google.common.base.Preconditions;

import org.eclipse.jface.action.Action;

import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.external.viewer.FilteredTree;
import org.eclipse.buildship.ui.view.execution.ExecutionsViewMessages;

/**
 * Toggles the filter widget in the {@link FilteredTree}.
 */
public final class ShowFilterAction extends Action {

    private final FilteredTree filteredTree;

    public ShowFilterAction(FilteredTree filteredTree) {
        super(null, AS_CHECK_BOX);
        this.filteredTree = Preconditions.checkNotNull(filteredTree);

        setToolTipText(ExecutionsViewMessages.Action_ShowFilter_Tooltip);
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

}
