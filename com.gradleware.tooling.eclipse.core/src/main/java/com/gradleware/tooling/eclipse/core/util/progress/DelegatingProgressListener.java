/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package com.gradleware.tooling.eclipse.core.util.progress;

import org.eclipse.core.runtime.IProgressMonitor;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProgressListener;

/**
 * {@link ProgressListener} implementation which delegates all Gradle {@link ProgressEvent}s to a
 * target Eclipse monitor.
 * <p>
 * While there is no target progress monitor set, the most recent progress message is preserved and
 * automatically assigned once a monitor is set.
 *
 * @see IProgressMonitor
 */
public final class DelegatingProgressListener implements ProgressListener {

    public final Object LOCK = new Object();

    private IProgressMonitor monitor;
    private String lastMessage;

    /**
     * Creates a new progress listener without an initial target Eclipse monitor.
     */
    public DelegatingProgressListener() {
        this.monitor = null;
    }

    /**
     * Creates a new progress listener with the given Eclipse target monitor.
     *
     * @param monitor the Eclipse target monitor to delegate to
     */
    public DelegatingProgressListener(IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Sets the given Eclipse target monitor.
     *
     * @param monitor the Eclipse target monitor to delegate to
     */
    public void setMonitor(IProgressMonitor monitor) {
        synchronized (this.LOCK) {
            this.monitor = monitor;
            if (this.monitor != null) {
                this.monitor.subTask(this.lastMessage);
            }
        }
    }

    /**
     * Delegates the event to the current Eclipse target monitor.
     *
     * @param event the event to delegate
     */
    @Override
    public void statusChanged(ProgressEvent event) {
        synchronized (this.LOCK) {
            if (this.monitor != null) {
                // if the monitor is present, then create a sub task for each progress event
                this.monitor.subTask(event.getDescription());
            } else {
                // if a monitor is not present, preserve the last event
                this.lastMessage = event.getDescription();
            }
        }
    }

}
