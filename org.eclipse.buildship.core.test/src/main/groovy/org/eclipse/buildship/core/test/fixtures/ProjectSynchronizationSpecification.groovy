package org.eclipse.buildship.core.test.fixtures

import com.google.common.util.concurrent.FutureCallback

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.util.Pair

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.jobs.Job

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration
import org.eclipse.buildship.core.projectimport.ProjectPreviewJob
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.workspace.NewProjectHandler
import org.eclipse.buildship.core.workspace.SynchronizeGradleProjectJob
import org.eclipse.buildship.core.workspace.SynchronizeGradleProjectsJob

abstract class ProjectSynchronizationSpecification extends WorkspaceSpecification {
    protected def synchronizeAndWait(File location, NewProjectHandler newProjectHandler = NewProjectHandler.IMPORT_AND_MERGE) {
        def job = newSynchronizationJob(location, GradleDistribution.fromBuild(), newProjectHandler)
        job.schedule()
        job.join()
    }

    protected def synchronizeAndWait(IProject... projects) {
        def job = new SynchronizeGradleProjectsJob(projects as List)
        job.schedule()
        job.join()
    }

    protected def importAndWait(File location, GradleDistribution distribution = GradleDistribution.fromBuild()) {
        def job = newSynchronizationJob(location, distribution, NewProjectHandler.IMPORT_AND_MERGE)
        job.schedule()
        job.join()
    }

    protected SynchronizeGradleProjectJob newSynchronizationJob(File location, GradleDistribution distribution = GradleDistribution.fromBuild(), NewProjectHandler newProjectHandler = NewProjectHandler.IMPORT_AND_MERGE) {
        def attributes = new FixedRequestAttributes(location, null, distribution, null, [], [])
        new SynchronizeGradleProjectJob(attributes, [], newProjectHandler, AsyncHandler.NO_OP)
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

    protected def waitForGradleJobsToFinish() {
        Job.jobManager.join(CorePlugin.GRADLE_JOB_FAMILY, null)
    }
}
