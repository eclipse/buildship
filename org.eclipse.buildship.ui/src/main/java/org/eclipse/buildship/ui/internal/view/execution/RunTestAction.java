/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.execution;

import java.util.List;

import org.gradle.tooling.events.test.TestOperationDescriptor;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import org.eclipse.jface.action.Action;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.TestRunConfiguration;
import org.eclipse.buildship.core.internal.launch.RunGradleTestLaunchRequestJob;
import org.eclipse.buildship.ui.internal.util.gradle.GradleUtils;
import org.eclipse.buildship.ui.internal.util.nodeselection.NodeSelection;
import org.eclipse.buildship.ui.internal.util.nodeselection.SelectionSpecificAction;

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
        List<TestOperationDescriptor> tests = collectSelectedTests(this.executionPage.getSelection());
        List<TestOperationDescriptor> filteredTests = GradleUtils.filterChildren(tests);
        TestRunConfiguration runConfig = CorePlugin.configurationManager().loadTestRunConfiguration(this.executionPage.getProcessDescription().getRunConfig());
        RunGradleTestLaunchRequestJob runTestsJob = new RunGradleTestLaunchRequestJob(filteredTests, runConfig);
        runTestsJob.schedule();
    }

    @Override
    public boolean isVisibleFor(NodeSelection selection) {
        return !selection.isEmpty() && FluentIterable.from(selection.toList(OperationItem.class)).anyMatch(TEST_OPERATION_ITEM_PREDICATE);
    }

    @Override
    public boolean isEnabledFor(NodeSelection selection) {
        return !selection.isEmpty() && FluentIterable.from(selection.toList(OperationItem.class)).allMatch(TEST_OPERATION_ITEM_PREDICATE);
    }

    @Override
    public void setEnabledFor(NodeSelection selection) {
        setEnabled(isEnabledFor(selection));
    }

    private List<TestOperationDescriptor> collectSelectedTests(NodeSelection nodeSelection) {
        return FluentIterable.from(nodeSelection.toList(OperationItem.class)).filter(TEST_OPERATION_ITEM_PREDICATE).transform(new Function<OperationItem, TestOperationDescriptor>() {

            @Override
            public TestOperationDescriptor apply(OperationItem operationItem) {
                return (TestOperationDescriptor) operationItem.getDescriptor();
            }
        }).toList();
    }

    /**
     * Predicate that matches {@code TestOperationDescriptor} instances.
     */
    private static final class TestOperationItemPredicate implements Predicate<OperationItem> {

        @Override
        public boolean apply(OperationItem operationItem) {
            return operationItem.getDescriptor() instanceof TestOperationDescriptor;
        }
    }

}
