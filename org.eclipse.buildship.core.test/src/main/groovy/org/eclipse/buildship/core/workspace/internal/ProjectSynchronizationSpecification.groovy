package org.eclipse.buildship.core.workspace.internal

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes

import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.IJavaProject

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.test.fixtures.BuildshipTestSpecification
import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.workspace.ExistingDescriptorHandler
import org.eclipse.buildship.core.workspace.SynchronizeGradleProjectJob

abstract class ProjectSynchronizationSpecification extends BuildshipTestSpecification {
    protected def synchronizeAndWait(File location, ExistingDescriptorHandler existingDescriptorHandler = ExistingDescriptorHandler.ALWAYS_KEEP) {
        def job = newProjectImportJob(location, existingDescriptorHandler)
        job.schedule()
        job.join()
    }

    private SynchronizeGradleProjectJob newProjectImportJob(File location, ExistingDescriptorHandler existingDescriptorHandler) {
        def attributes = new FixedRequestAttributes(location, null, GradleDistribution.fromBuild(), null, [], [])
        new SynchronizeGradleProjectJob(attributes, [], existingDescriptorHandler, AsyncHandler.NO_OP)
    }

    protected IProject newClosedProject(String name) {
        EclipseProjects.newClosedProject(name, folder(name))
    }

    protected IProject newOpenProject(String name) {
        EclipseProjects.newProject(name, folder(name))
    }

    protected IJavaProject newJavaProject(String name) {
        EclipseProjects.newJavaProject(name, folder(name))
    }

    protected File fileTree(@DelegatesTo(value = FileTreeBuilder, strategy = Closure.DELEGATE_FIRST) Closure config) {
        return new FileTreeBuilder(externalTestFolder).call(config)
    }

    protected File fileTree(String projectName, @DelegatesTo(value = FileTreeBuilder, strategy = Closure.DELEGATE_FIRST) Closure config) {
        return new FileTreeBuilder(externalTestFolder).dir(projectName, config)
    }

    protected IProject findProject(String name) {
        CorePlugin.workspaceOperations().findProjectByName(name).orNull()
    }

}
