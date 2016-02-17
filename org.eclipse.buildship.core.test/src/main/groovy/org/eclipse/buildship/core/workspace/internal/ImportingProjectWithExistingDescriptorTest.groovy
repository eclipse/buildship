package org.eclipse.buildship.core.workspace.internal

import org.gradle.api.JavaVersion
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.configuration.internal.ProjectConfigurationPersistence
import org.eclipse.buildship.core.test.fixtures.BuildshipTestSpecification
import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.test.fixtures.FileStructure
import org.eclipse.buildship.core.test.fixtures.GradleModel
import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.workspace.ExistingDescriptorHandler
import org.eclipse.buildship.core.workspace.GradleClasspathContainer
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResourceFilterDescription
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.JavaModelException;

class ImportingProjectWithExistingDescriptorTest extends CoupledProjectSynchronizationSpecification {

    def "The project is added to the workspace"() {
        def project = newOpenProject("sample-project")
        project.delete(false, null)
        setup:
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        expect:
        CorePlugin.workspaceOperations().getAllProjects().isEmpty()

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        CorePlugin.workspaceOperations().allProjects.size() == 1
        findProject('sample-project')
    }

    def "If the project descriptor is overwritten on import, then all existing settings are removed"() {
        setup:
        IProject project = newJavaProject('sample-project').project
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
        fileStructure().create {
            file 'sample-project/build.gradle', """
            """
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel, ExistingDescriptorHandler.ALWAYS_OVERWRITE)

        then:
        project.hasNature(GradleProjectNature.ID)
        !project.hasNature(JavaCore.NATURE_ID)

        when:
        JavaCore.create(project).rawClasspath

        then:
        thrown JavaModelException
    }

    def "All subprojects with existing .project files are handled by the ExistingDescriptorHandler"() {
        setup:
        EclipseProjects.newProject('subproject-a', folder('sample-project/subproject-a'))
        EclipseProjects.newProject('subproject-b', folder('sample-project/subproject-b'))
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
        fileStructure().create {
            file 'sample-project/subproject-a/build.gradle'
            file 'sample-project/subproject-b/build.gradle'
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle', "include 'subproject-a', 'subproject-b'"
        }
        ExistingDescriptorHandler handler = Mock(ExistingDescriptorHandler)

        when:
        GradleModel gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel, handler)

        then:
        2 * handler.shouldOverwriteDescriptor(_)
    }

    @Override
    protected void prepareProject(String name) {
        def project = newOpenProject(name)
        project.delete(false, true, null)
    }

    @Override
    protected void prepareJavaProject(String name) {
        def project = newJavaProject(name).project
        project.delete(false, true, null)
    }
}
