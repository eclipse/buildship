package org.eclipse.buildship.core.workspace.internal

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes

import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.IJavaProject

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.workspace.ExistingDescriptorHandler
import org.eclipse.buildship.core.workspace.SynchronizeGradleProjectJob

abstract class ProjectSynchronizationSpecification extends WorkspaceSpecification {
    protected def synchronizeAndWait(File location, ExistingDescriptorHandler existingDescriptorHandler = ExistingDescriptorHandler.ALWAYS_KEEP) {
        def job = newProjectImportJob(location, existingDescriptorHandler)
        job.schedule()
        job.join()
    }

    private SynchronizeGradleProjectJob newProjectImportJob(File location, ExistingDescriptorHandler existingDescriptorHandler) {
        def attributes = new FixedRequestAttributes(location, null, GradleDistribution.fromBuild(), null, [], [])
        new SynchronizeGradleProjectJob(attributes, [], existingDescriptorHandler, AsyncHandler.NO_OP)
    }

}
