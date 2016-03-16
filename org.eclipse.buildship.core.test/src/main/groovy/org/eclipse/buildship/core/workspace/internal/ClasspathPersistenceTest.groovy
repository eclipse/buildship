package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.workspace.WorkspaceGradleOperations
import org.eclipse.buildship.core.workspace.WorkspaceOperations;

class ClasspathPersistenceTest extends ProjectSynchronizationSpecification {

    def "the classpath container is persisted"() {
        setup:
        def projectDir = dir('sample-project') {
            file 'build.gradle',  '''apply plugin: "java"
               repositories { jcenter() }
               dependencies { compile "org.springframework:spring-beans:1.2.8"}
            '''
        }
        importAndWait(projectDir)

        WorkspaceGradleOperations workspaceOperations = Mock(WorkspaceGradleOperations)
        registerService(WorkspaceGradleOperations, workspaceOperations)

        IJavaProject javaProject = JavaCore.create(findProject("sample-project"))
        IProject project = javaProject.project

        expect:
        javaProject.getResolvedClasspath(false).find { it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }

        when:
        reimportWithoutSynchronization(project)

        then:
        0 * workspaceOperations.synchronizeGradleBuildWithWorkspace(*_)
        javaProject.getResolvedClasspath(false).find { it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }
    }

    def "The container initializer does not import new subprojects"() {
        setup:
        def projectDir = dir('sample-project') {
            file 'build.gradle',  'apply plugin: "java"'
        }

        importAndWait(projectDir)

        fileTree(projectDir) {
            dir 'sub'
            file 'settings.gradle', 'include "sub"'
        }

        CorePlugin.instance.stateLocation.append("classpath-persistence").toFile().delete()

        when:
        reimportWithoutSynchronization(findProject("sample-project"))

        then:
        workspace.root.projects.length == 1
    }

    private reimportWithoutSynchronization(IProject project) {
        def descriptor = project.description
        project.delete(false, true, null)
        project.create(descriptor, null)
        project.open(null)
        waitForGradleJobsToFinish()
    }

}
