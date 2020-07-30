/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.execution;

import java.util.List;
import java.util.stream.Collectors;

import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.test.TestOperationDescriptor;
import org.gradle.tooling.events.test.TestOutputDescriptor;

import org.eclipse.jface.action.Action;

import org.eclipse.buildship.ui.internal.util.nodeselection.NodeSelection;
import org.eclipse.buildship.ui.internal.util.nodeselection.SelectionSpecificAction;

/**
 * Opens the sources files of the the selected test operation items. Delegates to
 * {@link OpenTestSourceFileJob} to do the actual search and navigation.
 *
 * @see OpenTestSourceFileJob
 */
public final class ShowTestOutputAction extends Action implements SelectionSpecificAction {

    private final ExecutionPage executionPage;

    public ShowTestOutputAction(ExecutionPage executionPage) {
        //super(ExecutionViewMessages.Action_OpenTestSourceFile_Text);
        super("Show test output");
        this.executionPage = executionPage;
    }

    @Override
    public void run() {
        OperationItem item = this.executionPage.getSelection().getFirstElement(OperationItem.class);
        List<OperationItem> output = item.getChildren().stream().filter(i -> i.getDescriptor() instanceof TestOutputDescriptor).collect(Collectors.toList());

        for (OperationItem io : output) {
            System.out.print("!!" + ((TestOutputDescriptor)io.getDescriptor()).getMessage());
        }
    }

    @Override
    public boolean isVisibleFor(NodeSelection selection) {
        if (selection.toList().size() != 1) {
            return false;
        }

        if (!selection.hasAllNodesOfType(OperationItem.class)) {
            return false;
        }

        OperationItem item = selection.getFirstElement(OperationItem.class);
        OperationDescriptor descriptor = item.getDescriptor();

        return descriptor instanceof TestOperationDescriptor && !item.getChildren().stream().filter(i -> i.getDescriptor() instanceof TestOutputDescriptor).collect(Collectors.toList()).isEmpty();
    }

    @Override
    public boolean isEnabledFor(NodeSelection selection) {
        return isVisibleFor(selection);
    }

    @Override
    public void setEnabledFor(NodeSelection selection) {
        setEnabled(isEnabledFor(selection));
    }
}
