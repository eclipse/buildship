package org.eclipse.buildship.core

import java.util.function.Function

import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.GradleProject

import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IWorkspaceRunnable
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.event.Event
import org.eclipse.buildship.core.internal.event.EventListener
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.internal.workspace.ProjectCreatedEvent

class GradleBuildConnectionConcurrencyTest extends ProjectSynchronizationSpecification {

    def "Concurrently executed actions runs sequentially"() {
        setup:
        List<ModelQueryJob> jobs = (0..4).collect {
                File location = dir("GradleBuildConnectionConcurrencyTest_$it") {
                    file 'build.gradle', 'Thread.sleep(500)'
                }
                BuildConfiguration buildConfiguration = BuildConfiguration.forRootProjectDirectory(location).build()
                GradleBuild gradleBuild = GradleCore.workspace.createBuild(buildConfiguration)
                new ModelQueryJob(gradleBuild)
            }

        when:
        jobs.each { it.schedule() }
        jobs.each { it.join() }

        List<Long> finishTimes = jobs.collect { it.finishTime }.sort()

        then:
        (1..3).each { assert finishTimes[it + 1] - finishTimes[it] > 500 }
    }

    def "Action requires workspace rule"() {
        setup:
        // import a sample project
        File location = dir('GradleBuildConnectionConcurrencyTest')
        BuildConfiguration buildConfiguration = BuildConfiguration.forRootProjectDirectory(location).build()
        GradleBuild gradleBuild = GradleCore.workspace.createBuild(buildConfiguration)
        Job modelQueryJob = new ModelQueryJob(gradleBuild)

        when:
        IWorkspaceRunnable modelQueryOperation = {
            modelQueryJob.schedule()
            try { // Eclipse 4.3 did not implement Job#join(timeout, monitor)
                waitFor(1000) { modelQueryJob.state == Job.NONE }
            } catch (RuntimeException e) {
            }
            assert modelQueryJob.state != Job.NONE
        }
        workspace.run(modelQueryOperation, workspace.root, IResource.NONE, new NullProgressMonitor())

        then:
        // synchronization won't start until the job with the workspace rule finishes
        waitFor(1000) { modelQueryJob.state == Job.NONE }
    }

    class ModelQueryJob extends Job {

        GradleBuild gradleBuild
        long finishTime

        ModelQueryJob(GradleBuild gradleBuild) {
            super('model query job')
            this.gradleBuild = gradleBuild;
        }

        protected IStatus run(IProgressMonitor monitor) {
            Function action = { ProjectConnection c -> c.model(GradleProject).get() }
            gradleBuild.withConnection(action, monitor)
            finishTime = System.currentTimeMillis()
            Status.OK_STATUS
        }
    }
}
