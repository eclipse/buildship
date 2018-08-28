package org.eclipse.buildship.core.internal.test.fixtures

import org.gradle.tooling.GradleConnector

import com.google.common.base.Optional
import com.google.common.base.Preconditions

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IWorkspaceRunnable
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.jobs.Job

import org.eclipse.buildship.core.GradleCore
import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration
import org.eclipse.buildship.core.internal.util.gradle.GradleDistribution
import org.eclipse.buildship.core.internal.workspace.NewProjectHandler


abstract class ProjectSynchronizationSpecification extends WorkspaceSpecification {

    protected static final GradleDistribution DEFAULT_DISTRIBUTION = GradleDistribution.fromBuild()

    protected void synchronizeAndWait(File location) {
        Optional<IProject> project = CorePlugin.workspaceOperations().findProjectByLocation(location.canonicalFile)
        Preconditions.checkState(project.present, "Workspace does not have project located at ${location.absolutePath}")
        synchronizeAndWait(project.get())
    }

    protected void synchronizeAndWait(IProject project) {
        GradleCore.workspace.getBuild(project).synchronize(new NullProgressMonitor())
        waitForGradleJobsToFinish()
        waitForResourceChangeEvents()
    }

    protected void importAndWait(File location, GradleDistribution distribution = DEFAULT_DISTRIBUTION) {
        BuildConfiguration buildConfiguration = createOverridingBuildConfiguration(location, distribution)
        CorePlugin.gradleWorkspaceManager().getGradleBuild(buildConfiguration).synchronize(NewProjectHandler.IMPORT_AND_MERGE, GradleConnector.newCancellationTokenSource(), new NullProgressMonitor())
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
