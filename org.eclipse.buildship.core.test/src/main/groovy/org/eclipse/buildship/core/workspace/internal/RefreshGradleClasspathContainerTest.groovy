package org.eclipse.buildship.core.workspace.internal

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import com.google.common.collect.ImmutableList

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IWorkspaceRoot
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.IClasspathContainer
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectBuilder
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration
import org.eclipse.buildship.core.projectimport.ProjectImportJob
import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.test.fixtures.ProjectImportSpecification
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.workspace.GradleClasspathContainer
import org.eclipse.buildship.core.workspace.RefreshGradleClasspathContainerJob

class RefreshGradleClasspathContainerTest extends ProjectImportSpecification {

    def "Update the project classpath"() {
        setup:
        File location = importNewSimpleProject('simpleproject')
        IJavaProject project = findJavaProject('simpleproject')
        defineLocalGroovyDependency(new File(location, 'build.gradle'))

        when:
        executeRefreshGradleClasspathContainerJobAndWait(project)

        then:
        hasLocalGroovyDependencyDefinedInClasspathContainer(project)
    }

    def "Update changes the classpath of all related projects"() {
        setup:
        File location = importNewMultiProject('rootproject', 'subproject')
        IJavaProject rootProject = findJavaProject('rootproject')
        IJavaProject subProject = findJavaProject('subproject')

        defineLocalGroovyDependency(new File(location, 'build.gradle'))
        defineLocalGroovyDependency(new File("$location/subproject", 'build.gradle'))

        when:
        executeRefreshGradleClasspathContainerJobAndWait(subProject)

        then:
        hasLocalGroovyDependencyDefinedInClasspathContainer(rootProject)
        hasLocalGroovyDependencyDefinedInClasspathContainer(subProject)
    }

    def "Update doesn't change the classpath of unrelated projects"() {
        setup:
        File unrelatedProjectLocation = importNewSimpleProject('unrelatedproject')
        File location = importNewSimpleProject('simpleproject')
        IJavaProject unrelatedProject = findJavaProject('unrelatedproject')
        IJavaProject project = findJavaProject('simpleproject')

        defineLocalGroovyDependency(new File(location, 'build.gradle'))
        defineLocalGroovyDependency(new File(unrelatedProjectLocation, 'build.gradle'))

        when:
        executeRefreshGradleClasspathContainerJobAndWait(project)

        then:
        hasLocalGroovyDependencyDefinedInClasspathContainer(project)
        !hasLocalGroovyDependencyDefinedInClasspathContainer(unrelatedProject)
    }

    def "Updates multiple project roots at the same time"() {
        setup:
        File firstLocation = importNewSimpleProject('first')
        File secondLocation = importNewSimpleProject('second')
        IJavaProject firstProject = findJavaProject('first')
        IJavaProject secondProject = findJavaProject('second')

        defineLocalGroovyDependency(new File(firstLocation, 'build.gradle'))
        defineLocalGroovyDependency(new File(secondLocation, 'build.gradle'))

        when:
        executeRefreshGradleClasspathContainerJobAndWait(firstProject, secondProject)

        then:
        hasLocalGroovyDependencyDefinedInClasspathContainer(firstProject)
        hasLocalGroovyDependencyDefinedInClasspathContainer(secondProject)
    }

    private def importNewSimpleProject(String projectName) {
        def location = newProject(projectName)
        executeProjectImportAndWait(location)
        location
    }

    private def importNewMultiProject(String rootName, String subName) {
        def location = newMultiProject(rootName, subName)
        executeProjectImportAndWait(location)
        location
    }

    private def newProject(String projectName) {
        file(projectName, 'build.gradle') << 'apply plugin: "java"'
        file(projectName, 'settings.gradle') << ''
        folder(projectName, 'src', 'main', 'java')
        folder(projectName)
    }

    private def newMultiProject(String rootProjectName, String subProjectName) {
        file(rootProjectName, 'build.gradle') << 'apply plugin: "java"'
        folder(rootProjectName, 'src', 'main', 'java')
        file(rootProjectName, 'settings.gradle') << "include '$subProjectName'"
        folder(rootProjectName, 'subproject', 'src', 'main', 'java')
        file(rootProjectName, 'subproject', 'build.gradle') << 'apply plugin: "java"'
        folder(rootProjectName)
    }

    private def findJavaProject(String name) {
        JavaCore.create(CorePlugin.workspaceOperations().findProjectByName(name).get())
    }

    private def executeRefreshGradleClasspathContainerJobAndWait(IJavaProject... javaProjects) {
        def projects = javaProjects.collect { it.project }
        RefreshGradleClasspathContainerJob refreshJob = new RefreshGradleClasspathContainerJob(projects)
        refreshJob.schedule()
        refreshJob.join()
        waitForJobsToFinish()
    }

    private def defineLocalGroovyDependency(File buildScript) {
        buildScript << '\ndependencies { compile localGroovy() }'
    }

    private def hasLocalGroovyDependencyDefinedInClasspathContainer(IJavaProject javaProject) {
        IClasspathContainer rootContainer = JavaCore.getClasspathContainer(new Path(GradleClasspathContainer.CONTAINER_ID), javaProject)
        rootContainer.classpathEntries.find  { it.path.toPortableString().contains('groovy-all') } != null
    }

}
