package org.eclipse.buildship.core.workspace.internal

import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.workspace.NewProjectHandler
import org.eclipse.buildship.core.workspace.SynchronizationJob

class MergingSynchronizeGradleBuildsJobs extends ProjectSynchronizationSpecification {

    def "Jobs with the same configuration are merged"() {
        setup:
        File projectLocation = dir("sample-project") {
            file 'settings.gradle'
        }

        def buildConfiguration = createOverridingBuildConfiguration(projectLocation)
        def gradleBuild = new DefaultGradleBuild(buildConfiguration)
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
        def buildConfiguration = createOverridingBuildConfiguration(projectLocation)
        def gradleBuild = new DefaultGradleBuild(buildConfiguration)
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
        def buildConfiguration = createOverridingBuildConfiguration(projectLocation)
        def gradleBuild = new DefaultGradleBuild(buildConfiguration)
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
        File project1 = dir("project1")
        File project2 = dir("project2")
        def buildConfiguration1 = createOverridingBuildConfiguration(project1)
        def gradleBuild1 = new DefaultGradleBuild(buildConfiguration1)
        def buildConfiguration2 = createOverridingBuildConfiguration(project2)
        def gradleBuild2 = new DefaultGradleBuild(buildConfiguration2)
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
