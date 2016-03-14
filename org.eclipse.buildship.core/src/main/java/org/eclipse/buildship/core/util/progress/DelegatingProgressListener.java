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

package org.eclipse.buildship.core.util.progress;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

/**
 * ProgressListener implementation which delegates all Gradle ProgressEvents to a target Eclipse monitor.
 * <p>
 * @see IProgressMonitor
 */
public final class DelegatingProgressListener implements org.gradle.tooling.ProgressListener, org.gradle.tooling.events.ProgressListener {

    private final SubMonitor monitor;

    /**
     * Creates a new progress listener with the given Eclipse target monitor.
     *
     * @param monitor the Eclipse target monitor to delegate to
     */
    public DelegatingProgressListener(IProgressMonitor monitor) {
        this.monitor = SubMonitor.convert(monitor);
    }

    /**
     * Delegates the event to the current Eclipse target monitor.
     *
     * Will only delegate start events and report their descriptor name
     * to match the UI metaphors of Eclipse (reporting what is in progress instead of what happened).
     *
     * @param event the event to delegate
     */
    @Override
    public void statusChanged(org.gradle.tooling.events.ProgressEvent event) {
        if (!(event instanceof org.gradle.tooling.events.StartEvent)) {
            return;
        }
        if (this.monitor.isCanceled()) {
            return;
        }
        this.monitor.subTask(event.getDescriptor().getName());
    }

    /**
     * Delegates the event to the current Eclipse target monitor.
     *
     * @param event the event to delegate
     */
    @Override
    public void statusChanged(org.gradle.tooling.ProgressEvent event) {
        if (this.monitor.isCanceled()) {
            return;
        }
        this.monitor.subTask(event.getDescription());
    }

}
