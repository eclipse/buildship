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

import java.util.Map;

import org.gradle.tooling.events.FinishEvent;
import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.StartEvent;
import org.gradle.tooling.events.test.JvmTestKind;
import org.gradle.tooling.events.test.JvmTestOperationDescriptor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Listens to {@link org.gradle.tooling.events.ProgressEvent} instances that are sent by the Tooling
 * API while a build is executed. Each incoming event is added to the execution tree as an
 * {@link OperationItem} instance.
 */
public final class ExecutionProgressListener implements org.gradle.tooling.events.ProgressListener {

    private final Map<OperationDescriptor, OperationItem> executionItemMap;
    private final TreeViewer treeViewer;
    private UpdateExecutionPageJob updateExecutionPageJob;

    public ExecutionProgressListener(TreeViewer treeViewer, OperationItem root, Job executionJob) {
        this.executionItemMap = Maps.newLinkedHashMap();
        this.executionItemMap.put(null, Preconditions.checkNotNull(root));
        this.treeViewer = treeViewer;
        executionJob.addJobChangeListener(new JobChangeAdapter(){
            @Override
            public void done(IJobChangeEvent event) {
                if (ExecutionProgressListener.this.updateExecutionPageJob != null) {
                    ExecutionProgressListener.this.updateExecutionPageJob.stop();
                }
            }
        });
    }

    @Override
    public void statusChanged(ProgressEvent progressEvent) {
        // do not process the event if the operation is technical/artificial, i.e. the event
        // is not of interest to a normal user running a build
        OperationDescriptor descriptor = progressEvent.getDescriptor();
        if (isExcluded(descriptor)) {
            return;
        }

        // create the job to periodically update this page
        initUpdaterJob();

        // create a new operation item if the event is a start event, otherwise update the item
        OperationItem operationItem = this.executionItemMap.get(descriptor);
        if (null == operationItem) {
            operationItem = new OperationItem((StartEvent) progressEvent);
            this.executionItemMap.put(descriptor, operationItem);
            this.updateExecutionPageJob.startOperationItem(operationItem);
        } else {
            operationItem.setFinishEvent((FinishEvent) progressEvent);
            this.updateExecutionPageJob.stopOperationItem(operationItem);
            if (isJvmTestSuite(descriptor) && operationItem.getChildren().isEmpty()) {
                // do not display test suite nodes that have no children (unwanted artifacts from Gradle)
                OperationItem parentOperationItem = this.executionItemMap.get(findFirstNonExcludedParent(descriptor));
                parentOperationItem.removeChild(operationItem);
                return;
            }
        }

        // attach to (first non-excluded) parent, if this is a new operation (in case of StartEvent)
        OperationItem parentExecutionItem = this.executionItemMap.get(findFirstNonExcludedParent(descriptor));
        parentExecutionItem.addChild(operationItem);
    }

    private void initUpdaterJob() {
        if (this.updateExecutionPageJob == null) {
            this.updateExecutionPageJob = new UpdateExecutionPageJob(this.treeViewer);
            this.updateExecutionPageJob.schedule();
        }
    }

    private boolean isExcluded(OperationDescriptor descriptor) {
        // ignore the 'artificial' events issued for the root test event and for each forked test
        // process event
        if (descriptor instanceof JvmTestOperationDescriptor) {
            JvmTestOperationDescriptor jvmTestOperationDescriptor = (JvmTestOperationDescriptor) descriptor;
            return jvmTestOperationDescriptor.getSuiteName() != null && jvmTestOperationDescriptor.getClassName() == null;
        } else {
            return false;
        }
    }

    private OperationDescriptor findFirstNonExcludedParent(OperationDescriptor descriptor) {
        while (isExcluded(descriptor.getParent())) {
            descriptor = descriptor.getParent();
        }
        return descriptor.getParent();
    }

    private boolean isJvmTestSuite(OperationDescriptor descriptor) {
        if (descriptor instanceof JvmTestOperationDescriptor) {
            JvmTestOperationDescriptor testOperationDescriptor = (JvmTestOperationDescriptor) descriptor;
            if (testOperationDescriptor.getJvmTestKind() == JvmTestKind.SUITE ) {
                return true;
            }
        }
        return false;
    }
}
