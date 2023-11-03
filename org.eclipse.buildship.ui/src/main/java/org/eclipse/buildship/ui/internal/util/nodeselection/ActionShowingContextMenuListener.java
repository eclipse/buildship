/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
