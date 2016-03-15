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

import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

/**
 * {@link ProgressListener} implementation which delegates all Gradle {@link ProgressEvent} to a target Eclipse
 * monitor.
 * <p>
 *
 * @see IProgressMonitor
 */
public final class DelegatingProgressListener implements ProgressListener {

    private final SubMonitor monitor;
    private final Predicate<ProgressEvent> eventFilter;

    /**
     * Creates a new progress listener that will forward all events.
     *
     * @param monitor the Eclipse target monitor to delegate to
     */
    public DelegatingProgressListener(IProgressMonitor monitor) {
        this(monitor, Predicates.<ProgressEvent> alwaysTrue());
    }

    /**
     * Creates a new progress listener that will forward the events matched by the given filter.
     *
     * @param monitor the Eclipse target monitor to delegate to
     * @param eventFilter a filter for which progress events to forward
     */
    public DelegatingProgressListener(IProgressMonitor monitor, Predicate<ProgressEvent> eventFilter) {
        this.monitor = SubMonitor.convert(monitor);
        this.eventFilter = eventFilter;
    }

    /**
     * Delegates the event to the current Eclipse target monitor.
     *
     * @param event the event to delegate
     */
    @Override
    public void statusChanged(ProgressEvent event) {
        if (this.monitor.isCanceled()) {
            return;
        }
        if (!this.eventFilter.apply(event)) {
            return;
        }
        this.monitor.subTask(event.getDescription());
    }

}
