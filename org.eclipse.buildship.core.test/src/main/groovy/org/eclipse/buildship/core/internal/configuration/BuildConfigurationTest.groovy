/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification;

@SuppressWarnings("GroovyAccessibility")
class BuildConfigurationTest extends ProjectSynchronizationSpecification {

    def "can create new build configuration"() {
        setup:
        File projectDir = dir('project-dir').canonicalFile
        File gradleUserHome = dir('gradle-user-home').canonicalFile
        File javaHome = dir('java-home').canonicalFile
        List<String> arguments = ['--info']
        List<String> jvmArguments = ['-Dfoo=bar']
        boolean showConsole = false
        boolean showExecutions = false

        BuildConfiguration configuration = createOverridingBuildConfiguration(dir('project-dir'), GradleDistribution.forVersion('2.0'), false, false, false, gradleUserHome, javaHome, arguments, jvmArguments, showConsole, showExecutions)

        expect:
        configuration.rootProjectDirectory == projectDir
        configuration.gradleDistribution == GradleDistribution.forVersion('2.0')
        configuration.gradleUserHome == gradleUserHome
        configuration.javaHome == javaHome
        configuration.overrideWorkspaceSettings == true
        configuration.buildScansEnabled == false
        configuration.offlineMode == false
        configuration.autoSync == false
        configuration.arguments == arguments
        configuration.jvmArguments == jvmArguments
        configuration.showConsoleView == showConsole
        configuration.showExecutionsView == showExecutions
    }

    def "new build configuration can inherit workspace settings"() {
        setup:
        File projectDir = dir('project-dir')
        File workspaceGradleUserHome = dir('workspace-gradle-user-home').canonicalFile
        File workspaceJavaHome = dir('workspace-java-home').canonicalFile
        List<String> workspaceArguments = ['--info']
        List<String> workspaceJvmArguments = ['-Dfoo=bar']
        WorkspaceConfiguration orignalConfiguration = configurationManager.loadWorkspaceConfiguration()

        when:
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(distribution, workspaceGradleUserHome, workspaceJavaHome, offlineMode, buildScansEnabled, autoSync, workspaceArguments, workspaceJvmArguments, showConsole, showExecutions, false))
        BuildConfiguration configuration = createInheritingBuildConfiguration(projectDir)

        then:
        configuration.gradleDistribution == distribution
        configuration.gradleUserHome == workspaceGradleUserHome
        configuration.javaHome == workspaceJavaHome
        configuration.overrideWorkspaceSettings == false
        configuration.buildScansEnabled == buildScansEnabled
        configuration.offlineMode == offlineMode
        configuration.autoSync == autoSync
        configuration.arguments == workspaceArguments
        configuration.jvmArguments == workspaceJvmArguments
        configuration.showConsoleView == showConsole
        configuration.showExecutionsView == showExecutions

        cleanup:
        configurationManager.saveWorkspaceConfiguration(orignalConfiguration)

