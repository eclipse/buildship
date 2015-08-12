package org.eclipse.buildship.core.workspace.internal

import spock.lang.Ignore

import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IWorkspaceDescription
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.test.fixtures.ProjectImportSpecification


@Ignore
class DependencyExportTest extends ProjectImportSpecification {

    def "Exported transitive dependencies from project dependency are included"() {
        setup:
        File location = newMultiProjectWithGuavaDependency(false)

        when:
        executeProjectImportAndWait(location)
        waitForJobsToFinish() // wait the classpath container to be resolved

        then:
        def dependentProject = findProject('dependent-project')
        loadResolvedClasspath(dependentProject).any { IClasspathEntry entry -> entry.path.lastSegment() == 'guava-19.0-rc1.jar' }
    }

    def "Excluded transitive dependencies from project dependency with exclusion are not resolved"() {
        setup:
        File location = newMultiProjectWithGuavaDependency(true)

        when:
        executeProjectImportAndWait(location)
        waitForJobsToFinish() // wait the classpath container to be resolved

        then:
        def dependentProject = findProject('dependent-project')
        !loadResolvedClasspath(dependentProject).any { IClasspathEntry entry -> entry.path.lastSegment() == 'guava-19.0-rc1.jar' }
    }

    def newMultiProjectWithGuavaDependency(boolean excludeGuavaProjectDependency) {
        // create multi-project root
        file('multi-project-withguava', 'build.gradle') << ''
        file('multi-project-withguava', 'settings.gradle') <<
        '''include "guava-project"
           include "dependent-project"
        '''

        // Create project with guava dependency and code
        file('multi-project-withguava', 'guava-project', 'build.gradle') <<
        '''apply plugin: 'java'
           repositories { mavenCentral() }
           dependencies { compile 'com.google.guava:guava:19.0-rc1' }
        '''

        // create project with project dependency to the guava-project
        if (excludeGuavaProjectDependency) {
            file('multi-project-withguava', 'dependent-project', 'build.gradle') <<
            """apply plugin: 'java'
               repositories { mavenCentral() }
               dependencies { compile (project(":guava-project")) { exclude group: "com.google.guava" } }
            """
        } else {
            file('multi-project-withguava', 'dependent-project', 'build.gradle') <<
            """apply plugin: 'java'
               repositories { mavenCentral() }
               dependencies { compile (project(":guava-project")) }
            """
        }

        folder('multi-project-withguava', 'dependent-project', 'src', 'main', 'java')
        folder('multi-project-withguava')
    }

    private def loadResolvedClasspath(IProject project) {
        JavaCore.create(project).getResolvedClasspath(false)
    }
}
