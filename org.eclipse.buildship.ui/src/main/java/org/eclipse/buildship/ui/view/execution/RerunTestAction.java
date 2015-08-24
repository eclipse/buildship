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

import org.eclipse.buildship.core.launch.RunGradleConfigurationDelegateJob;
import org.eclipse.buildship.ui.util.nodeselection.NodeSelection;
import org.eclipse.buildship.ui.util.nodeselection.SelectionSpecificAction;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.gradle.tooling.events.FinishEvent;
import org.gradle.tooling.events.test.TestOperationDescriptor;
import org.gradle.tooling.events.test.TestOperationResult;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

/**
 * This action is used to rerun tests, which are selected in a
 * {@link ExecutionPage}.
 */
public class RerunTestAction extends Action implements SelectionSpecificAction {

    private ExecutionPage page;

    public RerunTestAction(ExecutionPage executionPage) {
        this.page = Preconditions.checkNotNull(executionPage);

        setText("Rerun selected Tests");
        registerJobChangeListener();
    }

    private void registerJobChangeListener() {
        Job job = this.page.getBuildJob();
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                RerunTestAction.this.setEnabled(event.getJob().getState() == Job.NONE);
            }
        });
        setEnabled(job.getState() == Job.NONE);
    }

    @Override
    public void run() {
        NodeSelection selection = this.page.getSelection();
        ImmutableList<OperationItem> nodes = selection.getNodes(OperationItem.class);
        ImmutableList<TestOperationDescriptor> testOperationDescriptors = FluentIterable.from(nodes)
                .filter(new Predicate<OperationItem>() {

                    @Override
                    public boolean apply(OperationItem operationItem) {
                        return operationItem.getFinishEvent() != null
                                && operationItem.getFinishEvent().getDescriptor() instanceof TestOperationDescriptor;
                    }
                }).transform(new Function<OperationItem, TestOperationDescriptor>() {

                    @Override
                    public TestOperationDescriptor apply(OperationItem operationItem) {
                        return (TestOperationDescriptor) operationItem.getFinishEvent().getDescriptor();
                    }
                }).toList();

        Job buildJob = this.page.getBuildJob();
        if (buildJob instanceof RunGradleConfigurationDelegateJob) {
            Job testLaunchRequestJob = RunGradleConfigurationDelegateJob.createTestReRunDelegateJob(
                    ((RunGradleConfigurationDelegateJob) buildJob).getLaunch(), "Rerun selected tests",
                    testOperationDescriptors, this.page.getConfigurationAttributes());
            testLaunchRequestJob.schedule();
        }
    }

    @Override
    public boolean isVisibleFor(NodeSelection selection) {
        if (selection.isEmpty()) {
            return false;
        }

        if (!selection.hasAllNodesOfType(OperationItem.class)) {
            return false;
        }

        // at least one selected node must be a Test
        ImmutableList<OperationItem> operationItems = selection.getNodes(OperationItem.class);
        return FluentIterable.from(operationItems).anyMatch(new Predicate<OperationItem>() {
            @Override
            public boolean apply(OperationItem operationItem) {
                FinishEvent finishEvent = operationItem.getFinishEvent();
                return finishEvent != null && finishEvent.getResult() instanceof TestOperationResult;
            }
        });
    }

    @Override
    public boolean isEnabledFor(NodeSelection selection) {
        return true;
    }

    @Override
    public void setEnabledFor(NodeSelection selection) {
    }
}
