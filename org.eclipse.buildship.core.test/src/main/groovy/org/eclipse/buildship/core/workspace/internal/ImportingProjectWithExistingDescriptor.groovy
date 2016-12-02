package org.eclipse.buildship.core.workspace.internal

import org.gradle.api.JavaVersion
import spock.lang.Ignore;

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.configuration.internal.ProjectConfigurationPersistence
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.workspace.NewProjectHandler
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

class ImportingProjectWithExistingDescriptor extends SingleProjectSynchronizationSpecification {

    def "The project is added to the workspace"() {
        def project = newProject("sample-project")
        project.delete(false, null)
        setup:
        def projectDir = dir('sample-project') {
            file 'settings.gradle'
        }

        expect:
        CorePlugin.workspaceOperations().getAllProjects().isEmpty()

        when:
        synchronizeAndWait(projectDir)

        then:
        CorePlugin.workspaceOperations().allProjects.size() == 1
        findProject('sample-project')
    }

    def "If the project descriptor is overwritten on import, then all existing settings are removed"() {
        setup:
        IProject project = newJavaProject('sample-project').project
        deleteAllProjects(false)
        def projectDir = dir('sample-project') {
            file 'settings.gradle'
        }

        when:
        synchronizeAndWait(projectDir, NewProjectHandler.IMPORT_AND_OVERWRITE)

        then:
        !project.hasNature(JavaCore.NATURE_ID)
        !project.getFile(".classpath").exists()
    }

    @Ignore("We will remove the whole merge feature soon")
    def "If the project descriptor is merged on import, then existing settings are kept"() {
        setup:
        IProject project = newJavaProject('sample-project').project
        deleteAllProjects(false)
        def projectDir = dir('sample-project') {
            file 'settings.gradle'
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        project.hasNature(JavaCore.NATURE_ID)
        JavaCore.create(project).rawClasspath
    }

    def "If the Gradle classpath container is missing, it is added"() {
        setup:
        IProject project = newJavaProject('sample-project').project
        deleteAllProjects(false)
        def projectDir = dir('sample-project') {
            file 'build.gradle', "apply plugin: 'java'"
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        project.hasNature(JavaCore.NATURE_ID)
        JavaCore.create(project).rawClasspath.any { it.path == GradleClasspathContainer.CONTAINER_PATH }
    }

    def "All subprojects with existing .project files are handled by the ExistingDescriptorHandler"() {
        setup:
        EclipseProjects.newProject('subproject-a', dir('sample-project/subproject-a'))
        EclipseProjects.newProject('subproject-b', dir('sample-project/subproject-b'))
        deleteAllProjects(false)
        def projectDir = dir('sample-project') {
            file 'settings.gradle', "include 'subproject-a', 'subproject-b'"
        }
        NewProjectHandler handler = Mock(NewProjectHandler)

        when:
        synchronizeAndWait(projectDir, handler)

        then:
        3 * handler.shouldImport(_) >> true
        2 * handler.shouldOverwriteDescriptor(*_)
    }

    @Override
    protected void prepareProject(String name) {
        def project = newProject(name)
        project.delete(false, true, null)
    }

    @Override
    protected void prepareJavaProject(String name) {
        def project = newJavaProject(name).project
        project.delete(false, true, null)
    }
}
