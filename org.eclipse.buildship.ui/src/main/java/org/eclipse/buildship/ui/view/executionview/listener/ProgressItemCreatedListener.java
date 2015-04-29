/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.view.executionview.listener;

import com.google.common.eventbus.Subscribe;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

import org.eclipse.buildship.ui.view.executionview.model.internal.ExecutionItemCreatedEvent;

/**
 * This class listens to {@link ExecutionItemCreatedEvent} events and expands the TreeViewer, so
 * that every new tree element is directly visible.
 */
public class ProgressItemCreatedListener {

    private TreeViewer viewer;
    private Display display;

    public ProgressItemCreatedListener(TreeViewer viewer) {
        this.viewer = viewer;
        this.display = viewer.getControl().getDisplay();
    }

    @Subscribe
    public void progressItemCreated(ExecutionItemCreatedEvent progressItemCreatedEvent) {
        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                viewer.expandAll();
            }
        });
    }
}
