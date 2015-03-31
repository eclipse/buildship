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

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Enables {@link SelectionSpecificAction} instances, after querying each action if it should be
 * enabled for the current selection.
 *
 * @see SelectionSpecificAction#isEnabledFor(NodeSelection)
 */
public final class ActionEnablingSelectionChangedListener implements ISelectionChangedListener {

    private final TreeViewer treeViewer;
    private final ImmutableList<SelectionSpecificAction> actions;

    public ActionEnablingSelectionChangedListener(TreeViewer treeViewer, List<? extends SelectionSpecificAction> actions) {
        this.treeViewer = Preconditions.checkNotNull(treeViewer);
        this.actions = ImmutableList.copyOf(actions);

        // initialize the actions based on the current selection
        handleSelection(NodeSelection.from(this.treeViewer.getSelection()));
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        NodeSelection selection = NodeSelection.from(this.treeViewer.getSelection());
        handleSelection(selection);
    }

    private void handleSelection(NodeSelection selection) {
        for (SelectionSpecificAction action : this.actions) {
            action.setEnabled(action.isEnabledFor(selection));
        }
    }

}
