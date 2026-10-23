/*******************************************************************************
 * Copyright (c) 2026 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.progress;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.gradle.tooling.events.StatusEvent;
import org.gradle.tooling.events.download.FileDownloadOperationDescriptor;

import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * {@link ProgressListener} implementation which shows the byte-level progress of file downloads
 * (e.g. dependency jars) as the sub-task of the target {@link IProgressMonitor}. Without this, the
 * monitor only shows the name of the file being downloaded, and a large download is
 * indistinguishable from a hang.
 * <p>
 * Gradle sends a {@link StatusEvent} for every few kilobytes downloaded, so the updates are rate
 * limited. The amount of work reported to the monitor is not changed.
 */
public final class FileDownloadProgressListener implements ProgressListener {

    private static final long UPDATE_INTERVAL_NANOS = TimeUnit.MILLISECONDS.toNanos(250);

    private final IProgressMonitor monitor;
    private final LongSupplier nanoClock;
    private long lastUpdate;
    private boolean updated;

    public FileDownloadProgressListener(IProgressMonitor monitor) {
        this(monitor, System::nanoTime);
    }

    FileDownloadProgressListener(IProgressMonitor monitor, LongSupplier nanoClock) {
        this.monitor = Preconditions.checkNotNull(monitor);
        this.nanoClock = Preconditions.checkNotNull(nanoClock);
    }

    @Override
    public void statusChanged(ProgressEvent event) {
        if (!(event instanceof StatusEvent) || !(event.getDescriptor() instanceof FileDownloadOperationDescriptor)) {
            return;
        }
        if (this.monitor.isCanceled()) {
            return;
        }
        long now = this.nanoClock.getAsLong();
        if (this.updated && now - this.lastUpdate < UPDATE_INTERVAL_NANOS) {
            return;
        }
        this.lastUpdate = now;
        this.updated = true;

        StatusEvent status = (StatusEvent) event;
        URI uri = ((FileDownloadOperationDescriptor) event.getDescriptor()).getUri();
        this.monitor.subTask(String.format("Download %s (%s)", fileName(uri), formatProgress(status.getProgress(), status.getTotal())));
    }

    private static String fileName(URI uri) {
        String path = uri.getPath();
        if (path == null || path.isEmpty() || path.endsWith("/")) {
            return uri.toString();
        }
        return path.substring(path.lastIndexOf('/') + 1);
    }

    static String formatProgress(long progress, long total) {
        if (total <= 0) {
            return formatBytes(progress);
        }
        return formatBytes(progress) + " / " + formatBytes(total);
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1000) {
            return bytes + " B";
        } else if (bytes < 1000 * 1000) {
            return String.format("%.1f kB", bytes / 1e3);
        } else {
            return String.format("%.1f MB", bytes / 1e6);
        }
    }
}
