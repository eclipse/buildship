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

import org.gradle.tooling.events.ProgressEvent;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 * Buffers {@link ProgressEvent}s for asynchronous UI updates in the {@link ExecutionPage}.
 */
public final class ExecutionProgressListener implements org.gradle.tooling.events.ProgressListener {
    private UpdateExecutionPageJob updateExecutionPageJob;
    private final ExecutionPage page;

    public ExecutionProgressListener(ExecutionPage page, Job executionJob) {
        this.page = page;
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
        initUpdaterJob();
        this.updateExecutionPageJob.addEvent(progressEvent);
    }

    private void initUpdaterJob() {
        if (this.updateExecutionPageJob == null) {
            this.updateExecutionPageJob = new UpdateExecutionPageJob(this.page);
            this.updateExecutionPageJob.schedule();
        }
    }
}
