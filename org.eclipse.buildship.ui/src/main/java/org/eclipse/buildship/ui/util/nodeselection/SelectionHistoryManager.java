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

package org.eclipse.buildship.ui.util.nodeselection;

import com.google.common.base.Preconditions;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Stores the sequence in which the currently selected nodes were selected in the {@link TreeViewer}.
 */
public final class SelectionHistoryManager {

    private final TreeViewer treeViewer;
    private final TreeViewerSelectionListener listener;
    private NodeSelection selectionHistory;

    public SelectionHistoryManager(TreeViewer treeViewer) {
        this.treeViewer = Preconditions.checkNotNull(treeViewer);
        this.listener = new TreeViewerSelectionListener();
        this.selectionHistory = NodeSelection.empty();

        init();
    }

    private void init() {
        this.treeViewer.addSelectionChangedListener(this.listener);
    }

    public NodeSelection getSelectionHistory() {
        return this.selectionHistory;
    }

    private void handleSelection(IStructuredSelection selection) {
        NodeSelection nodeSelection = NodeSelection.from(selection);
        this.selectionHistory = this.selectionHistory.mergeWith(nodeSelection);
    }

    public void dispose() {
        this.treeViewer.removeSelectionChangedListener(this.listener);
    }

    /**
     * {@code ISelectionChangedListener} that, for each selection change in the tree viewer, updates the selection history accordingly.
     */
    private final class TreeViewerSelectionListener implements ISelectionChangedListener {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            ISelection selection = event.getSelection();
            if (selection instanceof IStructuredSelection) {
                handleSelection((IStructuredSelection) selection);
            }
        }

    }

}
