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

package org.eclipse.buildship.ui.view.execution;

import java.util.List;

import org.gradle.tooling.events.test.TestOperationDescriptor;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import org.eclipse.jface.action.Action;

import org.eclipse.buildship.ui.util.nodeselection.NodeSelection;
import org.eclipse.buildship.ui.util.nodeselection.NodeSelectionProvider;
import org.eclipse.buildship.ui.util.nodeselection.SelectionSpecificAction;

/**
 * Action to launch a new Gradle execution specified by {@link TestOperationDescriptor} instances.
 */
public final class RunTestAction extends Action implements SelectionSpecificAction {

    private NodeSelectionProvider selectionProvider;

    public RunTestAction(NodeSelectionProvider selectionProvider) {
        super(ExecutionViewMessages.Action_ExecuteTest_Text);
        this.selectionProvider = Preconditions.checkNotNull(selectionProvider);
    }

    @Override
    public void run() {
        List<TestOperationDescriptor> testDescriptors = collectSelectedTestDescriptors(this.selectionProvider.getSelection());
        // TODO (donat) implement test launch
        System.out.println("ExecuteTestOperationAction fired. Tests to run:");
        for (TestOperationDescriptor testDescriptor : testDescriptors) {
            System.out.println("\tname=" + testDescriptor.getDisplayName());
        }
    }

    @Override
    public boolean isVisibleFor(NodeSelection selection) {
        // the action is visible if at least one test operation descriptor is selected
        return !collectSelectedTestDescriptors(selection).isEmpty();
    }

    @Override
    public boolean isEnabledFor(NodeSelection selection) {
        return true;
    }

    @Override
    public void setEnabledFor(NodeSelection selection) {
        // do nothing: if the action is visible then it's always enabled
    }

    private List<TestOperationDescriptor> collectSelectedTestDescriptors(NodeSelection nodeSelection) {
        return FluentIterable.from(nodeSelection.getNodes(OperationItem.class)).filter(new Predicate<OperationItem>() {

            @Override
            public boolean apply(OperationItem operationItem) {
                return operationItem.getFinishEvent() != null && operationItem.getFinishEvent().getDescriptor() instanceof TestOperationDescriptor;
            }
        }).transform(new Function<OperationItem, TestOperationDescriptor>() {

            @Override
            public TestOperationDescriptor apply(OperationItem operationItem) {
                return (TestOperationDescriptor) operationItem.getFinishEvent().getDescriptor();
            }
        }).toList();
    }
}
