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

package org.eclipse.buildship.ui.part.execution.listener;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.event.EventListener;
import org.eclipse.buildship.ui.part.ViewerProvider;
import org.eclipse.buildship.ui.part.execution.model.OperationItemCreatedEvent;

/**
 * This class listens to {@link OperationItemCreatedEvent} events and expands the TreeViewer, so
 * that every new tree element is directly visible.
 */
public class OperationItemCreatedListener implements EventListener {

    private ViewerProvider viewerPart;

    public OperationItemCreatedListener(ViewerProvider treePart) {
        this.viewerPart = treePart;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof OperationItemCreatedEvent) {
            operationItemCreated((OperationItemCreatedEvent) event);
        }
    }

    private void operationItemCreated(final OperationItemCreatedEvent operationItemCreatedEvent) {
        Viewer viewer = this.viewerPart.getViewer();
        if (viewer != null) {
            viewer.getControl().getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    Viewer viewer = OperationItemCreatedListener.this.viewerPart.getViewer();
                    if (viewer instanceof TreeViewer) {
                        ((TreeViewer) viewer).expandToLevel(operationItemCreatedEvent.getElement(), AbstractTreeViewer.ALL_LEVELS);
                    }
                }
            });
        }
    }
}
