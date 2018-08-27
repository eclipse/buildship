/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.util.progress;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;

/**
 * A progress monitor that only publishes the most recent task and sub task at a given rate. Can be used to
 * reduce pressure on the UI thread if an operation produces a lot of progress messages in a short
 * amount of time, without loosing the benefit of informing the user.
 *
 * @author Stefan Oehme
 */
public final class RateLimitingProgressMonitor extends ProgressMonitorWrapper {

    private final Timer timer;
    private final AtomicReference<String> lastTask;
    private final AtomicReference<String> lastSubTask;
    private final long rate;
    private final TimeUnit rateUnit;

    public RateLimitingProgressMonitor(IProgressMonitor monitor, long rate, TimeUnit rateUnit) {
        super(monitor);
        this.timer = new Timer();
        this.lastTask = new AtomicReference<String>();
        this.lastSubTask = new AtomicReference<String>();
        this.rate = rate;
        this.rateUnit = rateUnit;
    }

    @Override
    public void beginTask(String name, int totalWork) {
        this.timer.scheduleAtFixedRate(forwardMostRecentMessage(), 0, this.rateUnit.toMillis(this.rate));
        super.beginTask(name, totalWork);
    }

    @Override
    public void setTaskName(String name) {
        this.lastTask.set(name);
    }

    @Override
    public void subTask(String name) {
        this.lastSubTask.set(name);
    }

    @Override
    public void done() {
        this.timer.cancel();
        super.done();
    }

    private TimerTask forwardMostRecentMessage() {
        return new TimerTask() {

            @Override
            public void run() {
                String taskName = RateLimitingProgressMonitor.this.lastTask.getAndSet(null);
                if (taskName != null) {
                    RateLimitingProgressMonitor.super.setTaskName(taskName);
                }
                String subTaskName = RateLimitingProgressMonitor.this.lastSubTask.getAndSet(null);
                if (subTaskName != null) {
                    RateLimitingProgressMonitor.super.subTask(subTaskName);
                }
            }
        };
    }

}
