package org.eclipse.buildship.core.workspace.internal

import org.gradle.util.GradleVersion
import org.junit.Ignore;

import com.gradleware.tooling.toolingclient.GradleDistribution;

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification

@Ignore("TODO (donat) reimplement this class once we can import composites")
class SynchronizingMultipleBuilds extends ProjectSynchronizationSpecification {

    def "If there are no name clashes, no de-duplication is done"() {
        setup:
        def firstProject = dir('first') { file 'settings.gradle' }
        def secondProject = dir('second') { file 'settings.gradle' }

        when:
        importAndWait(firstProject)
        importAndWait(secondProject)

        then:
        allProjects().size() == 2
        findProject('first')
        findProject('second')
    }

    def "If one project has a problem, no project is synchronized"() {
        setup:
        def firstProject = dir('first') { file 'settings.gradle' }
        def secondProject = dir('second') { file 'settings.gradle' }
        importAndWait(firstProject)
        importAndWait(secondProject)

        fileTree(firstProject) {
            file 'settings.gradle', "rootProject.name = 'foo'"
        }

        fileTree(secondProject) {
            file 'settings.gradle', "error"
        }

        when:
        synchronizeAndWait()

        then:
        findProject("first")
        !findProject("foo")
    }

    def "External dependencies are substituted for project dependencies if all projects use same version"() {
        setup:
        def firstProject = dir('first') {
            file 'build.gradle', """
                apply plugin: 'java'
                dependencies {
                    testCompile 'junit:junit:4.12'
                }
            """
        }
        def secondProject = dir('second') {
            file 'settings.gradle', """
                rootProject.name = 'junit'
            """
            file 'build.gradle', """
                apply plugin: 'java'
                group = 'junit'
            """
        }

        when:
        importAndWait(firstProject)
        importAndWait(secondProject)

        then:
        allProjects().size() == 2
        resolvedClasspath(findProject('first')).any {
            it.path == new Path("/junit")
        }
    }

    def "Dependency substitution is disabled for mixed Gradle versions in the workspace"() {
        setup:
        def firstProject = dir('first') {
            file 'build.gradle', """
                apply plugin: 'java'
                dependencies {
                    testCompile 'junit:junit:4.12'
                }
            """
        }
        def secondProject = dir('second') {
            file 'settings.gradle', """
                rootProject.name = 'junit'
            """
            file 'build.gradle', """
                apply plugin: 'java'
                group = 'junit'
            """
        }

        when:
        importAndWait(firstProject)
        importAndWait(secondProject, GradleDistribution.forVersion("2.13"))

        then:
        allProjects().size() == 2
        !resolvedClasspath(findProject('first')).any {
            it.path == new Path("/junit")
        }
    }


    private IClasspathEntry[] resolvedClasspath(IProject project) {
        JavaCore.create(project).getResolvedClasspath(false)
    }
}
