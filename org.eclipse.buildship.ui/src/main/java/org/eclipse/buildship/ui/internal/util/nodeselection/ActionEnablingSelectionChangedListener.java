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

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * Enables {@link SelectionSpecificAction} instances, after querying each action if it should be
 * enabled for the current selection.
 *
 * @see SelectionSpecificAction#isEnabledFor(NodeSelection)
 */
public final class ActionEnablingSelectionChangedListener implements ISelectionChangedListener {

    private final NodeSelectionProvider selectionProvider;
    private final ImmutableList<SelectionSpecificAction> actions;

    public ActionEnablingSelectionChangedListener(NodeSelectionProvider selectionProvider, List<? extends SelectionSpecificAction> actions) {
        this.selectionProvider = Preconditions.checkNotNull(selectionProvider);
        this.actions = ImmutableList.copyOf(actions);

        // initialize the actions based on the current selection
        handleSelection(this.selectionProvider.getSelection());
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        handleSelection(this.selectionProvider.getSelection());
    }

    private void handleSelection(NodeSelection selection) {
        for (SelectionSpecificAction action : this.actions) {
            action.setEnabledFor(selection);
        }
    }

}
