package org.eclipse.buildship.core.workspace.internal

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes

import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.workspace.NewProjectHandler

class MergingSynchronizeGradleBuildsJobs extends ProjectSynchronizationSpecification {

    def "Jobs with the same configuration are merged"() {
        setup:
        File projectLocation = dir("sample-project") {
            file 'settings.gradle'
        }
        def requestAttributes = new FixedRequestAttributes(projectLocation, null, GradleDistribution.fromBuild(), null, [], [])
        def compositeBuild = new DefaultCompositeGradleBuild([requestAttributes] as Set)
        def jobs = [
            new SynchronizeCompositeBuildJob(compositeBuild, NewProjectHandler.IMPORT_AND_MERGE, AsyncHandler.NO_OP),
            new SynchronizeCompositeBuildJob(compositeBuild, NewProjectHandler.IMPORT_AND_MERGE, AsyncHandler.NO_OP)
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
        def requestAttributes = new FixedRequestAttributes(projectLocation, null, GradleDistribution.fromBuild(), null, [], [])
        def compositeBuild = new DefaultCompositeGradleBuild([requestAttributes] as Set)
        def jobs = [
            new SynchronizeCompositeBuildJob(compositeBuild, NewProjectHandler.IMPORT_AND_MERGE, AsyncHandler.NO_OP),
            new SynchronizeCompositeBuildJob(compositeBuild, NewProjectHandler.NO_OP, AsyncHandler.NO_OP)
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
        def requestAttributes = new FixedRequestAttributes(projectLocation, null, GradleDistribution.fromBuild(), null, [], [])
        def compositeBuild = new DefaultCompositeGradleBuild([requestAttributes] as Set)
        def jobs = [
            new SynchronizeCompositeBuildJob(compositeBuild, NewProjectHandler.IMPORT_AND_MERGE, AsyncHandler.NO_OP),
            new SynchronizeCompositeBuildJob(compositeBuild, NewProjectHandler.IMPORT_AND_OVERWRITE, AsyncHandler.NO_OP)
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
        def requestAttributes = new FixedRequestAttributes(projectLocation, null, GradleDistribution.fromBuild(), null, [], [])
        def compositeBuild = new DefaultCompositeGradleBuild([requestAttributes] as Set)
        def jobs = [
            new SynchronizeCompositeBuildJob(compositeBuild, NewProjectHandler.IMPORT_AND_MERGE, {monitor, token -> "Foo"}),
            new SynchronizeCompositeBuildJob(compositeBuild, NewProjectHandler.IMPORT_AND_MERGE, AsyncHandler.NO_OP)
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
        def requestAttributes = new FixedRequestAttributes(projectLocation, null, GradleDistribution.fromBuild(), null, [], [])
        def compositeBuild = new DefaultCompositeGradleBuild([requestAttributes] as Set)
        def jobs = [
            new SynchronizeCompositeBuildJob(compositeBuild, NewProjectHandler.IMPORT_AND_MERGE, {monitor, token -> "Foo"}),
            new SynchronizeCompositeBuildJob(compositeBuild, NewProjectHandler.IMPORT_AND_MERGE, {monitor, token -> "Bar"})
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
        def attributes1 = new FixedRequestAttributes(project1, null, GradleDistribution.fromBuild(), null, [], [])
        def compositeBuild1 = new DefaultCompositeGradleBuild([attributes1] as Set)
        def attributes2 = new FixedRequestAttributes(project2, null, GradleDistribution.fromBuild(), null, [], [])
        def compositeBuild2 = new DefaultCompositeGradleBuild([attributes2] as Set)
        def jobs = [
            new SynchronizeCompositeBuildJob(compositeBuild1, NewProjectHandler.NO_OP, AsyncHandler.NO_OP),
            new SynchronizeCompositeBuildJob(compositeBuild2, NewProjectHandler.NO_OP, AsyncHandler.NO_OP)
        ]

        when:
        jobs.each { it.schedule() }
        waitForGradleJobsToFinish()

        then:
        jobs.findAll { it.result != null }.size() == 2
    }
}
