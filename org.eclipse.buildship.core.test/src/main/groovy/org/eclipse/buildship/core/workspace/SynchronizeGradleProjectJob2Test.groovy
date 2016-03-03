package org.eclipse.buildship.core.workspace

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import com.google.common.collect.ImmutableList

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes

import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectBuilder
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.test.fixtures.TestEnvironment
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.util.variable.ExpressionUtils
import org.eclipse.buildship.core.workspace.internal.ProjectSynchronizationSpecification;

class SynchronizeGradleProjectJob2Test extends ProjectSynchronizationSpecification {

    def "Project import job creates a new project in the workspace"(boolean projectDescriptorExists) {
        setup:
        def applyJavaPlugin = false
        File projectLocation = newProject(projectDescriptorExists, applyJavaPlugin)

        when:
        synchronizeAndWait(projectLocation)

        then:
        CorePlugin.workspaceOperations().findProjectByName(projectLocation.name).present

        where:
        projectDescriptorExists << [false, true]
    }

    def "Project descriptors should be created iff they don't already exist"(boolean applyJavaPlugin, boolean projectDescriptorExists, String descriptorComment) {
        setup:
        File rootProject = newProject(projectDescriptorExists, applyJavaPlugin)

        when:
        synchronizeAndWait(rootProject)

        then:
        new File(rootProject, '.project').exists()
        new File(rootProject, '.classpath').exists() == applyJavaPlugin
        CorePlugin.workspaceOperations().findProjectInFolder(rootProject, null).get().getComment() == descriptorComment

        where:
        applyJavaPlugin | projectDescriptorExists | descriptorComment
        false           | false                   | 'Project simple-project created by Buildship.' // the comment from the generated descriptor
        false           | true                    | 'original'                                     // the comment from the original descriptor
        true            | false                   | 'Project simple-project created by Buildship.'
        true            | true                    | 'original'
    }

    def "Imported projects always have Gradle builder and nature"(boolean projectDescriptorExists) {
        setup:
        File rootProject = newProject(projectDescriptorExists, false)

        when:
        synchronizeAndWait(rootProject)

        then:
        def project = CorePlugin.workspaceOperations().findProjectByName(rootProject.name).get()
        GradleProjectNature.INSTANCE.isPresentOn(project)
        project.description.buildSpec.find { it.getBuilderName().equals(GradleProjectBuilder.INSTANCE.ID) }

        where:
        projectDescriptorExists << [false, true]
    }

    def "Imported parent projects have filters to hide the content of the children and the build folders"() {
        setup:
        File rootProject = newMultiProject()

        when:
        synchronizeAndWait(rootProject)

        then:
        def filters = CorePlugin.workspaceOperations().findProjectByName(rootProject.name).get().getFilters()
        filters.length == 3
        (filters[0].fileInfoMatcherDescription.arguments as String).endsWith('subproject')
        (filters[1].fileInfoMatcherDescription.arguments as String).endsWith('build')
        (filters[2].fileInfoMatcherDescription.arguments as String).endsWith('.gradle')
    }

    def "Importing a project twice won't result in duplicate filters"() {
        setup:
        def workspaceOperations = CorePlugin.workspaceOperations()
        File rootProject = newMultiProject()

        when:
        synchronizeAndWait(rootProject)

        deleteAllProjects(false)

        synchronizeAndWait(rootProject)

        then:
        def filters = workspaceOperations.findProjectByName(rootProject.name).get().getFilters()
        filters.length == 3
        (filters[0].fileInfoMatcherDescription.arguments as String).endsWith('subproject')
        (filters[1].fileInfoMatcherDescription.arguments as String).endsWith('build')
        (filters[2].fileInfoMatcherDescription.arguments as String).endsWith('.gradle')
    }

    def "Can import deleted project located in default location"() {
        setup:
        def workspaceOperations = CorePlugin.workspaceOperations()
        def workspaceRootLocation = LegacyEclipseSpockTestHelper.workspace.root.location.toFile()
        def location = new File(workspaceRootLocation, 'projectname')
        location.mkdirs()

        def project = workspaceOperations.createProject("projectname", location, ImmutableList.of(), new NullProgressMonitor())
        project.delete(false, true, new NullProgressMonitor())

        when:
        synchronizeAndWait(workspaceDir("projectname"))

        then:
        workspaceOperations.allProjects.size() == 1
    }

    def "Can import project located in workspace folder and with custom root name"() {
        setup:
        File rootProject = newProjectWithCustomNameInWorkspaceFolder()

        when:
        synchronizeAndWait(rootProject)

        then:
        LegacyEclipseSpockTestHelper.workspace.root.projects.length == 1
        def project = LegacyEclipseSpockTestHelper.workspace.root.projects[0]
        def locationExpression = ExpressionUtils.encodeWorkspaceLocation(project)
        def decodedLocation = ExpressionUtils.decode(locationExpression)
        rootProject.equals(new File(decodedLocation))

        cleanup:
        rootProject.deleteDir()
    }


    def "Mutliple jobs with the same configuration are merged"() {
        setup:
        File projectLocation = newProject(true, false)
        def jobs = (1..5).collect { newSynchronizationJob(projectLocation) }
        WorkspaceGradleOperations workspaceOperations = Mock(WorkspaceGradleOperations)
        TestEnvironment.registerService(WorkspaceGradleOperations, workspaceOperations)

        when:
        jobs.each { it.schedule() }
        waitForJobsToFinish()

        then:
        1 * workspaceOperations.synchronizeGradleBuildWithWorkspace(*_)

        cleanup:
        TestEnvironment.cleanup()
    }

    def File newProject(boolean projectDescriptorExists, boolean applyJavaPlugin) {
        dir('simple-project') {
            file 'build.gradle', applyJavaPlugin ? 'apply plugin: "java"' : ''
            dir 'src/main/java'
            if (projectDescriptorExists) {
                file '.project', '''<?xml version="1.0" encoding="UTF-8"?>
                    <projectDescription>
                        <name>simple-project</name>
                        <comment>original</comment>
                        <projects></projects>
                        <buildSpec></buildSpec>
                        <natures></natures>
                    </projectDescription>'''
                if (applyJavaPlugin) {
                    file '.classpath', '''<?xml version="1.0" encoding="UTF-8"?>
                        <classpath>
                            <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
                            <classpathentry kind="src" path="src/main/java"/>
                            <classpathentry kind="output" path="bin"/>
                        </classpath>'''
                }
            }
        }
    }


    def File newMultiProject() {
        dir('multi-project') {
            dir ('subproject')
            file 'settings.gradle', 'include "subproject"'
        }
    }

    def File newProjectWithCustomNameInWorkspaceFolder() {
        workspaceDir('Bug472223') {
            file 'settings.gradle', "rootProject.name = 'my-project-name-is-different-than-the-folder'"
        }
    }

}
