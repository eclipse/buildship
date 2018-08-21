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

package org.eclipse.buildship.core.internal.util.progress;

import java.util.Queue;

import com.google.common.base.Preconditions;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.EvictingQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

/**
 * {@link ProgressListener} implementation which delegates all Gradle {@link ProgressEvent} to a
 * target {@link IProgressMonitor}. The Tooling API does not provide up-front information about how
 * many work units will be needed. To give the user some perceived progress, this class will use a
 * logarithmic approach. Every new message will lead to 1/100 of the remaining progress to be consumed.
 * As a result, the bar will start out reasonably fast and slow down towards the end for bigger projects.
 */
public final class DelegatingProgressListener implements ProgressListener {

    private final SubMonitor monitor;
    private final Predicate<? super ProgressEvent> eventFilter;

    private DelegatingProgressListener(IProgressMonitor monitor, Predicate<? super ProgressEvent> eventFilter) {
        this.monitor = SubMonitor.convert(monitor);
        this.eventFilter = Preconditions.checkNotNull(eventFilter);
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
        this.monitor.setWorkRemaining(100);
        this.monitor.worked(1);
        this.monitor.subTask(event.getDescription());
    }

    /**
     * Creates a new {@link ProgressListener} that will forward all progress messages to the
     * provided {@link IProgressMonitor}.
     *
     * @param monitor the monitor to delegate to, may be null
     * @return the progress listener, never null
     */
    public static ProgressListener withFullOutput(IProgressMonitor monitor) {
        return new DelegatingProgressListener(monitor, Predicates.alwaysTrue());
    }

    /**
     * Creates a new {@link ProgressListener} that will forward a filtered set of
     * progress messages to the provided {@link IProgressMonitor}.
     *
     * The progress stream from Gradle contains a lot of duplicate log messages.
     * For instance, between configuring each subproject, Gradle inserts a
     * "configuring projects" message.
     *
     * Such duplicate lifecycle messages will not be forwarded by this listener.
     *
     * @param monitor the monitor to delegate to, may be null
     * @return the progress listener, never null
     *
     */
    public static ProgressListener withoutDuplicateLifecycleEvents(IProgressMonitor monitor) {
        Predicate<ProgressEvent> withoutDuplicates = new Predicate<ProgressEvent>() {
            final Queue<String> recentlySeen = EvictingQueue.create(10);

            @Override
            public boolean apply(ProgressEvent event) {
                String description = event.getDescription();
                boolean shouldShow = !this.recentlySeen.contains(description);
                this.recentlySeen.add(description);
                return shouldShow;
            }
        };
        return new DelegatingProgressListener(monitor, withoutDuplicates);
    }

}
