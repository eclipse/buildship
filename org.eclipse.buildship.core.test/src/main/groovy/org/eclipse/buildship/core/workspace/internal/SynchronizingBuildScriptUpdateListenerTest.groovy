package org.eclipse.buildship.core.workspace.internal

import org.eclipse.buildship.core.configuration.BuildConfiguration
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

import java.io.File

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject

class SynchronizingBuildScriptUpdateListenerTest extends ProjectSynchronizationSpecification {



    /*def "Execute project synchronization when build.gradle file created"() {

        setup: "Create a new Java project for testing"

        IJavaProject javaProject = newJavaProject('sample-project')
        IClasspathEntry[] entries = javaProject.rawClasspath + JavaCore.newContainerEntry(GradleClasspathContainer.CONTAINER_PATH)
        javaProject.setRawClasspath(entries, null)
        BuildConfiguration buildConfig = createOverridingBuildConfiguration(dir('sample-project'), DEFAULT_DISTRIBUTION)

        expect:
        !javaProject.getResolvedClasspath(false).find{ it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }

        when:
        getConfigurationManager()
            .saveWorkspaceConfiguration(configurationManager
                .saveWorkspaceConfiguration(
                    new WorkspaceConfiguration(GradleDistribution.fromBuild(), null, false, false, true)))

        def projectDir = dir('sample-project') {
            file 'build.gradle','''apply plugin: "java"
               repositories { jcenter() }
               dependencies { compile "org.springframework:spring-beans:1.2.8"}
            '''
            dir 'src/main/java'
        }

        waitForGradleJobsToFinish()

        then:
        javaProject.getResolvedClasspath(false).find{ it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }
    }

    def "Execute project synchronization when build.gradle file changes"() {
        // TODO (donat) implement test case
    }

    def "Execute project synchronization when build.gradle file deleted"() {
        // TODO (donat) implement test case
    }

    def "Execute project synchronization when custom build script changes"() {
        // TODO (donat) implement test case
    }

    def "Synchronization can be disabled for the entire workspace"() {
        // TODO (donat) implement test case
    }

    def "Synchronization can be disabled for a project"() {
        // TODO (donat) implement test case
    }*/
}
