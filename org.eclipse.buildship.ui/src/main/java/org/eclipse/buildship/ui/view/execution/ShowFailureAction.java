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

import org.gradle.tooling.events.FailureResult;
import org.gradle.tooling.events.FinishEvent;
import org.gradle.tooling.events.OperationResult;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.ui.generic.NodeSelection;
import org.eclipse.buildship.ui.generic.NodeSelectionProvider;
import org.eclipse.buildship.ui.generic.SelectionSpecificAction;

/**
 * Action opening a dialog which displays the {@link FailureResult} in a dialog.
 */
public final class ShowFailureAction extends Action implements SelectionSpecificAction {

    private final NodeSelectionProvider selectionProvider;

    public ShowFailureAction(NodeSelectionProvider selectionProvider) {
        super(ExecutionsViewMessages.Action_ShowFailure_Text);
        this.selectionProvider = Preconditions.checkNotNull(selectionProvider);
    }

    @Override
    public void run() {
        FailureResult failureResult = findFailureInFirstSelectedNode(this.selectionProvider.getSelection()).get();
        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        new FailureDialog(shell, ExecutionsViewMessages.Dialog_Failure_Title, failureResult).open();
    }

    @Override
    public boolean isVisibleFor(NodeSelection selection) {
        return findFailureInFirstSelectedNode(selection).isPresent();
    }

    @Override
    public boolean isEnabledFor(NodeSelection selection) {
        return findFailureInFirstSelectedNode(selection).isPresent();
    }

    private static Optional<FailureResult> findFailureInFirstSelectedNode(NodeSelection selection) {
        OperationItem operationitem = selection.getFirstNode(OperationItem.class);
        FinishEvent finishEvent = operationitem.getFinishEvent();
        if (finishEvent != null) {
            OperationResult operationResult = finishEvent.getResult();
            if (operationResult != null && operationResult instanceof FailureResult) {
                FailureResult failureResult = (FailureResult) operationResult;
                return Optional.of(failureResult);
            }
        }

        return Optional.absent();
    }

    @Override
    public void setEnabledFor(NodeSelection selection) {
        setEnabled(isEnabledFor(selection));
    }

}
