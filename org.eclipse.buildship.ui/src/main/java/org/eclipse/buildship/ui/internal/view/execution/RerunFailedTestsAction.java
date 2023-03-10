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

import org.gradle.tooling.events.test.JvmTestKind;
import org.gradle.tooling.events.test.JvmTestOperationDescriptor;
import org.gradle.tooling.events.test.TestFailureResult;
import org.gradle.tooling.events.test.TestFinishEvent;
import org.gradle.tooling.events.test.TestOperationDescriptor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.graph.Traverser;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.TestRunConfiguration;
import org.eclipse.buildship.core.internal.launch.RunGradleTestLaunchRequestJob;
import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;
import org.eclipse.buildship.ui.internal.i18n.UiMessages;
import org.eclipse.buildship.ui.internal.util.gradle.GradleUtils;

/**
 * Reruns the build represented by the target {@link org.eclipse.buildship.ui.internal.view.execution.ExecutionPage}.
 * <p/>
 * Note: we listen for removals of {@code ILaunchConfiguration} instances even though not every {@code ProcessDescription} implementation
 * is necessarily backed by a launch configuration. This means that in the worst case, {@code ProcessDescription#isRerunnable()} is invoked
 * unnecessarily (which does no harm).
 */
public final class RerunFailedTestsAction extends Action {

    private final ExecutionPage page;

    public RerunFailedTestsAction(ExecutionPage executionPage) {
        this.page = Preconditions.checkNotNull(executionPage);

        setToolTipText(UiMessages.Action_RerunFailedTests_Tooltip);
        setImageDescriptor(PluginImages.RERUN_FAILED_TESTS.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.RERUN_FAILED_TESTS.withState(ImageState.DISABLED).getImageDescriptor());

        registerJobChangeListener();
    }

    private void registerJobChangeListener() {
        Job job = this.page.getProcessDescription().getJob();
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                update();
            }
        });
        update();
    }

    private void update() {
        setEnabled(this.page.getProcessDescription().getJob().getState() == Job.NONE && !collectFailedTests().isEmpty());
    }

    @Override
    public void run() {
        List<TestOperationDescriptor> failedTests = collectFailedTests();
        List<TestOperationDescriptor> filteredFailedTests = GradleUtils.filterChildren(failedTests);
        TestRunConfiguration runConfig = CorePlugin.configurationManager().loadTestRunConfiguration(this.page.getProcessDescription().getRunConfig());
        RunGradleTestLaunchRequestJob job = new RunGradleTestLaunchRequestJob(filteredFailedTests, runConfig);
        job.schedule();
    }

    private ImmutableList<TestOperationDescriptor> collectFailedTests() {
        OperationItem root = (OperationItem) this.page.getPageControl().getViewer().getInput();
        if (root == null) {
            return ImmutableList.of();
        }

        Builder<TestOperationDescriptor> result = ImmutableList.builder();
        for (OperationItem item : Traverser.forTree(OperationItem::getChildren).breadthFirst(root)) {
            if (isFailedJvmTest(item)) {
                result.add((JvmTestOperationDescriptor) item.getFinishEvent().getDescriptor());
            }
        }
        return result.build();
    }

    private boolean isFailedJvmTest(OperationItem operationItem) {
        if (operationItem.getFinishEvent() instanceof TestFinishEvent) {
            TestFinishEvent testFinishEvent = (TestFinishEvent) operationItem.getFinishEvent();
            if (testFinishEvent.getResult() instanceof TestFailureResult && testFinishEvent.getDescriptor() instanceof JvmTestOperationDescriptor) {
                JvmTestOperationDescriptor descriptor = (JvmTestOperationDescriptor) testFinishEvent.getDescriptor();
                if (descriptor.getJvmTestKind() == JvmTestKind.ATOMIC || descriptor.getJvmTestKind() == JvmTestKind.UNKNOWN) {
                    return true;
                }
            }
        }
        return false;
    }

}
