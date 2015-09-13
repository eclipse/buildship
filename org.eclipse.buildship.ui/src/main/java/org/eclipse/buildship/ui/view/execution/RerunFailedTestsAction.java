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
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.launch.RunGradleTestLaunchRequestJob;
import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.i18n.UiMessages;
import org.eclipse.buildship.ui.util.gradle.GradleUtils;

import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.gradle.tooling.events.test.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Reruns the build represented by the target {@link org.eclipse.buildship.ui.view.execution.ExecutionPage}.
 * <p/>
 * Note: we listen for removals of {@code ILaunchConfiguration} instances even though not every {@code ProcessDescription} implementation
 * is necessarily backed by a launch configuration. This means that in the worst case, {@code ProcessDescription#isRerunnable()} is invoked
 * unnecessarily (which does no harm).
 */
public final class RerunFailedTestsAction extends Action {

    private final ExecutionPage page;

    private AtomicReference<Collection<?>> pageContent = new AtomicReference<Collection<?>>(Collections.emptyList());

    public RerunFailedTestsAction(ExecutionPage executionPage) {
        this.page = Preconditions.checkNotNull(executionPage);

        setToolTipText(UiMessages.Action_RerunFailedTests_Tooltip);
        setImageDescriptor(PluginImages.RERUN_FAILED_TESTS.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.RERUN_FAILED_TESTS.withState(ImageState.DISABLED).getImageDescriptor());

        registerPageContentCollection();
        registerJobChangeListener();
    }

    private void registerPageContentCollection() {
        this.page.getContentProvider().getKnownElements().addSetChangeListener(new ISetChangeListener() {

            @Override
            public void handleSetChange(SetChangeEvent event) {
                Object source = event.getSource();
                if (source instanceof Collection<?>) {
                    RerunFailedTestsAction.this.pageContent.set(ImmutableList.copyOf((Collection<?>)source));
                }
            }
        });
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
        GradleRunConfigurationAttributes configurationAttributes = this.page.getProcessDescription().getConfigurationAttributes();
        RunGradleTestLaunchRequestJob job = new RunGradleTestLaunchRequestJob(filteredFailedTests, configurationAttributes);
        job.schedule();
    }

    private ImmutableList<TestOperationDescriptor> collectFailedTests() {
        ImmutableList.Builder<TestOperationDescriptor> failedTests = ImmutableList.builder();

        ImmutableList<OperationItem> treeNodes = FluentIterable.from(this.pageContent.get()).filter(OperationItem.class).toList();
        for (OperationItem treeNode : treeNodes) {
            if (isFailedJvmTest(treeNode)) {
                failedTests.add((JvmTestOperationDescriptor) treeNode.getFinishEvent().getDescriptor());
            }
        }

        return failedTests.build();
    }

    private boolean isFailedJvmTest(OperationItem operationItem) {
        if (operationItem.getFinishEvent() instanceof TestFinishEvent) {
            TestFinishEvent testFinishEvent = (TestFinishEvent) operationItem.getFinishEvent();
            if (testFinishEvent.getResult() instanceof TestFailureResult && testFinishEvent.getDescriptor() instanceof JvmTestOperationDescriptor) {
                return true;
            }
        }
        return false;
    }

}
