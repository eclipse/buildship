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

package org.eclipse.buildship.ui.internal.util.nodeselection;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;

import org.eclipse.buildship.ui.internal.view.NoActionsAvailableAction;

/**
 * Adds {@link SelectionSpecificAction} instances as menu items to the context menu of the provided
 * menu manager, after querying each action if it should be shown for the current selection.
 *
 * @see SelectionSpecificAction#isVisibleFor(NodeSelection)
 */
public final class ActionShowingContextMenuListener implements IMenuListener {

    private final NodeSelectionProvider selectionProvider;
    private final ImmutableList<SelectionSpecificAction> actions;
    private final ImmutableList<? extends SelectionSpecificAction> actionsPrecededBySeparator;
    private final ImmutableList<? extends SelectionSpecificAction> actionsSucceededBySeparator;

    public ActionShowingContextMenuListener(NodeSelectionProvider selectionProvider, List<? extends SelectionSpecificAction> actions,
                                            List<? extends SelectionSpecificAction> actionsPrecededBySeparator, List<? extends SelectionSpecificAction> actionsSucceededBySeparator) {
        this.selectionProvider = Preconditions.checkNotNull(selectionProvider);
        this.actions = ImmutableList.copyOf(actions);
        this.actionsPrecededBySeparator = ImmutableList.copyOf(actionsPrecededBySeparator);
        this.actionsSucceededBySeparator = ImmutableList.copyOf(actionsSucceededBySeparator);
    }

    @Override
    public void menuAboutToShow(IMenuManager manager) {
        NodeSelection selection = this.selectionProvider.getSelection();
        handleSelection(manager, selection);
    }

    private void handleSelection(IMenuManager manager, NodeSelection selection) {
        boolean isContextMenuEmpty = true;
        for (SelectionSpecificAction action : this.actions) {
            if (action.isVisibleFor(selection)) {
                isContextMenuEmpty = false;
                // add preceding separator if requested
                if (this.actionsPrecededBySeparator.contains(action)) {
                    manager.add(new Separator());
                }

                // enable / add action
                action.setEnabledFor(selection);
                manager.add(action);

                // add succeeding separator if requested
                if (this.actionsSucceededBySeparator.contains(action)) {
                    manager.add(new Separator());
                }
            }
        }

        if (isContextMenuEmpty) {
            manager.add(new NoActionsAvailableAction());
        }
    }

}
