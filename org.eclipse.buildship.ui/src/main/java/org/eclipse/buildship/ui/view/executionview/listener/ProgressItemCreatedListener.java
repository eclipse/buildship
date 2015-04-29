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
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.buildship.ui.view.ViewerPart;
import org.eclipse.buildship.ui.view.executionview.model.internal.ExecutionItemCreatedEvent;

/**
 * This class listens to {@link ExecutionItemCreatedEvent} events and expands the TreeViewer, so
 * that every new tree element is directly visible.
 */
public class ProgressItemCreatedListener {

    private ViewerPart viewerPart;

    public ProgressItemCreatedListener(ViewerPart treePart) {
        this.viewerPart = treePart;
    }

    @Subscribe
    public void progressItemCreated(ExecutionItemCreatedEvent progressItemCreatedEvent) {
        Viewer viewer = viewerPart.getViewer();
        if (viewer != null) {
            viewer.getControl().getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    Viewer viewer = viewerPart.getViewer();
                    if (viewer instanceof TreeViewer) {
                        ((TreeViewer) viewer).expandAll();
                    }
                }
            });
        }
    }
}