        where:
        distribution                                                                 | offlineMode  | buildScansEnabled | autoSync | showConsole | showExecutions
        GradleDistribution.fromBuild()                                               | false        | false             | true     | false       | true
        GradleDistribution.forVersion("3.2.1")                                       | false        | true              | false    | true        | false
        GradleDistribution.forLocalInstallation(new File('/').canonicalFile)         | true         | true              | false    | true        | false
        GradleDistribution.forRemoteDistribution(new URI('http://example.com/gd'))   | true         | false             | true     | false       | true
    }

    def "new build configuration can override workspace settings"() {
        setup:
        File projectDir = dir('project-dir')
        File projectGradleUserHome = dir('gradle-user-home').canonicalFile
        File projectJavaHome = dir('java-home').canonicalFile
        List<String> projectArguments = ['--info']
        List<String> projectJvmArguments = ['-Dfoo=bar']


        when:
        BuildConfiguration configuration = createOverridingBuildConfiguration(projectDir, distribution, buildScansEnabled, offlineMode, autoSync, projectGradleUserHome, projectJavaHome, projectArguments, projectJvmArguments, showConsole, showExecutions)

        then:
        configuration.gradleDistribution == distribution
        configuration.gradleUserHome == projectGradleUserHome
        configuration.javaHome == projectJavaHome
        configuration.overrideWorkspaceSettings == true
        configuration.buildScansEnabled == buildScansEnabled
        configuration.offlineMode == offlineMode
        configuration.autoSync == autoSync
        configuration.arguments == projectArguments
        configuration.jvmArguments == projectJvmArguments
        configuration.showConsoleView == showConsole
        configuration.showExecutionsView == showExecutions

        where:
        distribution                                                                 | offlineMode  | buildScansEnabled | autoSync | showConsole | showExecutions
        GradleDistribution.fromBuild()                                               | false        | false             | true     | false       | true
        GradleDistribution.forVersion("3.2.1")                                       | false        | true              | false    | true        | false
        GradleDistribution.forLocalInstallation(new File('/').canonicalFile)         | true         | true              | false    | true        | false
        GradleDistribution.forRemoteDistribution(new URI('http://example.com/gd'))   | true         | false             | true     | false       | true
    }

    def "can't load invalid build configuration"() {
        when:
        configurationManager.loadBuildConfiguration(new File('nonexistent'))

        then:
        thrown RuntimeException

        when:
        def projectDir = dir('project-dir'){
            dir('.settings') {
                file "${CorePlugin.PLUGIN_ID}.prefs", """override.workspace.settings=not_true_nor_false
connection.gradle.distribution=MODIFIED_DISTRO"""
            }
        }.canonicalFile
        BuildConfiguration configuration = configurationManager.loadBuildConfiguration(projectDir)

        then:
        configuration.gradleDistribution == GradleDistribution.fromBuild()
        configuration.gradleUserHome == null
        configuration.overrideWorkspaceSettings == false
        configuration.buildScansEnabled == false
        configuration.offlineMode == false
        configuration.autoSync == false
    }

    def "can save and load build configuration"() {
        setup:
        File projectDir = dir('project-dir').canonicalFile
        BuildConfiguration configuration = createOverridingBuildConfiguration(projectDir, GradleDistribution.forVersion('2.0'))

        when:
        configurationManager.saveBuildConfiguration(configuration)
        configuration = configurationManager.loadBuildConfiguration(projectDir)

        then:
        configuration.rootProjectDirectory == projectDir
        configuration.gradleDistribution == GradleDistribution.forVersion('2.0')
        configuration.gradleUserHome == null
        configuration.javaHome == null
        configuration.overrideWorkspaceSettings == true
        configuration.buildScansEnabled == false
        configuration.offlineMode == false
        configuration.autoSync == false
        configuration.arguments == []
        configuration.jvmArguments == []
        configuration.showConsoleView == true
        configuration.showExecutionsView == true
    }

    def "can load build configuration from closed projects"() {
        setup:
        IProject project = newProject('project')
        File projectDir = project.location.toFile()
        BuildConfiguration configuration = createOverridingBuildConfiguration(projectDir, GradleDistribution.forVersion('2.0'))

        when:
        project.close(new NullProgressMonitor())
        configurationManager.saveBuildConfiguration(configuration)
        configuration = configurationManager.loadBuildConfiguration(projectDir)

        then:
        configuration.rootProjectDirectory == projectDir
        configuration.gradleDistribution == GradleDistribution.forVersion('2.0')
        configuration.gradleUserHome == null
        configuration.javaHome == null
        configuration.overrideWorkspaceSettings == true
        configuration.buildScansEnabled == false
        configuration.offlineMode == false
        configuration.autoSync == false
        configuration.arguments == []
        configuration.jvmArguments == []
        configuration.showConsoleView == true
        configuration.showExecutionsView == true
    }

    def "load build configuration respecting workspaces settings"() {
        setup:
        File projectDir = dir('project-dir')
        WorkspaceConfiguration originalWsConfig = configurationManager.loadWorkspaceConfiguration()
        BuildConfiguration buildConfig = createInheritingBuildConfiguration(projectDir)
        File workspaceGradleUserHome = dir('gradle-user-home').canonicalFile
        File workspaceJavaHome = dir('java-home').canonicalFile
        List<String> workspaceArguments = ['--info']
        List<String> workspaceJvmArguments = ['-Dfoo=bar']

        when:
        configurationManager.saveBuildConfiguration(buildConfig)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(distribution, workspaceGradleUserHome, workspaceJavaHome, offlineMode, buildScansEnabled, autoSync, workspaceArguments, workspaceJvmArguments, showConsole, showExecutions, false))
        buildConfig = configurationManager.loadBuildConfiguration(projectDir)

        then:
        buildConfig.overrideWorkspaceSettings == false
        buildConfig.gradleDistribution == distribution
        buildConfig.gradleUserHome == workspaceGradleUserHome
        buildConfig.javaHome == workspaceJavaHome
        buildConfig.buildScansEnabled == buildScansEnabled
        buildConfig.offlineMode == offlineMode
        buildConfig.autoSync == autoSync
        buildConfig.arguments == workspaceArguments
        buildConfig.jvmArguments == workspaceJvmArguments
        buildConfig.showConsoleView == showConsole
        buildConfig.showExecutionsView == showExecutions

        cleanup:
        configurationManager.saveWorkspaceConfiguration(originalWsConfig)

        where:
        distribution                                                                 | offlineMode  | buildScansEnabled | autoSync | showConsole | showExecutions
        GradleDistribution.fromBuild()                                               | false        | false             | true     | false       | true
        GradleDistribution.forVersion("3.2.1")                                       | false        | true              | false    | true        | false
        GradleDistribution.forLocalInstallation(new File('/').canonicalFile)         | true         | true              | false    | true        | false
        GradleDistribution.forRemoteDistribution(new URI('http://example.com/gd'))   | true         | false             | true     | false       | true
    }

    def "load build configuration overriding workspace settings"() {
        setup:
        File projectDir = dir('project-dir')
        WorkspaceConfiguration originalWsConfig = configurationManager.loadWorkspaceConfiguration()
        File projectGradleUserHome = dir('gradle-user-home').canonicalFile
        File projectJavaHome = dir('java-home').canonicalFile
        List<String> projectArguments = ['--info']
        List<String> projectJvmArguments = ['-Dfoo=bar']
        BuildConfiguration buildConfig = createOverridingBuildConfiguration(projectDir, distribution, buildScansEnabled, offlineMode, autoSync, projectGradleUserHome, projectJavaHome, projectArguments, projectJvmArguments, showConsole, showExecutions)

        when:
        configurationManager.saveBuildConfiguration(buildConfig)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(GradleDistribution.fromBuild(), null, null, !buildScansEnabled, !offlineMode, !autoSync, [], [], false, false, false))
        buildConfig = configurationManager.loadBuildConfiguration(projectDir)

        then:
        buildConfig.overrideWorkspaceSettings == true
        buildConfig.gradleDistribution == distribution
        buildConfig.gradleUserHome == projectGradleUserHome
        buildConfig.javaHome == projectJavaHome
        buildConfig.buildScansEnabled == buildScansEnabled
        buildConfig.offlineMode == offlineMode
        buildConfig.autoSync == autoSync
        buildConfig.arguments == projectArguments
        buildConfig.jvmArguments == projectJvmArguments
        buildConfig.showConsoleView == showConsole
        buildConfig.showExecutionsView == showExecutions

        cleanup:
        configurationManager.saveWorkspaceConfiguration(originalWsConfig)

        where:
        distribution                                                                 | offlineMode  | buildScansEnabled | autoSync | showConsole | showExecutions
        GradleDistribution.fromBuild()                                               | false        | false             | true     | false       | true
        GradleDistribution.forVersion("3.2.1")                                       | false        | true              | false    | true        | false
        GradleDistribution.forLocalInstallation(new File('/').canonicalFile)         | true         | true              | false    | true        | false
        GradleDistribution.forRemoteDistribution(new URI('http://example.com/gd'))   | true         | false             | true     | false       | true
    }

    private void setInvalidPreferenceOn(IProject project) {
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, CorePlugin.PLUGIN_ID)
        preferences.write(BuildConfigurationPersistence.PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, 'I am error.')
        preferences.flush()
    }
}
