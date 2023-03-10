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
