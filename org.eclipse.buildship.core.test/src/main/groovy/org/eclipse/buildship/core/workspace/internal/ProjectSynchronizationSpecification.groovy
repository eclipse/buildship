package org.eclipse.buildship.core.workspace.internal

import com.google.common.util.concurrent.FutureCallback

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.util.Pair

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.jobs.Job

import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration
import org.eclipse.buildship.core.projectimport.ProjectPreviewJob
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.workspace.ExistingDescriptorHandler
import org.eclipse.buildship.core.workspace.SynchronizeGradleProjectJob
import org.eclipse.buildship.core.workspace.SynchronizeGradleProjectsJob

abstract class ProjectSynchronizationSpecification extends WorkspaceSpecification {
    protected def synchronizeAndWait(File location, ExistingDescriptorHandler existingDescriptorHandler = ExistingDescriptorHandler.ALWAYS_KEEP) {
        def job = newSynchronizationJob(location, GradleDistribution.fromBuild(), existingDescriptorHandler)
        job.schedule()
        job.join()
    }

    protected def synchronizeAndWait(IProject... projects) {
        def job = new SynchronizeGradleProjectsJob(projects as List)
        job.schedule()
        job.join()
    }

    protected def importAndWait(File location, GradleDistribution distribution = GradleDistribution.fromBuild()) {
        def job = newSynchronizationJob(location, distribution, ExistingDescriptorHandler.ALWAYS_KEEP)
        job.schedule()
        job.join()
    }

    protected SynchronizeGradleProjectJob newSynchronizationJob(File location, GradleDistribution distribution = GradleDistribution.fromBuild(), ExistingDescriptorHandler existingDescriptorHandler = ExistingDescriptorHandler.ALWAYS_KEEP) {
        def attributes = new FixedRequestAttributes(location, null, distribution, null, [], [])
        new SynchronizeGradleProjectJob(attributes, [], existingDescriptorHandler, AsyncHandler.NO_OP)
    }

    protected def previewAndWait(File location, FutureCallback<Pair<OmniBuildEnvironment, OmniGradleBuildStructure>> resultHandler) {
        def job = newProjectPreviewJob(location, GradleDistribution.fromBuild(), resultHandler)
        job.schedule()
        job.join()
    }

    private ProjectPreviewJob newProjectPreviewJob(File location, GradleDistribution distribution, FutureCallback<Pair<OmniBuildEnvironment, OmniGradleBuildStructure>> resultHandler) {
        ProjectImportConfiguration configuration = new ProjectImportConfiguration()
        configuration.gradleDistribution = GradleDistributionWrapper.from(distribution)
        configuration.projectDir = location
        configuration.applyWorkingSets = true
        configuration.workingSets = []
        new ProjectPreviewJob(configuration, [], AsyncHandler.NO_OP, resultHandler)
    }

    protected def waitForJobsToFinish() {
        while (!Job.jobManager.isIdle()) {
            delay(100)
        }
    }

    protected def delay(long waitTimeMillis) {
        Thread.sleep(waitTimeMillis)
    }
}
