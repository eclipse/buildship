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

package org.eclipse.buildship.ui.generic;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;

/**
 * Adds {@link SelectionSpecificAction} instances as menu items to the context menu of a given
 * {@link TreeViewer} instance, after querying each action if it should be shown for the current
 * selection.
 *
 * @see SelectionSpecificAction#isVisibleFor(NodeSelection)
 */
public final class ActionShowingContextMenuListener implements IMenuListener {

    private final NodeSelectionProvider nodeSelectionProvider;
    private final ImmutableList<SelectionSpecificAction> actions;

    public ActionShowingContextMenuListener(NodeSelectionProvider nodeSelectionProvider, List<? extends SelectionSpecificAction> actions) {
        this.nodeSelectionProvider = Preconditions.checkNotNull(nodeSelectionProvider);
        this.actions = ImmutableList.copyOf(actions);
    }

    @Override
    public void menuAboutToShow(IMenuManager manager) {
        NodeSelection selection = this.nodeSelectionProvider.getNodeSelection();
        handleSelection(manager, selection);
    }

    private void handleSelection(IMenuManager manager, NodeSelection selection) {
        for (SelectionSpecificAction action : this.actions) {
            if (action.isVisibleFor(selection)) {
                action.setEnabled(action.isEnabledFor(selection));
                manager.add(action);
            }
        }
    }

}
