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

import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import com.google.common.collect.Maps;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.gradle.tooling.events.OperationDescriptor;

/**
 * Updates the duration of the registered {@link OperationItem} instances in the {@link ExecutionsView} in regular intervals.
 */
public final class UpdateDurationJob extends Job {

    private final long repeatDelay;
    private final OperationItemRenderer operationItemRenderer;
    private final Map<OperationDescriptor, OperationItem> operationItems;
    private volatile boolean running;

    public UpdateDurationJob(long repeatDelay, OperationItemRenderer operationItemRenderer) {
        super("Updating duration of non-finished operations");

        this.repeatDelay = repeatDelay;
        this.operationItemRenderer = operationItemRenderer;
        this.operationItems = Maps.newHashMap();
        this.running = true;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        // update all registered operation items
        for (OperationItem operationItem : getOperationItems()) {
            this.operationItemRenderer.updateDuration(operationItem);
        }

        // reschedule the job such that is runs again in repeatDelay ms
        schedule(this.repeatDelay);
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

    private List<OperationItem> getOperationItems() {
        synchronized (this.operationItems) {
            return ImmutableList.copyOf(this.operationItems.values());
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
