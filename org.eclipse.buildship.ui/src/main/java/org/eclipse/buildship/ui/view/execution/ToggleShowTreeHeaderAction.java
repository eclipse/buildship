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

import java.util.Iterator;

import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.ui.view.TreeViewerState;

/**
 * An action on the {@link ExecutionsView} to toggle whether to show/hide the tree header on all
 * pages.
 */
public final class ToggleShowTreeHeaderAction extends Action {

    private final ExecutionsView view;

    public ToggleShowTreeHeaderAction(ExecutionsView view) {
        super(null, AS_CHECK_BOX);
        this.view = Preconditions.checkNotNull(view);

        setText(ExecutionsViewMessages.Action_ShowTreeHeader_Text);
        setChecked(getState().isShowTreeHeader());

        updateHeaderVisibility();
    }

    private TreeViewerState getState() {
        return this.view.getTreeViewerState();
    }

    @Override
    public void run() {
        getState().setShowTreeHeader(isChecked());
        updateHeaderVisibility();
    }

    private void updateHeaderVisibility() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                boolean showTreeHeader = getState().isShowTreeHeader();
                Iterator<ExecutionPage> pages = FluentIterable.from(ToggleShowTreeHeaderAction.this.view.getPages()).filter(ExecutionPage.class).iterator();
                while (pages.hasNext()) {
                    pages.next().getPageControl().getViewer().getTree().setHeaderVisible(showTreeHeader);
                }
            }
        });
    }

}
