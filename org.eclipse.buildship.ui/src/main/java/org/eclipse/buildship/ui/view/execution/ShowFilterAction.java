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

package org.eclipse.buildship.ui.view.execution;

import org.eclipse.jface.action.Action;

import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.viewer.FilteredTree;

/**
 * Action for the {@link ExecutionPage} showing/hiding the filter control for the execution tree.
 */
public class ShowFilterAction extends Action {

    private ExecutionPage page;

    public ShowFilterAction(ExecutionPage page) {
        super(ExecutionsViewMessages.Action_ShowFilter_Text, AS_CHECK_BOX);
        this.page = page;
        setImageDescriptor(PluginImages.FILTER_EXECUTION.withState(ImageState.ENABLED).getImageDescriptor());
    }

    @Override
    public void run() {
        FilteredTree filteredTree = this.page.getPageControl();
        boolean showFilterControls = !filteredTree.isShowFilterControls();
        filteredTree.setShowFilterControls(showFilterControls);

        // if the filter was not visible then set the focus on it,
        // if it was not visible clear the filter text to display all nodes
        if (filteredTree.isShowFilterControls()) {
            filteredTree.getFilterControl().setFocus();
        } else {
            filteredTree.getFilterControl().setText(""); //$NON-NLS-1$
        }
    }

}
