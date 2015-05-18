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

import org.eclipse.buildship.ui.part.FilteredTreeProvider;
import org.eclipse.buildship.ui.viewer.FilteredTree;
import org.eclipse.core.commands.*;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import java.util.Map;

/**
 * Toggles the visibility of the filter in {@code FilteredTreeProvider} implementations. Note that
 * the visibility state of the tree filter is transient.
 */
public final class ShowTreeFilterHandler extends AbstractHandler implements IElementUpdater {

    private static final String COMMAND_TOGGLE_STATE_ID = "org.eclipse.ui.commands.toggleState";

    @SuppressWarnings("rawtypes")
    @Override
    public void updateElement(UIElement element, Map parameters) {
        IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
        if (activePart instanceof FilteredTreeProvider) {
            FilteredTree filteredTree = ((FilteredTreeProvider) activePart).getFilteredTree();
            element.setChecked(filteredTree != null && filteredTree.isShowFilterControls());
        }
    }

    @Override
    public void setEnabled(Object evaluationContext) {
        boolean enabled = false;
        if (evaluationContext instanceof IEvaluationContext) {
            Object activePart = ((IEvaluationContext) evaluationContext).getVariable(ISources.ACTIVE_PART_NAME);
            if (activePart instanceof FilteredTreeProvider) {
                FilteredTree filteredTree = ((FilteredTreeProvider) activePart).getFilteredTree();
                enabled = filteredTree != null;
            }
        }
        setBaseEnabled(enabled);
    }


    @Override
    public final Object execute(ExecutionEvent event) throws ExecutionException {
        // get the current true/false state of the command
        Command command = event.getCommand();
        State state = command.getState(COMMAND_TOGGLE_STATE_ID);
        Object stateValue = state.getValue();
        if (stateValue == null) {
            state.setValue(false);
        }

        IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
        if (activePart instanceof FilteredTreeProvider) {
            FilteredTree filteredTree = ((FilteredTreeProvider) activePart).getFilteredTree();
            filteredTree.setShowFilterControls(!filteredTree.isShowFilterControls());

            HandlerUtil.toggleCommandState(command);
        }

        return null;
    }

}
