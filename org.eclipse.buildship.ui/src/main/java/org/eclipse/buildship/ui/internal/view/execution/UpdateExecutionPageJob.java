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

package org.eclipse.buildship.ui.internal.view.execution;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.gradle.tooling.events.ProgressEvent;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Updates the duration of the registered {@link OperationItem} instances in the
 * {@link ExecutionsView} in regular intervals.
 */
public final class UpdateExecutionPageJob extends Job {

    private static final double MAX_UPDATES_PER_SECOND = 10.0;

    private final ExecutionPage page;
    private final BlockingQueue<ProgressEvent> queue = new LinkedBlockingQueue<>();
    private volatile boolean running;

    public UpdateExecutionPageJob(ExecutionPage page) {
        super("Updating duration of non-finished operations");
        this.page = page;
        this.running = true;
        setSystem(true);
    }

    public void addEvent(ProgressEvent event) {
        this.queue.add(event);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        RateLimiter rateLimiter = RateLimiter.create(MAX_UPDATES_PER_SECOND);
        while(this.running || !this.queue.isEmpty()) {
            rateLimiter.acquire();

            List<ProgressEvent> events = Lists.newArrayList();
            this.queue.drainTo(events);

            Display display = PlatformUI.getWorkbench().getDisplay();
            if (!display.isDisposed()) {
                display.syncExec(new UpdateExecutionPageContent(this.page, events));
            }
        }

        return Status.OK_STATUS;
    }

    public void stop() {
        this.running = false;
    }

    /**
     * UI job to refresh active items in the viewer.
     */
    private static class UpdateExecutionPageContent implements Runnable {
        private final ExecutionPage page;
        private final List<ProgressEvent> events;

        public UpdateExecutionPageContent(ExecutionPage page, List<ProgressEvent> events) {
            this.page = page;
            this.events = events;
        }

        @Override
        public void run() {
            if (!this.page.getPageControl().isDisposed()) {
                for(ProgressEvent event : this.events) {
                    this.page.onProgress(event);
                }
                this.page.refreshChangedItems();
            }
        }
    }
}