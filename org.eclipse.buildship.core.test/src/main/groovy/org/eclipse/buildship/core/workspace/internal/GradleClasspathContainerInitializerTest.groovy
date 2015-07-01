package org.eclipse.buildship.core.workspace.internal

import java.io.File

import com.google.common.base.Optional

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild
import com.gradleware.tooling.toolingmodel.OmniEclipseProject
import com.gradleware.tooling.toolingmodel.OmniEclipseSourceDirectory
import com.gradleware.tooling.toolingmodel.Path
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.repository.ModelRepository
import com.gradleware.tooling.toolingmodel.repository.ModelRepositoryProvider

import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.ClasspathContainerInitializer
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.test.fixtures.TestEnvironment
import org.eclipse.buildship.core.util.file.FileUtils
import org.eclipse.buildship.core.workspace.ClasspathDefinition

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IPath

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.ProjectConfiguration
import org.eclipse.buildship.core.configuration.ProjectConfigurationManager


class GradleClasspathContainerInitializerTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    IJavaProject javaProject

    def setup() {
        javaProject = blancJavaProject()
    }

    def cleanup() {
        TestEnvironment.cleanup()
        CorePlugin.workspaceOperations().deleteAllProjects(null)
    }

    def "Create a source folder"() {
        given:
        gradleModel(['src'])

        expect:
        javaProject.rawClasspath.length == 1
        javaProject.rawClasspath[0].entryKind == IClasspathEntry.CPE_CONTAINER

        when:
        Job initializerJob = executeClasspathInitializerAndWait()

        then:
        initializerJob.result.isOK()
        javaProject.rawClasspath.length == 2
        javaProject.rawClasspath[0].entryKind == IClasspathEntry.CPE_SOURCE
        javaProject.rawClasspath[0].path.toPortableString() == "/project-name/src"
        javaProject.rawClasspath[1].entryKind == IClasspathEntry.CPE_CONTAINER
    }

    def "Duplicate source folders are merged into one sorce entry"() {
        given:
        gradleModel(['src', 'src'])

        when:
        Job initializerJob = executeClasspathInitializerAndWait()

        then:
        initializerJob.result.isOK()
        javaProject.rawClasspath.length == 2
    }

    def "Source folders coming from Gradle are removed if they no longer exist"() {
        given:
        IFolder sourceFolder = javaProject.project.getFolder('src-old')
        FileUtils.ensureFolderHierarchyExists(sourceFolder)
        javaProject.setRawClasspath((javaProject.rawClasspath
            + JavaCore.newSourceEntry(sourceFolder.location, new IPath[0], new IPath[0], null,
                 JavaCore.newClasspathAttribute(GradleClasspathContainerInitializer.CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL, "true"))) as IClasspathEntry[], null)
        gradleModel(['src-new'])

        when:
        Job initializerJob = executeClasspathInitializerAndWait()

        then:
        initializerJob.result.isOK()
        javaProject.rawClasspath.length == 2
        javaProject.rawClasspath[0].entryKind == IClasspathEntry.CPE_SOURCE
        javaProject.rawClasspath[0].path.toPortableString() == "/project-name/src-new"
        javaProject.rawClasspath[1].entryKind == IClasspathEntry.CPE_CONTAINER
    }

    def "Non-model source folders are preserved even if they are not part of the Gradle model" () {
        given:
        IFolder sourceFolder = javaProject.project.getFolder('src')
        FileUtils.ensureFolderHierarchyExists(sourceFolder)
        javaProject.setRawClasspath((javaProject.rawClasspath + JavaCore.newSourceEntry(sourceFolder.location)) as IClasspathEntry[], null)
        gradleModel(['gradle-src'])

        expect:
        javaProject.rawClasspath.length == 2

        when:
        Job initializerJob = executeClasspathInitializerAndWait()
        then:
        initializerJob.result.isOK()
        javaProject.rawClasspath.length == 3
        javaProject.rawClasspath.findAll {
            it.entryKind == IClasspathEntry.CPE_SOURCE &&
            it.extraAttributes.length == 0 &&
            it.path == sourceFolder.location
        }.size() == 1
    }

    private Job executeClasspathInitializerAndWait() {
        ClasspathContainerInitializer initializer = JavaCore.getClasspathContainerInitializer(ClasspathDefinition.GRADLE_CLASSPATH_CONTAINER_ID)
        org.eclipse.core.runtime.Path containerPath = new org.eclipse.core.runtime.Path(ClasspathDefinition.GRADLE_CLASSPATH_CONTAINER_ID)
        Job initializerJob = initializer.scheduleClasspathInitialization(containerPath, javaProject, FetchStrategy.LOAD_IF_NOT_CACHED)
        initializerJob.join()
        initializerJob
    }

    private IJavaProject blancJavaProject() {
        // create project folder
        File location = tempFolder.newFolder('project-location')

        // create project
        IProject project = CorePlugin.workspaceOperations().createProject('project-name', location, [], [], null)
        def description = project.getDescription()
        description.setNatureIds([JavaCore.NATURE_ID] as String[])
        project.setDescription(description, null)

        // convert it to a java project
        IJavaProject javaProject = JavaCore.create(project)
        javaProject.setRawClasspath([
            JavaCore.newContainerEntry(JavaRuntime.getDefaultJREContainerEntry().getPath())] as IClasspathEntry[], null)
        IFolder outputLocation = project.getFolder('bin')
        FileUtils.ensureFolderHierarchyExists(outputLocation)
        javaProject.setOutputLocation(outputLocation.getFullPath(), null)
        javaProject
    }

    private void gradleModel(List<String> sourceDirectoryPaths) {
        def sourceDirectories = sourceDirectoryPaths.collect {
            OmniEclipseSourceDirectory sourceDirectory = Mock(OmniEclipseSourceDirectory)
            sourceDirectory.getPath() >> it
            sourceDirectory
        }

        OmniEclipseProject eclipseProject = Mock(OmniEclipseProject)
        eclipseProject.tryFind(_) >> Optional.of(eclipseProject)
        eclipseProject.getSourceDirectories() >> sourceDirectories
        eclipseProject.getProjectDependencies() >> []
        eclipseProject.getExternalDependencies() >> []

        OmniEclipseGradleBuild eclipseGradleBuild = Mock(OmniEclipseGradleBuild)
        eclipseGradleBuild.getRootEclipseProject() >> eclipseProject

        ModelRepository modelRepository = Mock(ModelRepository)
        modelRepository.fetchEclipseGradleBuild(_,_) >> eclipseGradleBuild

        def modelRepositoryProvider = Mock(ModelRepositoryProvider)
        modelRepositoryProvider.getModelRepository(_) >> modelRepository

        def projectConfigurationManager = Mock(ProjectConfigurationManager)
        projectConfigurationManager.readProjectConfiguration(_) >> ProjectConfiguration.from(new FixedRequestAttributes(new File("."), null,
            GradleDistribution.fromBuild(), null, [],[]), new Path(':'), new File('.'))

        TestEnvironment.registerService(ProjectConfigurationManager, projectConfigurationManager)
        TestEnvironment.registerService(ModelRepositoryProvider, modelRepositoryProvider)
    }
}
