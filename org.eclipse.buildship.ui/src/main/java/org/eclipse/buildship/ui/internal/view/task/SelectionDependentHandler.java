/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task;

import org.eclipse.buildship.ui.internal.util.nodeselection.NodeSelection;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Base class for all {@code AbstractHandler} classes that contain execution logic that depends on the current selection.
 */
 abstract class SelectionDependentHandler extends AbstractHandler {

    @Override
    public void setEnabled(Object evaluationContext) {
        boolean enabled = false;
        if (evaluationContext instanceof IEvaluationContext) {
            IEvaluationContext evalContext = (IEvaluationContext) evaluationContext;
            // getting the current ISelection from the IEvaluationContext
            // See https://wiki.eclipse.org/Command_Core_Expressions
            Object variable = evalContext.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
            if (variable instanceof NodeSelection) {
                enabled = isEnabledFor((NodeSelection) variable);
            }
        }
        setBaseEnabled(enabled);
    }

    protected NodeSelection getSelectionHistory(ExecutionEvent event) {
        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        return NodeSelection.from(currentSelection);
    }

    protected abstract boolean isEnabledFor(NodeSelection selection);

}
