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
import org.gradle.tooling.events.test.JvmTestOperationDescriptor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.ui.view.Page;

/**
 * Listens to {@link org.gradle.tooling.events.ProgressEvent} instances that are sent by the Tooling
 * API while a build is executed. Each incoming event is added to the execution tree as an
 * {@link OperationItem} instance.
 */
public final class ExecutionProgressListener implements org.gradle.tooling.events.ProgressListener {

    private static final int UPDATE_DURATION_JOB_INTERVAL_IN_MS = 1000;

    private final Page executionPage;
    private final Map<OperationDescriptor, OperationItem> executionItemMap;
    private final OperationItemRenderer operationItemRenderer;
    private UpdateDurationJob updateDurationJob;

    public ExecutionProgressListener(Page executionPage, OperationItem root) {
        this.executionPage = Preconditions.checkNotNull(executionPage);
        this.executionItemMap = Maps.newLinkedHashMap();
        this.executionItemMap.put(null, Preconditions.checkNotNull(root));
        this.operationItemRenderer = new OperationItemRenderer();
    }

    @Override
    public void statusChanged(ProgressEvent progressEvent) {
        // do not process the event if the operation is technical/artificial, i.e. the event
        // is not of interest to a normal user running a build
        OperationDescriptor descriptor = progressEvent.getDescriptor();
        if (isExcluded(descriptor)) {
            return;
        }

        // create the job to update the duration of running operations as late as possible
        // to make sure it is only created if really needed
        initDurationJobIfNeeded();

        // create a new operation item if the event is a start event, otherwise update the item
        OperationItem operationItem = this.executionItemMap.get(descriptor);
        if (null == operationItem) {
            operationItem = new OperationItem((StartEvent) progressEvent);
            this.executionItemMap.put(descriptor, operationItem);
            this.updateDurationJob.addOperationItem(operationItem);
        } else {
            operationItem.setFinishEvent((FinishEvent) progressEvent);
            this.updateDurationJob.removeOperationItem(operationItem);
        }

        // configure the operation item based on the event details
        this.operationItemRenderer.update(operationItem);

        // attach to (first non-excluded) parent, if this is a new operation (in case of StartEvent)
        OperationItem parentExecutionItem = this.executionItemMap.get(findFirstNonExcludedParent(descriptor));
        parentExecutionItem.addChild(operationItem);

        // ensure that if it is a newly added node it is made visible (in case of StartEvent)
        if (operationItem.getFinishEvent() == null) {
            makeNodeVisible(operationItem);
        }
    }

    private void initDurationJobIfNeeded() {
        if (this.updateDurationJob == null) {
            this.updateDurationJob = new UpdateDurationJob(UPDATE_DURATION_JOB_INTERVAL_IN_MS, this.operationItemRenderer);
            this.updateDurationJob.schedule(UPDATE_DURATION_JOB_INTERVAL_IN_MS);
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

    private void makeNodeVisible(final OperationItem operationItem) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                @SuppressWarnings({ "cast", "RedundantCast" })
                TreeViewer treeViewer = (TreeViewer) ExecutionProgressListener.this.executionPage.getAdapter(TreeViewer.class);
                treeViewer.expandToLevel(operationItem, AbstractTreeViewer.ALL_LEVELS);
            }
        });
    }

}
