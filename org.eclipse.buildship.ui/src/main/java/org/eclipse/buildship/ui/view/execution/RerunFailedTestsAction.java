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
import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.gradle.tooling.events.test.JvmTestOperationDescriptor;
import org.gradle.tooling.events.test.TestFailureResult;
import org.gradle.tooling.events.test.TestFinishEvent;
import org.gradle.tooling.events.test.TestOperationDescriptor;
import org.gradle.tooling.events.test.TestOperationResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * Action for the {@link ExecutionPage} to rerun all failed tests.
 */
public class RerunFailedTestsAction extends Action implements ProgressListener {

    private ExecutionPage page;
    private Builder<TestOperationDescriptor> failedTestOperationDescriptorsBuilder;

    private ImmutableList<TestOperationDescriptor> failedTestOperationDescriptors;

    public RerunFailedTestsAction(ExecutionPage page) {
        this.page = page;
        setToolTipText(ExecutionViewMessages.Action_Rerun_Failed_Tests_Text);
        setImageDescriptor(PluginImages.RERUN_FAILED_TESTS.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.RERUN_FAILED_TESTS.withState(ImageState.DISABLED).getImageDescriptor());

        this.failedTestOperationDescriptorsBuilder = ImmutableList.builder();
        registerJobChangeListener();
    }

    private void registerJobChangeListener() {
        Job job = this.page.getBuildJob();
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                RerunFailedTestsAction.this.failedTestOperationDescriptors = RerunFailedTestsAction.this.failedTestOperationDescriptorsBuilder
                        .build();
                setEnabled(event.getJob().getState() == Job.NONE
                        && !RerunFailedTestsAction.this.failedTestOperationDescriptors.isEmpty());
            }
        });
        setEnabled(job.getState() == Job.NONE);
    }

    @Override
    public void run() {
        Job buildJob = this.page.getBuildJob();
        if (buildJob instanceof RunGradleConfigurationDelegateJob) {
            Job testLaunchRequestJob = RunGradleConfigurationDelegateJob.createTestReRunDelegateJob(
                    ((RunGradleConfigurationDelegateJob) buildJob).getLaunch(),
                    ExecutionViewMessages.Action_Rerun_Failed_Tests_Text, this.failedTestOperationDescriptors,
                    this.page.getConfigurationAttributes());
            testLaunchRequestJob.schedule();
        }
    }

    @Override
    public void statusChanged(ProgressEvent event) {
        if (event instanceof TestFinishEvent) {
            TestOperationResult operationResult = ((TestFinishEvent) event).getResult();
            if (operationResult instanceof TestFailureResult) {
                OperationDescriptor descriptor = event.getDescriptor();
                if (descriptor instanceof JvmTestOperationDescriptor
                        && ((JvmTestOperationDescriptor) descriptor).getMethodName() != null) {
                    this.failedTestOperationDescriptorsBuilder.add((TestOperationDescriptor) descriptor);
                }
            }
        }
    }
}
