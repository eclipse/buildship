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
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.BuildConfiguration
import org.eclipse.buildship.core.configuration.ConfigurationManager
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification;
import org.eclipse.buildship.core.workspace.WorkspaceOperations

@SuppressWarnings("GroovyAccessibility")
class BuildConfigurationTest extends ProjectSynchronizationSpecification {

    @Shared
    ConfigurationManager configurationManager = CorePlugin.configurationManager()

    @Shared
    WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations()

    def "can create new build configuration"() {
        setup:
        File projectDir = dir('project-dir').canonicalFile
        BuildConfiguration configuration = configurationManager.createBuildConfiguration(dir('project-dir'), GradleDistribution.forVersion('2.0'), false, false, false)

        expect:
        configuration.rootProjectDirectory == projectDir
        configuration.gradleDistribution == GradleDistribution.forVersion('2.0')
        configuration.overrideWorkspaceSettings == false
        configuration.buildScansEnabled == false
        configuration.offlineMode == false
    }

    def "new build configuration can inherit workspace settings"(boolean buildScansEnabled, boolean offlineMode) {
        setup:
        File projectDir = dir('project-dir')
        WorkspaceConfiguration orignalConfiguration = configurationManager.loadWorkspaceConfiguration()

        when:
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(dir('gradle-user-home'), offlineMode, buildScansEnabled))
        BuildConfiguration configuration = configurationManager.createBuildConfiguration(projectDir, GradleDistribution.fromBuild(), false, false, false)

        then:
        configuration.overrideWorkspaceSettings == false
        configuration.buildScansEnabled == buildScansEnabled
        configuration.offlineMode == offlineMode

        cleanup:
        configurationManager.saveWorkspaceConfiguration(orignalConfiguration)

        where:
        buildScansEnabled | offlineMode
        false             | false
        false             | true
        true              | true
        true              | false
    }

    def "new build configuration can override workspace settings"(boolean buildScansEnabled, boolean offlineMode) {
        setup:
        File projectDir = dir('project-dir')

        when:
        BuildConfiguration configuration = configurationManager.createBuildConfiguration(projectDir, GradleDistribution.fromBuild(), true, buildScansEnabled, offlineMode)

        then:
        configuration.overrideWorkspaceSettings == true
        configuration.buildScansEnabled == buildScansEnabled
        configuration.offlineMode == offlineMode

        where:
        buildScansEnabled | offlineMode
        false             | false
        false             | true
        true              | true
        true              | false
    }

    def "no Gradle root project configurations available when there are no projects"() {
        expect:
        configurationManager.loadAllBuildConfigurations().isEmpty()
    }

    def "no Gradle root project configurations available when there are no Eclipse projects with Gradle nature"() {
        given:
        newProject("sample-project")

        expect:
        configurationManager.loadAllBuildConfigurations().isEmpty()
    }

    def "no Gradle root project configurations available when there are no open Eclipse projects with Gradle nature"() {
        given:
        IProject project = workspaceOperations.createProject("sample-project", testDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())
        project.close(null)

        expect:
        configurationManager.loadAllBuildConfigurations().isEmpty()
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

        when:
        importAndWait(rootDir)
        IProject root = findProject('project one')
        Set<BuildConfiguration> configurations = configurationManager.loadAllBuildConfigurations()

        then:
        configurations.size() == 1
        configurations[0].gradleDistribution == GradleDistribution.fromBuild()
        configurations[0].overrideWorkspaceSettings == false
        configurations[0].buildScansEnabled == false
        configurations[0].offlineMode == false
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

        when:
        importAndWait(rootDirOne)
        importAndWait(rootDirTwo, GradleDistribution.forVersion("1.12"))
        IProject rootProjectOne = findProject('project one')
        IProject rootProjectTwo = findProject('project two')
        Set<BuildConfiguration> configurations = configurationManager.loadAllBuildConfigurations()
        BuildConfiguration configuration1 = configurations.find { it.rootProjectDirectory.name == 'root1' }
        BuildConfiguration configuration2 = configurations.find { it.rootProjectDirectory.name == 'root2' }

        then:
        configurations.size() == 2
        configuration1.gradleDistribution == GradleDistribution.fromBuild()
        configuration1.overrideWorkspaceSettings == false
        configuration1.buildScansEnabled == false
        configuration1.offlineMode == false
        configuration2.gradleDistribution == GradleDistribution.forVersion('1.12')
        configuration2.overrideWorkspaceSettings == false
        configuration2.buildScansEnabled == false
        configuration2.offlineMode == false
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
            Set<BuildConfiguration> configurations = configurationManager.loadAllBuildConfigurations()

            then:
            configurations.size() == 1
            configurations[0].rootProjectDirectory.name == 'root1'
        }

    def "can save and load build configuration"() {
        setup:
        File projectDir = dir('project-dir').canonicalFile
        BuildConfiguration configuration = configurationManager.createBuildConfiguration(projectDir, GradleDistribution.forVersion('2.0'), false, false, false)

        when:
        configurationManager.saveBuildConfiguration(configuration)
        configuration = configurationManager.loadBuildConfiguration(projectDir)

        then:
        configuration.rootProjectDirectory == projectDir
        configuration.gradleDistribution == GradleDistribution.forVersion('2.0')
        configuration.overrideWorkspaceSettings == false
        configuration.buildScansEnabled == false
        configuration.offlineMode == false
    }

    def "load build configuration respecting workspaces settings"(boolean buildScansEnabled, boolean offlineMode) {
        setup:
        File projectDir = dir('project-dir')
        WorkspaceConfiguration originalWsConfig = configurationManager.loadWorkspaceConfiguration()
        BuildConfiguration buildConfig = configurationManager.createBuildConfiguration(projectDir, GradleDistribution.fromBuild(), false, false, false)

        when:
        configurationManager.saveBuildConfiguration(buildConfig)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(null, offlineMode, buildScansEnabled))
        buildConfig = configurationManager.loadBuildConfiguration(projectDir)

        then:
        buildConfig.overrideWorkspaceSettings == false
        buildConfig.buildScansEnabled == buildScansEnabled
        buildConfig.offlineMode == offlineMode

        cleanup:
        configurationManager.saveWorkspaceConfiguration(originalWsConfig)

        where:
        buildScansEnabled | offlineMode
        false             | false
        false             | true
        true              | true
        true              | false
    }

    def "load build configuration overriding workspace settings"(boolean buildScansEnabled, boolean offlineMode) {
        setup:
        File projectDir = dir('project-dir')
        WorkspaceConfiguration originalWsConfig = configurationManager.loadWorkspaceConfiguration()
        BuildConfiguration buildConfig = configurationManager.createBuildConfiguration(projectDir, GradleDistribution.fromBuild(), true, buildScansEnabled, offlineMode)

        when:
        configurationManager.saveBuildConfiguration(buildConfig)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(null, !buildScansEnabled, !offlineMode))
        buildConfig = configurationManager.loadBuildConfiguration(projectDir)

        then:
        buildConfig.overrideWorkspaceSettings == true
        buildConfig.buildScansEnabled == buildScansEnabled
        buildConfig.offlineMode == offlineMode

        cleanup:
        configurationManager.saveWorkspaceConfiguration(originalWsConfig)

        where:
        buildScansEnabled | offlineMode
        false             | false
        false             | true
        true              | true
        true              | false
    }

    private void setInvalidPreferenceOn(IProject project) {
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, CorePlugin.PLUGIN_ID)
        preferences.write(BuildConfigurationPersistence.PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, 'I am error.')
        preferences.flush()
    }
}
