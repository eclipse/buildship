package org.eclipse.buildship.core.workspace.internal

import groovy.lang.DelegatesTo;

import com.gradleware.tooling.junit.TestDirectoryProvider;

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.IJavaProject

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.test.fixtures.BuildshipTestSpecification
import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.test.fixtures.GradleModel
import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.workspace.ExistingDescriptorHandler

abstract class ProjectSynchronizationSpecification extends BuildshipTestSpecification {
    protected def executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(GradleModel gradleModel, ExistingDescriptorHandler existingDescriptorHandler = ExistingDescriptorHandler.ALWAYS_KEEP) {
        // Note: executing the synchronizeGradleProjectWithWorkspaceProject() in a new job is necessary
        // as the jdt operations expect that all modifications are guarded by proper rules. For the sake
        // of this test class we simply use the workspace root as the job rule.
        Job job = new Job('') {
            protected IStatus run(IProgressMonitor monitor) {
                Job.jobManager.beginRule(LegacyEclipseSpockTestHelper.workspace.root, monitor)
                new DefaultWorkspaceGradleOperations().synchronizeGradleBuildWithWorkspace(gradleModel.build, gradleModel.attributes, [], existingDescriptorHandler, new NullProgressMonitor())
                Job.jobManager.endRule(LegacyEclipseSpockTestHelper.workspace.root)
                Status.OK_STATUS
            }
        }
        job.schedule()
        job.join()
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

    protected GradleModel loadGradleModel(String location) {
        GradleModel.fromProject(folder(location))
    }

    protected IProject findProject(String name) {
        CorePlugin.workspaceOperations().findProjectByName(name).orNull()
    }

}
