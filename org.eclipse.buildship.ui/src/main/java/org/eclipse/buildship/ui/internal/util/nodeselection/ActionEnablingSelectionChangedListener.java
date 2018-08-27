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
