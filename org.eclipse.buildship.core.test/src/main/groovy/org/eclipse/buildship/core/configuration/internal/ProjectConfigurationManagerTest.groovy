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

import spock.lang.Shared

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.GradlePluginsRuntimeException
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.configuration.ProjectConfiguration
import org.eclipse.buildship.core.configuration.ProjectConfigurationManager
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification;
import org.eclipse.buildship.core.workspace.WorkspaceOperations

@SuppressWarnings("GroovyAccessibility")
class ProjectConfigurationManagerTest extends ProjectSynchronizationSpecification {

    @Shared
    ProjectConfigurationManager configurationManager = CorePlugin.projectConfigurationManager()

    @Shared
    WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations();

    def "no Gradle root project configurations available when there are no projects"() {
        setup:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations()
        assert rootProjectConfigurations == [] as Set
    }

    def "no Gradle root project configurations available when there are no Eclipse projects with Gradle nature"() {
        given:
        newProject("sample-project")

        when:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations()

        then:
        assert rootProjectConfigurations == [] as Set
    }

    def "no Gradle root project configurations available when there are no open Eclipse projects with Gradle nature"() {
        given:
        IProject project = workspaceOperations.createProject("sample-project", testDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())
        project.close(null)

        when:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations()

        then:
        assert rootProjectConfigurations == [] as Set
    }

    def "one Gradle root project configuration when one Gradle multi-project build is imported"() {
        setup:
        def rootDir = dir("root") {
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

        importAndWait(rootDir)

        when:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations()

        then:
        rootProjectConfigurations == [
                ProjectConfiguration.fromWorkspaceConfig(
                    rootDir,
                    GradleDistribution.fromBuild()
                )
            ] as Set
    }

    def "two Gradle root project configurations when two Gradle multi-project builds are imported"() {
        setup:
        def rootDirOne = dir("root1") {
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

        def rootDirTwo = dir("root2") {
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

        importAndWait(rootDirOne)
        importAndWait(rootDirTwo, GradleDistribution.forVersion("1.12"))

        when:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations()

        then:
        rootProjectConfigurations == [
                ProjectConfiguration.fromWorkspaceConfig(
                    rootDirOne,
                    GradleDistribution.fromBuild()
                ),
                ProjectConfiguration.fromWorkspaceConfig(
                    rootDirTwo,
                    GradleDistribution.forVersion('1.12')
                )
            ] as Set
    }

    def "error thrown when projects of same multi-project build have different shared project configurations"() {
        given:
        // create root project and use Gradle version 2.0 in the persisted configuration
        IProject rootProject = workspaceOperations.createProject("root-project", testDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())
        def projectConfiguration = ProjectConfiguration.fromWorkspaceConfig(rootProject, testDir, GradleDistribution.forVersion("2.0"))
        configurationManager.saveProjectConfiguration(projectConfiguration)

        // create child project and use Gradle version 1.0 in the persisted configuration
        IProject childProject = workspaceOperations.createProject("child-project", dir("child-project"), Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())
        def childProjectConfiguration = ProjectConfiguration.fromWorkspaceConfig(childProject, testDir, GradleDistribution.forVersion("1.0"))
        configurationManager.saveProjectConfiguration(childProjectConfiguration)

        when:
        configurationManager.getRootProjectConfigurations()

        then:
        thrown(GradlePluginsRuntimeException)
    }

    def "save and read project with full configuration"() {
        given:
        IProject project = workspaceOperations.createProject("sample-project", testDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())

        def projectConfiguration = ProjectConfiguration.fromWorkspaceConfig(project, project.getLocation().toFile(), GradleDistribution.forVersion("1.12"))

        when:
        configurationManager.saveProjectConfiguration(projectConfiguration)

        then:
        configurationManager.readProjectConfiguration(project) == projectConfiguration
    }

    def "save and read project with minimal configuration"() {
        given:
        IProject project = workspaceOperations.createProject("sample-project", testDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())

        def projectConfiguration = ProjectConfiguration.fromWorkspaceConfig(project, project.location.toFile(), GradleDistribution.fromBuild())

        when:
        configurationManager.saveProjectConfiguration(projectConfiguration)

        then:
        configurationManager.readProjectConfiguration(project) == projectConfiguration
    }

    def "project configuration can be read even if project is not yet refreshed"() {
        given:
        IProject project = workspaceOperations.createProject("sample-project", testDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())

        def projectConfiguration = ProjectConfiguration.fromWorkspaceConfig(project, project.location.toFile(), GradleDistribution.fromBuild())
        configurationManager.saveProjectConfiguration(projectConfiguration)

        def projectDescription = project.description
        project.delete(false, true, null)
        project.create(projectDescription, null)
        project.open(IResource.BACKGROUND_REFRESH, null)

        when:
        def readConfiguration = configurationManager.readProjectConfiguration(project)

        then:
        readConfiguration == projectConfiguration
    }

    def "missing project configurations are handled correcly"() {
        given:
        IProject project = workspaceOperations.createProject("sample-project", testDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())

        when:
        configurationManager.readProjectConfiguration(project)

        then:
        thrown RuntimeException

        when:
        def configuration = configurationManager.tryReadProjectConfiguration(project)

        then:
        !configuration.isPresent()
    }

    def "broken project configurations are handled correctly"() {
        given:
        IProject project = workspaceOperations.createProject("sample-project", testDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())

        def projectConfiguration = ProjectConfiguration.fromWorkspaceConfig(project, project.location.toFile(), GradleDistribution.fromBuild())
        configurationManager.saveProjectConfiguration(projectConfiguration)

        when:
        setInvalidPreferenceOn(project)
        configurationManager.readProjectConfiguration(project)

        then:
        thrown RuntimeException

        when:
        def configuration = configurationManager.tryReadProjectConfiguration(project)

        then:
        !configuration.isPresent()
    }

    def "broken project configurations are excluded from the root configurations"() {
        setup:
        def rootDirOne = dir("root1") {
            file('settings.gradle').text = "rootProject.name = 'one'"
        }

        def rootDirTwo = dir("root2") {
            file('settings.gradle').text = "rootProject.name = 'two'"
        }

        importAndWait(rootDirOne)
        importAndWait(rootDirTwo)

        when:
        setInvalidPreferenceOn(findProject('two'))
        List configurations = configurationManager.getRootProjectConfigurations() as List

        then:
        configurations.size() == 1
    }

    def "broken project configurations excluded from the project configurations"() {
        setup:
        def rootDirOne = dir("root1") {
            file('settings.gradle').text = '''
                rootProject.name = 'one'
                include 'sub'
            '''
            sub {
                file('build.gradle').text = '''
                   apply plugin: 'java'
                '''
            }
        }

        def rootDirTwo = dir("root2") {
            file('settings.gradle').text = "rootProject.name = 'two'"
        }

        importAndWait(rootDirOne)
        importAndWait(rootDirTwo)

        when:
        setInvalidPreferenceOn(findProject('sub'))
        List configurations = configurationManager.getAllProjectConfigurations() as List

        then:
        configurations.size() == 2
    }

    private void setInvalidPreferenceOn(IProject project) {
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, CorePlugin.PLUGIN_ID)
        preferences.write(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, 'I am error.')
        preferences.flush()
    }

}
