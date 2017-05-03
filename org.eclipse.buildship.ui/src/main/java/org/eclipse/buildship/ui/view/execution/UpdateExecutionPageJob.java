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

import org.gradle.tooling.events.OperationDescriptor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Updates the duration of the registered {@link OperationItem} instances in the {@link ExecutionsView} in regular intervals.
 */
public final class UpdateExecutionPageJob extends Job {

    private static final int REPEAT_DELAY = 500;

    private final Map<OperationDescriptor, OperationItem> operationItems;
    private volatile boolean running;
    private final TreeViewer treeViewer;

    public UpdateExecutionPageJob(TreeViewer treeViewer) {
        super("Updating duration of non-finished operations");

        this.treeViewer = treeViewer;
        this.operationItems = Maps.newHashMap();
        this.running = true;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        Display display = PlatformUI.getWorkbench().getDisplay();
        if (!display.isDisposed()) {
            display.syncExec(new Runnable() {

                @Override
                public void run() {
                    if (!UpdateExecutionPageJob.this.treeViewer.getControl().isDisposed()) {
                        UpdateExecutionPageJob.this.treeViewer.refresh(true);
                        UpdateExecutionPageJob.this.treeViewer.expandToLevel(AbstractTreeViewer.ALL_LEVELS);
                    }
                }
            });
        }
        // reschedule the job such that is runs again in repeatDelay ms
        schedule(REPEAT_DELAY);
        return Status.OK_STATUS;
    }

    public void addOperationItem(OperationItem operationItem) {
        Preconditions.checkNotNull(operationItem.getStartEvent());
        synchronized (this.operationItems) {
            this.operationItems.put(operationItem.getStartEvent().getDescriptor(), operationItem);
        }
    }

    public void removeOperationItem(OperationItem operationItem) {
        Preconditions.checkNotNull(operationItem.getStartEvent());
        synchronized (this.operationItems) {
            this.operationItems.remove(operationItem.getStartEvent().getDescriptor());
            if (this.operationItems.isEmpty()) {
                stop();
            }
        }
    }

    @Override
    public boolean shouldSchedule() {
        return this.running;
    }

    public void stop() {
        this.running = false;
    }
}
