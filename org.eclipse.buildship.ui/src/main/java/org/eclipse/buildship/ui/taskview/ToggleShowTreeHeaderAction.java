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

package org.eclipse.buildship.ui.taskview;

import com.google.common.base.Preconditions;

import org.eclipse.buildship.ui.part.TreeViewerState;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;

/**
 * An action on the {@link TaskView} to toggle whether to show/hide the tree header.
 */
public final class ToggleShowTreeHeaderAction extends Action {

    private final TreeViewer treeViewer;
    private final TreeViewerState treeViewerState;

    public ToggleShowTreeHeaderAction(TreeViewer treeViewer, TreeViewerState treeViewerState) {
        super(null, AS_CHECK_BOX);
        this.treeViewer = Preconditions.checkNotNull(treeViewer);
        this.treeViewerState = Preconditions.checkNotNull(treeViewerState);

        setText(TaskViewMessages.Action_ShowTreeHeader_Text);
        setChecked(this.treeViewerState.isShowTreeHeader());

        updateHeaderVisibility();
    }

    @Override
    public void run() {
        this.treeViewerState.setShowTreeHeader(isChecked());
        updateHeaderVisibility();
    }

    private void updateHeaderVisibility() {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                Tree tree = ToggleShowTreeHeaderAction.this.treeViewer.getTree();
                tree.setHeaderVisible(ToggleShowTreeHeaderAction.this.treeViewerState.isShowTreeHeader());
            }
        });
    }

}
