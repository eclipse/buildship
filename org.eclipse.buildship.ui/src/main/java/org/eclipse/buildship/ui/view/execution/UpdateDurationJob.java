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

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * This job updates the duration of running Operations in the ExecutionView every 1000 ms.
 */
public class UpdateDurationJob extends Job {

    private boolean running = true;
    private long repeatDelay;
    private OperationItemConfigurator operationItemConfigurator;
    private Collection<OperationItem> operationItems = ImmutableList.<OperationItem> of();

    public UpdateDurationJob(long repeatDelay, OperationItemConfigurator operationItemConfigurator) {
        super("Updating Operation Duration...");
        this.repeatDelay = repeatDelay;
        this.operationItemConfigurator = operationItemConfigurator;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        ImmutableList<OperationItem> immuteableOperationItems = ImmutableList.<OperationItem> copyOf(this.operationItems);
        for (OperationItem operationItem : immuteableOperationItems) {
            this.operationItemConfigurator.updateDuration(operationItem);
        }
        schedule(this.repeatDelay);
        return Status.OK_STATUS;
    }

    public Collection<OperationItem> getOperationItems() {
        return this.operationItems;
    }

    public void setOperationItems(Collection<OperationItem> operationItems) {
        if (operationItems.isEmpty()) {
            stop();
        } else {
            this.operationItems = operationItems;
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
