package org.eclipse.buildship.ui.internal.test.fixtures

import org.gradle.tooling.GradleConnector

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration
import org.eclipse.buildship.core.internal.util.gradle.GradleDistribution
import org.eclipse.buildship.core.internal.workspace.NewProjectHandler

abstract class ProjectSynchronizationSpecification extends WorkspaceSpecification {

    protected static final GradleDistribution DEFAULT_DISTRIBUTION = GradleDistribution.fromBuild()

    protected void synchronizeAndWait(File location, NewProjectHandler newProjectHandler = NewProjectHandler.IMPORT_AND_MERGE) {
        startSynchronization(location, GradleDistribution.fromBuild(), newProjectHandler)
        waitForGradleJobsToFinish()
    }

    protected void importAndWait(File location, GradleDistribution distribution = GradleDistribution.fromBuild()) {
        startSynchronization(location, distribution, NewProjectHandler.IMPORT_AND_MERGE)
        waitForGradleJobsToFinish()
    }

    protected void startSynchronization(File location, GradleDistribution distribution = DEFAULT_DISTRIBUTION, NewProjectHandler newProjectHandler = NewProjectHandler.IMPORT_AND_MERGE) {
        BuildConfiguration buildConfiguration = createOverridingBuildConfiguration(location, distribution)
        CorePlugin.gradleWorkspaceManager().getGradleBuild(buildConfiguration).synchronize(newProjectHandler, GradleConnector.newCancellationTokenSource(), new NullProgressMonitor())
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
        CorePlugin.gradleWorkspaceManager().getGradleBuilds(projects as Set).synchronize(NewProjectHandler.IMPORT_AND_MERGE, GradleConnector.newCancellationTokenSource(), new NullProgressMonitor())
        waitForGradleJobsToFinish()
    }
}
