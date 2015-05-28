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
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.eclipse.buildship.ui.generic.NodeSelection;
import org.eclipse.buildship.ui.generic.NodeSelectionProvider;
import org.eclipse.buildship.ui.generic.SelectionSpecificAction;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.gradle.tooling.Failure;
import org.gradle.tooling.events.FailureResult;
import org.gradle.tooling.events.FinishEvent;

import java.util.List;

/**
 * Opens a dialog which displays the {@link FailureResult} in a dialog.
 */
public final class ShowFailureAction extends Action implements SelectionSpecificAction {

    private final NodeSelectionProvider selectionProvider;

    public ShowFailureAction(NodeSelectionProvider selectionProvider) {
        super(ExecutionsViewMessages.Action_ShowFailure_Text);
        this.selectionProvider = Preconditions.checkNotNull(selectionProvider);
    }

    @Override
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        List<? extends Failure> failures = collectFailures(this.selectionProvider.getSelection());
        new FailureDialog(shell, ExecutionsViewMessages.Dialog_Failure_Title, failures).open();
    }

    private List<? extends Failure> collectFailures(NodeSelection selection) {
        if (selection.isEmpty()) {
            return ImmutableList.of();
        }

        if (!selection.hasAllNodesOfType(OperationItem.class)) {
            return ImmutableList.of();
        }

        List<Failure> result = Lists.newArrayList();
        ImmutableList<OperationItem> operationItems = selection.getNodes(OperationItem.class);
        for (OperationItem operationItem : operationItems) {
            FinishEvent finishEvent = operationItem.getFinishEvent();
            if (finishEvent != null && finishEvent.getResult() instanceof FailureResult) {
                List<? extends Failure> failures = ((FailureResult) finishEvent.getResult()).getFailures();
                result.addAll(failures);
            }
        }
        return result;
    }

    @Override
    public boolean isVisibleFor(NodeSelection selection) {
        if (selection.isEmpty()) {
            return false;
        }

        if (!selection.hasAllNodesOfType(OperationItem.class)) {
            return false;
        }

        ImmutableList<OperationItem> operationItems = selection.getNodes(OperationItem.class);
        return FluentIterable.from(operationItems).anyMatch(new Predicate<OperationItem>() {
            @Override
            public boolean apply(OperationItem operationItem) {
                FinishEvent finishEvent = operationItem.getFinishEvent();
                return finishEvent != null && finishEvent.getResult() instanceof FailureResult;
            }
        });
    }

    @Override
    public boolean isEnabledFor(NodeSelection selection) {
        if (selection.isEmpty()) {
            return false;
        }

        if (!selection.hasAllNodesOfType(OperationItem.class)) {
            return false;
        }

        ImmutableList<OperationItem> operationItems = selection.getNodes(OperationItem.class);
        return FluentIterable.from(operationItems).allMatch(new Predicate<OperationItem>() {
            @Override
            public boolean apply(OperationItem operationItem) {
                FinishEvent finishEvent = operationItem.getFinishEvent();
                return finishEvent != null && finishEvent.getResult() instanceof FailureResult;
            }
        });
    }


    @Override
    public void setEnabledFor(NodeSelection selection) {
        setEnabled(isEnabledFor(selection));
    }

}
