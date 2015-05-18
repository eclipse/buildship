/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - refactoring and integration
 */

package org.eclipse.buildship.ui.handler;

import org.eclipse.buildship.ui.part.TreeViewerState;
import org.eclipse.buildship.ui.part.ViewerProvider;
import org.eclipse.buildship.ui.part.execution.ExecutionsViewState;
import org.eclipse.core.commands.*;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import java.util.Map;

/**
 * Toggles the visibility of the header in {@link ViewerProvider} implementations.
 */
public final class ShowTreeHeaderHandler extends AbstractHandler implements IElementUpdater {

    private static final String COMMAND_TOGGLE_STATE_ID = "org.eclipse.ui.commands.toggleState";

    private final TreeViewerState prefs;

    public ShowTreeHeaderHandler() {
        this.prefs = new ExecutionsViewState();
    }

    @Override
    public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
        element.setChecked(getToggleState());
    }

    @Override
    public final Object execute(ExecutionEvent event) throws ExecutionException {
        // get the current true/false state of the command
        Command command = event.getCommand();
        State state = command.getState(COMMAND_TOGGLE_STATE_ID);
        Object stateValue = state.getValue();
        if (!(stateValue instanceof Boolean)) {
            return null;
        }

        Object execute = doExecute(event);
        Boolean stateBoolean = (Boolean) stateValue;
        if (getToggleState() != stateBoolean) {
            HandlerUtil.toggleCommandState(command);
        }
        return execute;
    }

    protected boolean getToggleState() {
        return this.prefs.isShowTreeHeader();
    }

    protected Object doExecute(ExecutionEvent event) {
        // invert current header visible state
        this.prefs.setShowTreeHeader(!getToggleState());

        IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
        if (activePart instanceof ViewerProvider) {
            ViewerProvider viewerPart = (ViewerProvider) activePart;
            setHeaderVisible(viewerPart.getViewer(), getToggleState());
        }

        return null;
    }

    private static void setHeaderVisible(Viewer viewer, boolean visible) {
        if (viewer instanceof TreeViewer) {
            Tree tree = ((TreeViewer) viewer).getTree();
            tree.setHeaderVisible(visible);
        } else if (viewer instanceof TableViewer) {
            Table table = ((TableViewer) viewer).getTable();
            table.setHeaderVisible(visible);
        }
    }

}
