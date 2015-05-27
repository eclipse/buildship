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

import org.gradle.tooling.events.FinishEvent;
import org.gradle.tooling.events.test.TestFailureResult;
import org.gradle.tooling.events.test.TestFinishEvent;
import org.gradle.tooling.events.test.TestOperationResult;

import com.google.common.base.Optional;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.ui.generic.NodeSelection;
import org.eclipse.buildship.ui.generic.NodeSelectionProvider;
import org.eclipse.buildship.ui.generic.SelectionSpecificAction;

/**
 * Action opening a dialog which displays the {@link TestFailureResult} if can be obtained from the
 * selection of the executions view.
 */
public final class ShowTestFailureAction extends Action implements SelectionSpecificAction {

    private final NodeSelectionProvider selectionProvider;

    public ShowTestFailureAction(NodeSelectionProvider selectionProvider) {
        super(ExecutionsViewMessages.Action_ShowTestFailure_Text);
        this.selectionProvider = selectionProvider;
    }

    @Override
    public void run() {
        TestFailureResult testFailureResult = findFailuretInFirstNodeSelection(this.selectionProvider.getSelection()).get();
        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        new FailureDialog(shell, ExecutionsViewMessages.Dialog_Failure_Title, testFailureResult).open();
    }

    @Override
    public boolean isVisibleFor(NodeSelection selection) {
        return findFailuretInFirstNodeSelection(selection).isPresent();
    }

    @Override
    public boolean isEnabledFor(NodeSelection selection) {
        return findFailuretInFirstNodeSelection(selection).isPresent();
    }

    private static Optional<TestFailureResult> findFailuretInFirstNodeSelection(NodeSelection selection) {
        OperationItem operationitem = selection.getFirstNode(OperationItem.class);
        FinishEvent finishEvent = operationitem.getFinishEvent();
        if (finishEvent != null && finishEvent instanceof TestFinishEvent) {
            TestFinishEvent testFinishEvent = (TestFinishEvent) finishEvent;
            TestOperationResult testOperationResult = testFinishEvent.getResult();
            if (testOperationResult instanceof TestFailureResult) {
                return Optional.of((TestFailureResult) testOperationResult);
            }
        }

        return Optional.absent();
    }

    @Override
    public void setEnabledFor(NodeSelection selection) {
        setEnabled(isEnabledFor(selection));
    }

}
