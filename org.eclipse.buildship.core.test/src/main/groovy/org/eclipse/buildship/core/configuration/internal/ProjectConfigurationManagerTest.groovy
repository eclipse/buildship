/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.configuration.internal

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

import com.google.common.collect.ImmutableList

import com.gradleware.tooling.junit.TestFile
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.Path
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.ProjectScope
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.GradlePluginsRuntimeException
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.configuration.ProjectConfiguration
import org.eclipse.buildship.core.configuration.ProjectConfigurationManager
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration
import org.eclipse.buildship.core.test.fixtures.EclipseProjects;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.workspace.SynchronizeGradleProjectJob
import org.eclipse.buildship.core.workspace.WorkspaceOperations

@SuppressWarnings("GroovyAccessibility")
class ProjectConfigurationManagerTest extends Specification {

    @Shared
    ProjectConfigurationManager configurationManager = CorePlugin.projectConfigurationManager()

    @Shared
    WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations();

    @Rule
    TemporaryFolder tempFolder

    def cleanup() {
        workspaceOperations.deleteAllProjects(new NullProgressMonitor())
    }

    def "no Gradle root project configurations available when there are no projects"() {
        setup:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations()
        assert rootProjectConfigurations == [] as Set
    }

    def "no Gradle root project configurations available when there are no Eclipse projects with Gradle nature"() {
        given:
        File projectDir = tempFolder.root
        workspaceOperations.createProject("sample-project", projectDir, ImmutableList.of(), new NullProgressMonitor())

        when:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations()

        then:
        assert rootProjectConfigurations == [] as Set
    }

    def "no Gradle root project configurations available when there are no open Eclipse projects with Gradle nature"() {
        given:
        File projectDir = tempFolder.root
        IProject project = workspaceOperations.createProject("sample-project", projectDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())
        project.close(new NullProgressMonitor())

        when:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations()

        then:
        assert rootProjectConfigurations == [] as Set
    }

    def "one Gradle root project configuration when one Gradle multi-project build is imported"() {
        setup:
        def rootDir = new TestFile(tempFolder.newFolder())
        rootDir.create {
            file('settings.gradle').text = '''
                rootProject.name = 'project one'
                include 'sub1'
                include 'sub2'
            '''
            sub1 {
                file('build.gradle').text = '''
                   apply plugin: 'java'
                '''
            }
            sub2 {
                file('build.gradle').text = '''
                   apply plugin: 'java'
                '''
            }
        }

        def importConfigurationOne = new ProjectImportConfiguration()
        importConfigurationOne.projectDir = new File(rootDir.absolutePath)
        importConfigurationOne.gradleDistribution = GradleDistributionWrapper.from(GradleDistributionWrapper.DistributionType.WRAPPER, null)
        importConfigurationOne.applyWorkingSets = true
        importConfigurationOne.workingSets = []

        new SynchronizeGradleProjectJob(importConfigurationOne.toFixedAttributes(), importConfigurationOne.workingSets.getValue(), AsyncHandler.NO_OP).runToolingApiJobInWorkspace(new NullProgressMonitor())

        when:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations()

        then:
        rootProjectConfigurations == [
                ProjectConfiguration.from(
                        new FixedRequestAttributes(rootDir,
                                null,
                                GradleDistribution.fromBuild(),
                                null,
                                ImmutableList.of(),
                                ImmutableList.of()),
                        Path.from(':'))] as Set
    }

