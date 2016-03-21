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

package org.eclipse.buildship.ui.view.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.ui.i18n.UiMessages;
import org.eclipse.buildship.ui.view.MultiPageView;
import org.eclipse.buildship.ui.view.TreeViewerState;

/**
 * An action on the {@link ExecutionsView} to toggle whether to show/hide the tree header on all
 * pages.
 */
public final class ToggleShowTreeHeaderAction extends Action {

    private final MultiPageView multiPageView;
    private final TreeViewerState treeViewerState;

    public ToggleShowTreeHeaderAction(MultiPageView multiPageView, TreeViewerState treeViewerState) {
        super(null, AS_CHECK_BOX);
        this.multiPageView = Preconditions.checkNotNull(multiPageView);
        this.treeViewerState = Preconditions.checkNotNull(treeViewerState);

        setText(UiMessages.Action_ShowTreeHeader_Text);
        setChecked(this.treeViewerState.isShowTreeHeader());

        updateHeaderVisibility();
    }

    @Override
    public void run() {
        this.treeViewerState.setShowTreeHeader(isChecked());
        updateHeaderVisibility();
    }

    private void updateHeaderVisibility() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                boolean showTreeHeader = ToggleShowTreeHeaderAction.this.treeViewerState.isShowTreeHeader();
                for (ExecutionPage executionPage : FluentIterable.from(ToggleShowTreeHeaderAction.this.multiPageView.getPages()).filter(ExecutionPage.class)) {
                    Tree tree = executionPage.getPageControl().getViewer().getTree();
                    if (!tree.isDisposed()) {
                        tree.setHeaderVisible(showTreeHeader);
                    }
                }
            }
        });
    }

}
