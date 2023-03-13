/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace

import org.eclipse.buildship.core.GradleCore
import org.eclipse.buildship.core.BuildConfiguration
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class MergingSynchronizeGradleBuildsJobs extends ProjectSynchronizationSpecification {

    def "Jobs with the same configuration are merged"() {
        setup:
        File projectLocation = dir("sample-project") {
            file 'settings.gradle'
        }
        def gradleBuild = gradleBuildFor(projectLocation)
        def jobs = [
            new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE, gradleBuild),
            new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE, gradleBuild),
        ]

        when:
        jobs.each { it.schedule() }
        waitForGradleJobsToFinish()

        then:
        jobs.findAll { it.result != null }.size() == 1
    }

    def "A no-op new project handler is covered by any other"() {
        setup:
        File projectLocation = dir("sample-project") {
            file 'settings.gradle'
        }
        def gradleBuild = gradleBuildFor(projectLocation)
        def jobs = [
            new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE, gradleBuild),
            new SynchronizationJob(NewProjectHandler.NO_OP, gradleBuild),
        ]

        when:
        jobs.each { it.schedule() }
        waitForGradleJobsToFinish()

        then:
        jobs.findAll { it.result != null }.size() == 1
    }

    def "A job with a different new project handler is not covered"() {
        setup:
        File projectLocation = dir("sample-project") {
            file 'settings.gradle'
        }
        def gradleBuild = gradleBuildFor(projectLocation)
        def jobs = [
            new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE, gradleBuild),
            new SynchronizationJob(Mock(NewProjectHandler), gradleBuild),
        ]

        when:
        jobs.each { it.schedule() }
        waitForGradleJobsToFinish()

        then:
        jobs.findAll { it.result != null }.size() == 2
    }

    def "A job for a different set of projects is not covered"() {
        setup:
        def gradleBuild1 = gradleBuildFor(dir("project1"))
        def gradleBuild2 = gradleBuildFor(dir("project2"))
        def jobs = [
            new SynchronizationJob(NewProjectHandler.NO_OP, gradleBuild1),
            new SynchronizationJob(NewProjectHandler.NO_OP, gradleBuild2),
        ]

        when:
        jobs.each { it.schedule() }
        waitForGradleJobsToFinish()

        then:
        jobs.findAll { it.result != null }.size() == 2
    }
}
