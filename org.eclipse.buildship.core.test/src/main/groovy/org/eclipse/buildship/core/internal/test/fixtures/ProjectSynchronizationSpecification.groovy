package org.eclipse.buildship.core.test.fixtures

import org.gradle.tooling.GradleConnector

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IWorkspaceRunnable
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.jobs.Job

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.BuildConfiguration
import org.eclipse.buildship.core.util.gradle.GradleDistribution
import org.eclipse.buildship.core.workspace.NewProjectHandler


abstract class ProjectSynchronizationSpecification extends WorkspaceSpecification {

    protected static final GradleDistribution DEFAULT_DISTRIBUTION = GradleDistribution.fromBuild()

    protected void synchronizeAndWait(File location, NewProjectHandler newProjectHandler = NewProjectHandler.IMPORT_AND_MERGE) {
        startSynchronization(location, DEFAULT_DISTRIBUTION, newProjectHandler)
        waitForGradleJobsToFinish()
        waitForResourceChangeEvents()
    }

    protected void importAndWait(File location, GradleDistribution distribution = DEFAULT_DISTRIBUTION) {
        startSynchronization(location, distribution, NewProjectHandler.IMPORT_AND_MERGE)
        waitForGradleJobsToFinish()
        waitForResourceChangeEvents()
    }

    protected void importProject(File location, GradleDistribution distribution = DEFAULT_DISTRIBUTION) {
        startSynchronization(location, distribution, NewProjectHandler.IMPORT_AND_MERGE)
    }

    protected void startSynchronization(File location, GradleDistribution distribution = DEFAULT_DISTRIBUTION, NewProjectHandler newProjectHandler = NewProjectHandler.IMPORT_AND_MERGE) {
        BuildConfiguration buildConfiguration = createOverridingBuildConfiguration(location, distribution)
        CorePlugin.gradleWorkspaceManager().getGradleBuild(buildConfiguration).synchronize(newProjectHandler, GradleConnector.newCancellationTokenSource(), new NullProgressMonitor())
    }

    protected void synchronizeAndWait(IProject... projects) {
        CorePlugin.gradleWorkspaceManager().getGradleBuilds(projects as Set).synchronize(NewProjectHandler.IMPORT_AND_MERGE, GradleConnector.newCancellationTokenSource(), new NullProgressMonitor())
        waitForGradleJobsToFinish()
        waitForResourceChangeEvents()
    }

    protected def waitForGradleJobsToFinish() {
        Job.jobManager.join(CorePlugin.GRADLE_JOB_FAMILY, null)
    }

    protected void waitForResourceChangeEvents() {
        workspace.run({} as IWorkspaceRunnable, null, IResource.NONE, null);
    }
}
