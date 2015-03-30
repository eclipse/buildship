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

package com.gradleware.tooling.eclipse.ui.generic;

import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.TreeViewer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Adds {@link SelectionSpecificAction} instances as menu items to the context menu of a given
 * {@link TreeViewer} instance, after querying each action if it should be shown for the current
 * selection.
 *
 * @see SelectionSpecificAction#isVisibleFor(NodeSelection)
 */
public final class ActionShowingContextMenuListener implements IMenuListener {

    private final TreeViewer treeViewer;
    private final ImmutableList<SelectionSpecificAction> actions;

    public ActionShowingContextMenuListener(TreeViewer treeViewer, List<? extends SelectionSpecificAction> actions) {
        this.treeViewer = Preconditions.checkNotNull(treeViewer);
        this.actions = ImmutableList.copyOf(actions);
    }

    @Override
    public void menuAboutToShow(IMenuManager manager) {
        NodeSelection selection = NodeSelection.from(this.treeViewer.getSelection());
        handleSelection(manager, selection);
    }

    private void handleSelection(IMenuManager manager, NodeSelection selection) {
        for (SelectionSpecificAction action : this.actions) {
            if (action.isVisibleFor(selection)) {
                manager.add(action);
            }
        }
    }

}