    def "two Gradle root project configurations when two Gradle multi-project builds are imported"() {
        setup:
        def rootDirOne = new TestFile(tempFolder.newFolder())
        rootDirOne.create {
            file('settings.gradle').text = '''
                rootProject.name = 'project one'
                include 'sub1'
                include 'sub2'
            '''
            sub1 {
                file('build.gradle').text = '''
                   apply plugin: 'java'
                '''
            }
            sub2 {
                file('build.gradle').text = '''
                   apply plugin: 'java'
                '''
            }
        }

        def rootDirTwo = new TestFile(tempFolder.newFolder())
        rootDirTwo.create {
            file('settings.gradle').text = '''
                rootProject.name = 'project two'
                include 'alpha'
                include 'beta'
            '''
            alpha {
                file('build.gradle').text = '''
                   apply plugin: 'java'
                '''
            }
            beta {
                file('build.gradle').text = '''
                   apply plugin: 'java'
                '''
            }
        }

        def importConfigurationOne = new ProjectImportConfiguration()
        importConfigurationOne.projectDir = new File(rootDirOne.absolutePath)
        importConfigurationOne.gradleDistribution = GradleDistributionWrapper.from(GradleDistributionWrapper.DistributionType.WRAPPER, null)
        importConfigurationOne.applyWorkingSets = true
        importConfigurationOne.workingSets = []

        def importConfigurationTwo = new ProjectImportConfiguration()
        importConfigurationTwo.projectDir = new File(rootDirTwo.absolutePath)
        importConfigurationTwo.gradleDistribution = GradleDistributionWrapper.from(GradleDistributionWrapper.DistributionType.VERSION, '1.12')
        importConfigurationTwo.applyWorkingSets = true
        importConfigurationTwo.workingSets = []

        new SynchronizeGradleProjectJob(importConfigurationOne.toFixedAttributes(), importConfigurationOne.workingSets.getValue(), AsyncHandler.NO_OP).runToolingApiJobInWorkspace(new NullProgressMonitor())
        new SynchronizeGradleProjectJob(importConfigurationTwo.toFixedAttributes(), importConfigurationOne.workingSets.getValue(), AsyncHandler.NO_OP).runToolingApiJobInWorkspace(new NullProgressMonitor())

        when:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations()

        then:
        rootProjectConfigurations == [
                ProjectConfiguration.from(
                        new FixedRequestAttributes(rootDirOne,
                                null,
                                GradleDistribution.fromBuild(),
                                null,
                                ImmutableList.of(),
                                ImmutableList.of()),
                        Path.from(':')),
                ProjectConfiguration.from(
                        new FixedRequestAttributes(rootDirTwo,
                                null,
                                GradleDistribution.forVersion('1.12'),
                                null,
                                ImmutableList.of(),
                                ImmutableList.of()),
                        Path.from(':'))] as Set
    }

    def "error thrown when projects of same multi-project build have different shared project configurations"() {
        given:
        File rootProjectDir = tempFolder.newFolder()

        // create root project and use Gradle version 2.0 in the persisted configuration
        IProject rootProject = workspaceOperations.createProject("root-project", rootProjectDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())
        def requestAttributes = new FixedRequestAttributes(rootProjectDir, null, GradleDistribution.forVersion("2.0"), null,
                ImmutableList.copyOf("-Xmx256M"), ImmutableList.copyOf("foo"))
        def projectConfiguration = ProjectConfiguration.from(requestAttributes, Path.from(":"))
        configurationManager.saveProjectConfiguration(projectConfiguration, rootProject)

        // create child project and use Gradle version 1.0 in the persisted configuration
        File childProjectDir = tempFolder.newFolder()
        IProject childProject = workspaceOperations.createProject("child-project", childProjectDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())
        def childRequestAttributes = new FixedRequestAttributes(rootProjectDir, null, GradleDistribution.forVersion("1.0"), null,
                ImmutableList.copyOf("-Xmx256M"), ImmutableList.copyOf("foo"))
        def childProjectConfiguration = ProjectConfiguration.from(childRequestAttributes, Path.from(":child"))
        configurationManager.saveProjectConfiguration(childProjectConfiguration, childProject)

        when:
        configurationManager.getRootProjectConfigurations()

        then:
        thrown(GradlePluginsRuntimeException)
    }

