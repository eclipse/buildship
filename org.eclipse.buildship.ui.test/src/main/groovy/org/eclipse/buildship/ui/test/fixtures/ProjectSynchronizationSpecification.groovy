package org.eclipse.buildship.ui.test.fixtures

import com.google.common.util.concurrent.FutureCallback

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.util.Pair

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.jobs.Job

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration
import org.eclipse.buildship.core.projectimport.ProjectPreviewJob
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.workspace.NewProjectHandler

abstract class ProjectSynchronizationSpecification extends WorkspaceSpecification {
    protected void synchronizeAndWait(File location, NewProjectHandler newProjectHandler = NewProjectHandler.IMPORT_AND_MERGE) {
        startSynchronization(location, GradleDistribution.fromBuild(), newProjectHandler)
        waitForGradleJobsToFinish()
    }

    protected void importAndWait(File location, GradleDistribution distribution = GradleDistribution.fromBuild()) {
        startSynchronization(location, distribution, NewProjectHandler.IMPORT_AND_MERGE)
        waitForGradleJobsToFinish()
    }

    private void startSynchronization(File location, GradleDistribution distribution = GradleDistribution.fromBuild(), NewProjectHandler newProjectHandler = NewProjectHandler.IMPORT_AND_MERGE) {
        FixedRequestAttributes attributes = new FixedRequestAttributes(location, null, distribution, null, [], [])
        CorePlugin.gradleWorkspaceManager().getGradleBuild(attributes).synchronize(newProjectHandler)
    }

    protected void importExistingAndWait(File location) {
        def description = workspace.newProjectDescription(location.name)
        description.setLocation(new Path(location.path))
        def project = workspace.root.getProject(location.name)
        project.create(description, null)
        project.open(null)
        waitForResourceChangeEvents()
        waitForGradleJobsToFinish()
    }

    protected void synchronizeAndWait(IProject... projects) {
        CorePlugin.gradleWorkspaceManager().getCompositeBuild().synchronize(NewProjectHandler.IMPORT_AND_MERGE)
        waitForGradleJobsToFinish()
    }

    protected void previewAndWait(File location, FutureCallback<Pair<OmniBuildEnvironment, OmniGradleBuildStructure>> resultHandler) {
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

}
