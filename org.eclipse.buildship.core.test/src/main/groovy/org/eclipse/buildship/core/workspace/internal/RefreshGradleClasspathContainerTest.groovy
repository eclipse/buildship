package org.eclipse.buildship.core.workspace.internal

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

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
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.workspace.GradleClasspathContainer
import org.eclipse.buildship.core.workspace.RefreshGradleClasspathContainerJob

class RefreshGradleClasspathContainerTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    def cleanup() {
        CorePlugin.workspaceOperations().deleteAllProjects(null)
    }

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

    // -- helper methods --

    private def importNewSimpleProject(String projectName) {
        def location = newProject(projectName)
        ProjectImportJob importJob = newProjectImportJob(location)
        importJob.schedule()
        importJob.join()
        location
    }

    private def importNewMultiProject(String rootName, String subName) {
        def location = newMultiProject(rootName, subName)
        ProjectImportJob importJob = newProjectImportJob(location)
        importJob.schedule()
        importJob.join()
        location
    }

    private def newProject(String projectName) {
        def location = tempFolder.newFolder(projectName)
        new File(location, 'build.gradle') << 'apply plugin: "java"'
        new File(location, 'settings.gradle') << ''
        new File(location, 'src/main/java').mkdirs()
        location
    }

    private def newMultiProject(String rootProjectName, String subProjectName) {
        def location = tempFolder.newFolder(rootProjectName)
        new File(location, 'build.gradle') << 'apply plugin: "java"'
        new File(location, 'src/main/java').mkdirs()
        new File(location, 'settings.gradle') << 'include "' + subProjectName + '"'
        def subProject = new File(location, "subproject")
        subProject.mkdirs()
        new File(subProject, 'src/main/java').mkdirs()
        new File(subProject, 'build.gradle') << 'apply plugin: "java"'
        location
    }

    private def newProjectImportJob(File location) {
        ProjectImportConfiguration configuration = new ProjectImportConfiguration()
        configuration.gradleDistribution = GradleDistributionWrapper.from(GradleDistribution.fromBuild())
        configuration.projectDir = location
        configuration.applyWorkingSets = true
        configuration.workingSets = []
        new ProjectImportJob(configuration, AsyncHandler.NO_OP)
    }

    private def findJavaProject(String name) {
        JavaCore.create(CorePlugin.workspaceOperations().findProjectByName(name).get())
    }

    private def executeRefreshGradleClasspathContainerJobAndWait(IJavaProject... javaProjects) {
        def projects = javaProjects.collect { it.project }
        RefreshGradleClasspathContainerJob refreshJob = new RefreshGradleClasspathContainerJob(projects)
        refreshJob.schedule()
        refreshJob.join()
        waitForJobs()
    }

    private def defineLocalGroovyDependency(File buildScript) {
        buildScript << '\ndependencies { compile localGroovy() }'
    }

    private def hasLocalGroovyDependencyDefinedInClasspathContainer(IJavaProject javaProject) {
        IClasspathContainer rootContainer = JavaCore.getClasspathContainer(new Path(GradleClasspathContainer.CONTAINER_ID), javaProject)
        rootContainer.classpathEntries.find  { it.path.toPortableString().contains('groovy-all') } != null
    }

    private static def waitForJobs() {
        while (!Job.getJobManager().isIdle()) {
            Thread.sleep(200)
        }
    }
}
