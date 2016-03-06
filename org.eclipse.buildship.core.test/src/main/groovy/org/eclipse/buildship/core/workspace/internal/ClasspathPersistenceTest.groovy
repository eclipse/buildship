package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification;
import org.eclipse.buildship.core.test.fixtures.TestEnvironment;
import org.eclipse.buildship.core.workspace.GradleClasspathContainer
import org.eclipse.buildship.core.workspace.WorkspaceGradleOperations;

class ClasspathPersistenceTest extends ProjectSynchronizationSpecification {
    def "The classpath container is persisted"() {
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

    private reimportWithoutSynchronization(IProject project) {
        def descriptor = project.description
        project.delete(false, true, null)
        project.create(descriptor, null)
        project.open(null)
        waitForGradleJobsToFinish()
    }
}
