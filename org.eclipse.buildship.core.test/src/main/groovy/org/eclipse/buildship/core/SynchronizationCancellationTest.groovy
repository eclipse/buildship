/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core

import java.util.concurrent.CountDownLatch

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.jobs.Job

import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class SynchronizationCancellationTest extends ProjectSynchronizationSpecification {

    File location

    def setup() {
        location = dir('app') {
            file 'build.gradle', '''
                Thread.sleep(300)
            '''
        }
    }

    def "Can cancel import"() {
        setup:
        CountDownLatch latch = new CountDownLatch(1)
        Job job = new SyncJob(latch, location)

        when:
        job.schedule()
        latch.await()
        job.cancel()
        job.join()

        then:
        job.result.severity == IStatus.CANCEL
    }

    def "Can cancel synchronization"() {
        setup:
        importAndWait(location)
        CountDownLatch latch = new CountDownLatch(1)
        Job job = new SyncJob(latch, findProject('app'))

        when:
        job.schedule()
        latch.await()
        job.cancel()
        job.join()

        then:
        job.result.severity == IStatus.CANCEL
    }

    class SyncJob extends Job {

        CountDownLatch latch
        GradleBuild gradleBuild

        private SyncJob(CountDownLatch latch, File location) {
            this(latch, gradleBuildFor(location))
        }

        private SyncJob(CountDownLatch latch, IProject project) {
            this(latch, gradleBuildFor(project))
        }

        private SyncJob(CountDownLatch latch, GradleBuild gradleBuild) {
            super("Test sync job")
            this.latch = latch
            this.gradleBuild = gradleBuild
        }

        IStatus run(IProgressMonitor monitor) {
            latch.countDown()
            gradleBuild.synchronize(monitor).status
        }
    }
}
