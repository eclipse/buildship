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

import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.buildship.ui.part.FilteredTreeProvider;
import org.eclipse.buildship.ui.viewer.FilteredTree;

/**
 * This action is used to toggle the visibility of the filter controls of a {@link FilteredTree},
 * which is provided by a {@link FilteredTreeProvider}.
 *
 */
public class ShowFilterControlsAction extends Action {

    public static final String ID = "org.eclipse.buildship.ui.actions.ShowFilterControlsAction"; //$NON-NLS-1$

    private FilteredTreeProvider filteredTreeProvider;

    public ShowFilterControlsAction(FilteredTreeProvider filteredTreeProvider) {
        super("Filter", AS_CHECK_BOX);
        this.filteredTreeProvider = filteredTreeProvider;
        setId(ID);
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        URL filterImageUrl = FileLocator.find(bundle, new Path("icons/full/elcl16/filter.png"), null); //$NON-NLS-1$
        setImageDescriptor(ImageDescriptor.createFromURL(filterImageUrl));
    }

    @Override
    public void run() {
        FilteredTree filteredTree = filteredTreeProvider.getFilteredTree();
        if (filteredTree != null) {
            boolean showFilterControls = !filteredTree.isShowFilterControls();
            filteredTree.setShowFilterControls(showFilterControls);
            if (filteredTree.isShowFilterControls()) {
                filteredTree.getFilterControl().setFocus();
            } else {
                filteredTree.getFilterControl().setText(""); //$NON-NLS-1$
            }
        }
    }

}
