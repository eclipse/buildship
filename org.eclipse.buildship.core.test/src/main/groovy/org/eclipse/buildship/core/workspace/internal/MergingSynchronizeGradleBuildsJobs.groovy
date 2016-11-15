package org.eclipse.buildship.core.workspace.internal

import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.util.configuration.FixedRequestAttributesBuilder
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.workspace.NewProjectHandler

class MergingSynchronizeGradleBuildsJobs extends ProjectSynchronizationSpecification {

    def "Jobs with the same configuration are merged"() {
        setup:
        File projectLocation = dir("sample-project") {
            file 'settings.gradle'
        }
        def requestAttributes = FixedRequestAttributesBuilder.fromEmptySettings(projectLocation).build()
        def gradleBuild = new DefaultGradleBuild(requestAttributes)
        def jobs = [
            SynchronizeGradleBuildsJob.forSingleGradleBuild(gradleBuild, NewProjectHandler.IMPORT_AND_MERGE, AsyncHandler.NO_OP),
            SynchronizeGradleBuildsJob.forSingleGradleBuild(gradleBuild, NewProjectHandler.IMPORT_AND_MERGE, AsyncHandler.NO_OP)
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
        def requestAttributes = FixedRequestAttributesBuilder.fromEmptySettings(projectLocation).build()
        def gradleBuild = new DefaultGradleBuild(requestAttributes)
        def jobs = [
            SynchronizeGradleBuildsJob.forSingleGradleBuild(gradleBuild, NewProjectHandler.IMPORT_AND_MERGE, AsyncHandler.NO_OP),
            SynchronizeGradleBuildsJob.forSingleGradleBuild(gradleBuild, NewProjectHandler.NO_OP, AsyncHandler.NO_OP)
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
        def requestAttributes = FixedRequestAttributesBuilder.fromEmptySettings(projectLocation).build()
        def gradleBuild = new DefaultGradleBuild(requestAttributes)
        def jobs = [
            SynchronizeGradleBuildsJob.forSingleGradleBuild(gradleBuild, NewProjectHandler.IMPORT_AND_MERGE, AsyncHandler.NO_OP),
            SynchronizeGradleBuildsJob.forSingleGradleBuild(gradleBuild, NewProjectHandler.IMPORT_AND_OVERWRITE, AsyncHandler.NO_OP)
        ]

        when:
        jobs.each { it.schedule() }
        waitForGradleJobsToFinish()

        then:
        jobs.findAll { it.result != null }.size() == 2
    }

    def "A no-op initializer is covered by any other"() {
        setup:
        File projectLocation = dir("sample-project") {
            file 'settings.gradle'
        }
        def requestAttributes = FixedRequestAttributesBuilder.fromEmptySettings(projectLocation).build()
        def gradleBuild = new DefaultGradleBuild(requestAttributes)
        def jobs = [
            SynchronizeGradleBuildsJob.forSingleGradleBuild(gradleBuild, NewProjectHandler.IMPORT_AND_MERGE, {monitor, token -> "Foo"}),
            SynchronizeGradleBuildsJob.forSingleGradleBuild(gradleBuild, NewProjectHandler.IMPORT_AND_MERGE, AsyncHandler.NO_OP)
        ]

        when:
        jobs.each { it.schedule() }
        waitForGradleJobsToFinish()

        then:
        jobs.findAll { it.result != null }.size() == 1
    }

    def "A job with a different initializer is not covered"() {
        setup:
        File projectLocation = dir("sample-project") {
            file 'settings.gradle'
        }
        def requestAttributes = FixedRequestAttributesBuilder.fromEmptySettings(projectLocation).build()
        def gradleBuild = new DefaultGradleBuild(requestAttributes)
        def jobs = [
            SynchronizeGradleBuildsJob.forSingleGradleBuild(gradleBuild, NewProjectHandler.IMPORT_AND_MERGE, {monitor, token -> "Foo"}),
            SynchronizeGradleBuildsJob.forSingleGradleBuild(gradleBuild, NewProjectHandler.IMPORT_AND_MERGE, {monitor, token -> "Bar"})
        ]

        when:
        jobs.each { it.schedule() }
        waitForGradleJobsToFinish()

        then:
        jobs.findAll { it.result != null }.size() == 2
    }

    def "A job for a different set of projects is not covered"() {
        setup:
        File project1 = dir("project1")
        File project2 = dir("project2")
        def attributes1 = FixedRequestAttributesBuilder.fromEmptySettings(project1).build()
        def gradleBuild1 = new DefaultGradleBuild(attributes1)
        def attributes2 = FixedRequestAttributesBuilder.fromEmptySettings(project2).build()
        def gradleBuild2 = new DefaultGradleBuild(attributes2)
        def jobs = [
            SynchronizeGradleBuildsJob.forSingleGradleBuild(gradleBuild1, NewProjectHandler.NO_OP, AsyncHandler.NO_OP),
            SynchronizeGradleBuildsJob.forSingleGradleBuild(gradleBuild2, NewProjectHandler.NO_OP, AsyncHandler.NO_OP)
        ]

        when:
        jobs.each { it.schedule() }
        waitForGradleJobsToFinish()

        then:
        jobs.findAll { it.result != null }.size() == 2
    }
}