    def "save and read project with full configuration"() {
        given:
        File projectDir = tempFolder.root
        IProject project = workspaceOperations.createProject("sample-project", projectDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())

        def requestAttributes = new FixedRequestAttributes(project.getLocation().toFile(), tempFolder.newFolder(), GradleDistribution.forVersion("1.12"), tempFolder.newFolder(),
                ImmutableList.copyOf("-Xmx256M"), ImmutableList.copyOf("foo"))
        def projectConfiguration = ProjectConfiguration.from(requestAttributes, Path.from(":"))

        when:
        configurationManager.saveProjectConfiguration(projectConfiguration, project)

        then:
        configurationManager.readProjectConfiguration(project) == projectConfiguration
    }

    def "save and read project with minimal configuration"() {
        given:
        File projectDir = tempFolder.newFolder()
        IProject project = workspaceOperations.createProject("sample-project", projectDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())

        def attributes = new FixedRequestAttributes(project.getLocation().toFile(), null, GradleDistribution.fromBuild(), null,
                ImmutableList.of(), ImmutableList.of())
        def projectConfiguration = ProjectConfiguration.from(attributes, Path.from(":"))

        when:
        configurationManager.saveProjectConfiguration(projectConfiguration, project)

        then:
        configurationManager.readProjectConfiguration(project) == projectConfiguration
    }

    def "project configuration can be read even if project is not yet refreshed"() {
        given:
        File projectDir = tempFolder.newFolder()
        IProject project = workspaceOperations.createProject("sample-project", projectDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())

        def attributes = new FixedRequestAttributes(project.getLocation().toFile(), null, GradleDistribution.fromBuild(), null,
                ImmutableList.of(), ImmutableList.of())
        def projectConfiguration = ProjectConfiguration.from(attributes, Path.from(":"))
        configurationManager.saveProjectConfiguration(projectConfiguration, project)

        def projectDescription = project.description
        project.delete(false, true, null)
        project.create(projectDescription, null)
        project.open(IResource.BACKGROUND_REFRESH, null)

        when:
        def readConfiguration = configurationManager.readProjectConfiguration(project)

        then:
        readConfiguration == projectConfiguration
    }

    def "legacy project configuration is converted to use the Eclipse preferences api"() {
        setup:
        IProject project = EclipseProjects.newProject('sample-project', tempFolder.root)
        tempFolder.newFolder('.settings')
        tempFolder.newFile('.settings/gradle.prefs') << """{
          "1.0": {
             "project_path": ":",
             "connection_project_dir": ".",
             "connection_gradle_user_home": null,
             "connection_gradle_distribution": "GRADLE_DISTRIBUTION(WRAPPER)",
             "connection_java_home": null,
             "connection_jvm_arguments": "",
             "connection_arguments": ""
          }
        }
        """

        when:
        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor())
        configurationManager.saveProjectConfiguration(configurationManager.readProjectConfiguration(project), project)

        then:
        !new File(tempFolder.root, '.settings/gradle.prefs').exists()
        new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID).get(DefaultProjectConfigurationPersistence.PREF_KEY_PROJECT_PATH, null) == ':'
        new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID).get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, null) == ''
    }

    def "legacy project configuration conversion handles absolute paths"() {
        setup:
        IProject project = EclipseProjects.newProject('sample-project', tempFolder.root)
        tempFolder.newFolder('.settings')
        tempFolder.newFile('.settings/gradle.prefs') << """{
          "1.0": {
             "project_path": ":",
             "connection_project_dir": "${tempFolder.root.parentFile.canonicalPath.replace('\\', '\\\\')}",
             "connection_gradle_user_home": null,
             "connection_gradle_distribution": "GRADLE_DISTRIBUTION(WRAPPER)",
             "connection_java_home": null,
             "connection_jvm_arguments": "",
             "connection_arguments": ""
          }
        }
        """

        when:
        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor())
        configurationManager.saveProjectConfiguration(configurationManager.readProjectConfiguration(project), project)

        then:
        !new File(tempFolder.root, '.settings/gradle.prefs').exists()
        new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID).get(DefaultProjectConfigurationPersistence.PREF_KEY_PROJECT_PATH, null) == ':'
        new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID).get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, null) == '..'
    }

}
