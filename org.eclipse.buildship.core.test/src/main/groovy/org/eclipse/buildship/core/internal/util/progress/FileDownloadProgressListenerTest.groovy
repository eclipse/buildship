/*******************************************************************************
 * Copyright (c) 2026 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.progress

import java.util.concurrent.TimeUnit

import org.gradle.tooling.events.OperationDescriptor
import org.gradle.tooling.events.StatusEvent
import org.gradle.tooling.events.download.FileDownloadOperationDescriptor
import org.gradle.tooling.events.download.FileDownloadStartEvent
import spock.lang.Specification

import org.eclipse.core.runtime.IProgressMonitor

class FileDownloadProgressListenerTest extends Specification {

    static final String JAR_URI = 'https://repo.example.org/libs/foo/1.0/foo-1.0.jar'

    Locale defaultLocale
    IProgressMonitor monitor = Mock(IProgressMonitor)
    long time = 0
    FileDownloadProgressListener listener = new FileDownloadProgressListener(monitor, { time })

    def setup() {
        defaultLocale = Locale.default
        Locale.default = Locale.US
    }

    def cleanup() {
        Locale.default = defaultLocale
    }

    def "Shows file name and downloaded bytes as sub-task"() {
        when:
        listener.statusChanged(downloadStatus(JAR_URI, progress, total))

        then:
        1 * monitor.subTask("Download foo-1.0.jar ($expected)")

        where:
        progress   | total      | expected
        12_300_000 | 37_400_000 | '12.3 MB / 37.4 MB'
        2_500      | 900_000    | '2.5 kB / 900.0 kB'
        512        | 1_024      | '512 B / 1.0 kB'
        1_500_000  | -1         | '1.5 MB'
        1_500_000  | 0          | '1.5 MB'
    }

    def "Updates are rate limited"() {
        when:
        listener.statusChanged(downloadStatus(JAR_URI, 1_000, 10_000))
        time += TimeUnit.MILLISECONDS.toNanos(100)
        listener.statusChanged(downloadStatus(JAR_URI, 2_000, 10_000))
        time += TimeUnit.MILLISECONDS.toNanos(200)
        listener.statusChanged(downloadStatus(JAR_URI, 3_000, 10_000))

        then:
        1 * monitor.subTask('Download foo-1.0.jar (1.0 kB / 10.0 kB)')
        0 * monitor.subTask('Download foo-1.0.jar (2.0 kB / 10.0 kB)')
        1 * monitor.subTask('Download foo-1.0.jar (3.0 kB / 10.0 kB)')
    }

    def "Ignores events other than file download status events"() {
        setup:
        FileDownloadStartEvent startEvent = Mock(FileDownloadStartEvent)
        startEvent.descriptor >> downloadDescriptor(JAR_URI)
        StatusEvent otherStatusEvent = Mock(StatusEvent)
        otherStatusEvent.descriptor >> Mock(OperationDescriptor)
        otherStatusEvent.progress >> 1
        otherStatusEvent.total >> 2

        when:
        listener.statusChanged(startEvent)
        listener.statusChanged(otherStatusEvent)

        then:
        0 * monitor.subTask(_)
    }

    def "Does nothing if the monitor is cancelled"() {
        setup:
        monitor.isCanceled() >> true

        when:
        listener.statusChanged(downloadStatus(JAR_URI, 1, 2))

        then:
        0 * monitor.subTask(_)
    }

    private StatusEvent downloadStatus(String uri, long progress, long total) {
        StatusEvent event = Mock(StatusEvent)
        event.descriptor >> downloadDescriptor(uri)
        event.progress >> progress
        event.total >> total
        event.unit >> 'bytes'
        event
    }

    private FileDownloadOperationDescriptor downloadDescriptor(String uri) {
        FileDownloadOperationDescriptor descriptor = Mock(FileDownloadOperationDescriptor)
        descriptor.uri >> new URI(uri)
        descriptor.name >> "Download $uri"
        descriptor
    }
}
