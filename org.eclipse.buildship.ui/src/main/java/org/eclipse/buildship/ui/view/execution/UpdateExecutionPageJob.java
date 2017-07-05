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
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.gradle.tooling.events.FailureResult;
import org.gradle.tooling.events.FinishEvent;
import org.gradle.tooling.events.task.TaskOperationDescriptor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Updates the duration of the registered {@link OperationItem} instances in the
 * {@link ExecutionsView} in regular intervals.
 */
public final class UpdateExecutionPageJob extends Job {

    private static final int REPEAT_DELAY = 100;

    private final TreeViewer treeViewer;
    // if the value is true then the operation can be removed
    private final ConcurrentMap<OperationItem, Boolean> activeItems;
    private volatile boolean running;

    public UpdateExecutionPageJob(TreeViewer treeViewer) {
        super("Updating duration of non-finished operations");
        this.treeViewer = treeViewer;
        this.activeItems = new MapMaker().concurrencyLevel(2).makeMap();
        this.running = true;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        // use a copy of to ignore concurrent changes
        Map<OperationItem, Boolean> activeItemsCopy = ImmutableMap.copyOf(this.activeItems);

        // update operations in the view
        Display display = PlatformUI.getWorkbench().getDisplay();
        if (!display.isDisposed()) {
            display.syncExec(new UpdateExecutionPageContent(this.treeViewer, activeItemsCopy.keySet()));
        }

        // delete updated build operations that were marked removable
        for (OperationItem item : activeItemsCopy.keySet()) {
            if (activeItemsCopy.get(item)) {
                this.activeItems.remove(item);
            }
        }

        // reschedule the job such that is runs again in repeatDelay ms
        schedule(REPEAT_DELAY);
        return Status.OK_STATUS;
    }

    public void startOperationItem(OperationItem item) {
        Preconditions.checkNotNull(item.getStartEvent());
        this.activeItems.put(item, Boolean.FALSE);
    }

    public void stopOperationItem(OperationItem item) {
        Preconditions.checkNotNull(item.getStartEvent());
        this.activeItems.replace(item, Boolean.TRUE);
    }

    @Override
    public boolean shouldSchedule() {
        return this.running;
    }

    public void stop() {
        this.running = false;
    }

    /**
     * UI job to refresh active items in the viewer.
     */
    private static class UpdateExecutionPageContent implements Runnable {

        private final Set<OperationItem> activeItems;
        private final TreeViewer treeViewer;

        public UpdateExecutionPageContent(TreeViewer treeViewer, Set<OperationItem> activeItems) {
            this.treeViewer = treeViewer;
            this.activeItems = activeItems;
        }

        @Override
        public void run() {
            if (!this.treeViewer.getControl().isDisposed()) {
                for (OperationItem item : this.activeItems) {
                    this.treeViewer.update(item, null);
                }

                this.treeViewer.refresh(false);

                for (OperationItem item : this.activeItems) {
                    if (shouldBeVisible(item)) {
                        this.treeViewer.expandToLevel(item, 0);
                    }
                }
            }
        }

        private boolean shouldBeVisible(OperationItem item) {
            return isOnMax2ndLevel(item) || isTaskOperation(item) || isFailedOperation(item);
        }

        private boolean isOnMax2ndLevel(OperationItem item) {
            int level = 2;
            while (level >= 0) {
                if (item.getParent() == null) {
                    return true;
                } else {
                    level--;
                    item = item.getParent();
                }
            }
            return false;
        }

        private boolean isTaskOperation(OperationItem item) {
            return item.getStartEvent().getDescriptor() instanceof TaskOperationDescriptor;
        }

        private boolean isFailedOperation(OperationItem item) {
                FinishEvent finishEvent = item.getFinishEvent();
                return finishEvent != null ? finishEvent.getResult() instanceof FailureResult : false;
        }
    }
}