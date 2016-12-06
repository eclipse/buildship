package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.workspace.GradleClasspathContainer

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
