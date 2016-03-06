package org.eclipse.buildship.core.workspace.internal

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import com.google.common.collect.ImmutableList

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes

import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectBuilder
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification;
import org.eclipse.buildship.core.test.fixtures.TestEnvironment
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.util.variable.ExpressionUtils
import org.eclipse.buildship.core.workspace.WorkspaceGradleOperations;;

class ImportingProjectInDefaultLocation extends ProjectSynchronizationSpecification {

    def "Can import deleted project located in default location"() {
        setup:
        def workspaceOperations = CorePlugin.workspaceOperations()
        def workspaceRootLocation = workspace.root.location.toFile()
        def location = new File(workspaceRootLocation, 'projectname')
        location.mkdirs()

        def project = workspaceOperations.createProject("projectname", location, ImmutableList.of(), new NullProgressMonitor())
        project.delete(false, true, new NullProgressMonitor())

        when:
        synchronizeAndWait(workspaceDir("projectname"))

        then:
        workspaceOperations.allProjects.size() == 1
    }

    def "Can import project located in workspace folder and with custom root name"() {
        setup:
        File rootProject = newProjectWithCustomNameInWorkspaceFolder()

        when:
        synchronizeAndWait(rootProject)

        then:
        workspace.root.projects.length == 1
        def project = workspace.root.projects[0]
        def locationExpression = ExpressionUtils.encodeWorkspaceLocation(project)
        def decodedLocation = ExpressionUtils.decode(locationExpression)
        rootProject.equals(new File(decodedLocation))

        cleanup:
        rootProject.deleteDir()
    }

    def File newProjectWithCustomNameInWorkspaceFolder() {
        workspaceDir('Bug472223') {
            file 'settings.gradle', "rootProject.name = 'my-project-name-is-different-than-the-folder'"
        }
    }

}
