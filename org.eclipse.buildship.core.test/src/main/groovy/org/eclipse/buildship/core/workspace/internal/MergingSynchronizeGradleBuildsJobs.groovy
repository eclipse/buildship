package org.eclipse.buildship.core.workspace.internal

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.util.progress.SynchronizationJob
import org.eclipse.buildship.core.util.progress.ToolingApiStatus
import org.eclipse.buildship.core.workspace.NewProjectHandler

class MergingSynchronizeGradleBuildsJobs extends ProjectSynchronizationSpecification {

    def "Jobs with the same configuration are merged"() {
        setup:
        File projectLocation = dir("sample-project") {
            file 'settings.gradle'
        }

        def buildConfiguration = createOverridingBuildConfiguration(projectLocation)
        def gradleBuild = new DefaultGradleBuild(buildConfiguration)
        def jobs = [
            new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE, AsyncHandler.NO_OP, gradleBuild) { void handleStatus(ToolingApiStatus status) {} } ,
            new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE, AsyncHandler.NO_OP, gradleBuild) { void handleStatus(ToolingApiStatus status) {} } ,
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
            new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE, AsyncHandler.NO_OP, gradleBuild) { void handleStatus(ToolingApiStatus status) {} } ,
            new SynchronizationJob(NewProjectHandler.NO_OP, AsyncHandler.NO_OP, gradleBuild) { void handleStatus(ToolingApiStatus status) {} } ,
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
            new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE, AsyncHandler.NO_OP, gradleBuild) { void handleStatus(ToolingApiStatus status) {} } ,
            new SynchronizationJob(Mock(NewProjectHandler), AsyncHandler.NO_OP, gradleBuild) { void handleStatus(ToolingApiStatus status) {} } ,
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
        def buildConfiguration = createOverridingBuildConfiguration(projectLocation)
        def gradleBuild = new DefaultGradleBuild(buildConfiguration)
        def jobs = [
            new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE, {monitor, token -> "Foo"}, gradleBuild) { void handleStatus(ToolingApiStatus status) {} } ,
            new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE, AsyncHandler.NO_OP, gradleBuild) { void handleStatus(ToolingApiStatus status) {} } ,
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
        def buildConfiguration = createOverridingBuildConfiguration(projectLocation)
        def gradleBuild = new DefaultGradleBuild(buildConfiguration)
        def jobs = [
            new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE, {monitor, token -> "Foo"}, gradleBuild) { void handleStatus(ToolingApiStatus status) {} } ,
            new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE, {monitor, token -> "Bar"}, gradleBuild) { void handleStatus(ToolingApiStatus status) {} } ,
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
            new SynchronizationJob(NewProjectHandler.NO_OP, AsyncHandler.NO_OP, gradleBuild1) { void handleStatus(ToolingApiStatus status) {} } ,
            new SynchronizationJob(NewProjectHandler.NO_OP, AsyncHandler.NO_OP, gradleBuild2) { void handleStatus(ToolingApiStatus status) {} } ,
        ]

        when:
        jobs.each { it.schedule() }
        waitForGradleJobsToFinish()

        then:
        jobs.findAll { it.result != null }.size() == 2
    }
}
