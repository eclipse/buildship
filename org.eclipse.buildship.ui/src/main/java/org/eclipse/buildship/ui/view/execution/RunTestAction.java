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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.launch.RunGradleTestLaunchRequestJob;
import org.eclipse.buildship.ui.util.nodeselection.NodeSelection;
import org.eclipse.buildship.ui.util.nodeselection.SelectionSpecificAction;
import org.eclipse.jface.action.Action;
import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.test.TestOperationDescriptor;

import java.util.List;

/**
 * Action to launch a new Gradle execution specified by {@link TestOperationDescriptor} instances.
 */
public final class RunTestAction extends Action implements SelectionSpecificAction {

    private static final TestOperationItemPredicate TEST_OPERATION_ITEM_PREDICATE = new TestOperationItemPredicate();

    private final ExecutionPage executionPage;

    public RunTestAction(ExecutionPage executionPage) {
        super(ExecutionViewMessages.Action_RunTest_Text);
        this.executionPage = Preconditions.checkNotNull(executionPage);
    }

    @Override
    public void run() {
        List<TestOperationDescriptor> testDescriptors = collectSelectedTestOperationDescriptors(this.executionPage.getSelection());
        List<TestOperationDescriptor> filteredTestDescriptors = filterChildren(testDescriptors);
        GradleRunConfigurationAttributes configurationAttributes = this.executionPage.getProcessDescription().getConfigurationAttributes();
        RunGradleTestLaunchRequestJob runTestsJob = new RunGradleTestLaunchRequestJob(filteredTestDescriptors, configurationAttributes);
        runTestsJob.schedule();
    }

    @Override
    public boolean isVisibleFor(NodeSelection selection) {
        return !selection.isEmpty() && FluentIterable.from(selection.getNodes(OperationItem.class)).anyMatch(TEST_OPERATION_ITEM_PREDICATE);
    }

    @Override
    public boolean isEnabledFor(NodeSelection selection) {
        return !selection.isEmpty() && FluentIterable.from(selection.getNodes(OperationItem.class)).allMatch(TEST_OPERATION_ITEM_PREDICATE);
    }

    @Override
    public void setEnabledFor(NodeSelection selection) {
        setEnabled(isEnabledFor(selection));
    }

    private List<TestOperationDescriptor> collectSelectedTestOperationDescriptors(NodeSelection nodeSelection) {
        return FluentIterable.from(nodeSelection.getNodes(OperationItem.class)).filter(TEST_OPERATION_ITEM_PREDICATE).transform(new Function<OperationItem, TestOperationDescriptor>() {

            @Override
            public TestOperationDescriptor apply(OperationItem operationItem) {
                return (TestOperationDescriptor) operationItem.getStartEvent().getDescriptor();
            }
        }).toList();
    }

    private List<TestOperationDescriptor> filterChildren(List<TestOperationDescriptor> testDescriptors) {
        ImmutableList.Builder<TestOperationDescriptor> withoutChildren = ImmutableList.builder();
        for (TestOperationDescriptor testDescriptor : testDescriptors) {
            if (!isParentSelected(testDescriptor, testDescriptors)) {
                withoutChildren.add(testDescriptor);
            }
        }
        return withoutChildren.build();
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean isParentSelected(TestOperationDescriptor candidate, List<TestOperationDescriptor> selectedTestDescriptors) {
        OperationDescriptor parent = candidate.getParent();
        if (parent instanceof TestOperationDescriptor) {
            return selectedTestDescriptors.contains(parent) || isParentSelected((TestOperationDescriptor) parent, selectedTestDescriptors);
        } else {
            return false;
        }
    }

    /**
     * Predicate that matches {@code TestOperationDescriptor} instances.
     */
    private static final class TestOperationItemPredicate implements Predicate<OperationItem> {

        @Override
        public boolean apply(OperationItem operationItem) {
            return operationItem.getStartEvent().getDescriptor() instanceof TestOperationDescriptor;
        }

    }

}
