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
        File gradleUserHome = dir('gradle-user-home').canonicalFile
        BuildConfiguration configuration = configurationManager.createBuildConfiguration(dir('project-dir'), GradleDistribution.forVersion('2.0'), gradleUserHome, true, false, false)

        expect:
        configuration.rootProjectDirectory == projectDir
        configuration.gradleDistribution == GradleDistribution.forVersion('2.0')
        configuration.gradleUserHome == gradleUserHome
        configuration.overrideWorkspaceSettings == true
        configuration.buildScansEnabled == false
        configuration.offlineMode == false
    }

    def "new build configuration can inherit workspace settings"(GradleDistribution distribution, String gradleUserHome, boolean buildScansEnabled, boolean offlineMode) {
        setup:
        File projectDir = dir('project-dir')
        File workspaceGradleUserHome = dir('workspace-gradle-user-home').canonicalFile
        WorkspaceConfiguration orignalConfiguration = configurationManager.loadWorkspaceConfiguration()

        when:
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(distribution, workspaceGradleUserHome, offlineMode, buildScansEnabled))
        BuildConfiguration configuration = configurationManager.createBuildConfiguration(projectDir, GradleDistribution.fromBuild(), new File(gradleUserHome), false, false, false)

        then:
        configuration.gradleDistribution == distribution
        configuration.gradleUserHome == workspaceGradleUserHome
        configuration.overrideWorkspaceSettings == false
        configuration.buildScansEnabled == buildScansEnabled
        configuration.offlineMode == offlineMode

        cleanup:
        configurationManager.saveWorkspaceConfiguration(orignalConfiguration)

        where:
        distribution                                                                 | gradleUserHome    | offlineMode  | buildScansEnabled
        GradleDistribution.fromBuild()                                               | 'gradleuserhome1' | false        | false
        GradleDistribution.forVersion("3.2.1")                                       | 'gradleuserhome2' | false        | true
        GradleDistribution.forLocalInstallation(new File('/').canonicalFile)         | 'gradleuserhome3' | true         | true
        GradleDistribution.forRemoteDistribution(new URI('http://example.com/gd'))   | 'gradleuserhome4' | true         | false
    }

    def "new build configuration can override workspace settings"(GradleDistribution distribution, String gradleUserHome, boolean buildScansEnabled, boolean offlineMode) {
        setup:
        File projectDir = dir('project-dir')
        File projectGradleUserHome = dir(gradleUserHome).canonicalFile

        when:
        BuildConfiguration configuration = configurationManager.createBuildConfiguration(projectDir, distribution, projectGradleUserHome, true, buildScansEnabled, offlineMode)

        then:
        configuration.gradleDistribution == distribution
        configuration.gradleUserHome == projectGradleUserHome
        configuration.overrideWorkspaceSettings == true
        configuration.buildScansEnabled == buildScansEnabled
        configuration.offlineMode == offlineMode

        where:
        distribution                                                                 | gradleUserHome    | offlineMode  | buildScansEnabled
        GradleDistribution.fromBuild()                                               | 'gradleuserhome1' | false        | false
        GradleDistribution.forVersion("3.2.1")                                       | 'gradleuserhome2' | false        | true
        GradleDistribution.forLocalInstallation(new File('/').canonicalFile)         | 'gradleuserhome3' | true         | true
        GradleDistribution.forRemoteDistribution(new URI('http://example.com/gd'))   | 'gradleuserhome4' | true         | false
    }

    def "can't load invalid build configuration"() {
        when:
        configurationManager.loadBuildConfiguration(new File('nonexistent'))

        then:
        thrown RuntimeException

        when:
        def projectDir = dir('project-dir'){
            dir('.settings') {
                file "${CorePlugin.PLUGIN_ID}.prefs", """override.workspace.settings=true
connection.gradle.distribution=INVALID_GRADLE_DISTRO"""
            }
        }.canonicalFile
        configurationManager.loadBuildConfiguration(projectDir)

        then:
        thrown RuntimeException
    }

    def "can save and load build configuration"() {
        setup:
        File projectDir = dir('project-dir').canonicalFile
        BuildConfiguration configuration = configurationManager.createBuildConfiguration(projectDir, GradleDistribution.forVersion('2.0'), null, true, false, false)

        when:
        configurationManager.saveBuildConfiguration(configuration)
        configuration = configurationManager.loadBuildConfiguration(projectDir)

        then:
        configuration.rootProjectDirectory == projectDir
        configuration.gradleDistribution == GradleDistribution.forVersion('2.0')
        configuration.gradleUserHome == null
        configuration.overrideWorkspaceSettings == true
        configuration.buildScansEnabled == false
        configuration.offlineMode == false
    }

    def "can load build configuration from closed projects"() {
        setup:
        IProject project = newProject('project')
        File projectDir = project.location.toFile()
        BuildConfiguration configuration = configurationManager.createBuildConfiguration(projectDir, GradleDistribution.forVersion('2.0'), null, true, false, false)

        when:
        project.close(new NullProgressMonitor())
        configurationManager.saveBuildConfiguration(configuration)
        configuration = configurationManager.loadBuildConfiguration(projectDir)

        then:
        configuration.rootProjectDirectory == projectDir
        configuration.gradleDistribution == GradleDistribution.forVersion('2.0')
        configuration.gradleUserHome == null
        configuration.overrideWorkspaceSettings == true
        configuration.buildScansEnabled == false
        configuration.offlineMode == false
    }

    def "load build configuration respecting workspaces settings"(GradleDistribution distribution, String gradleUserHome, boolean buildScansEnabled, boolean offlineMode) {
        setup:
        File projectDir = dir('project-dir')
        WorkspaceConfiguration originalWsConfig = configurationManager.loadWorkspaceConfiguration()
        BuildConfiguration buildConfig = configurationManager.createBuildConfiguration(projectDir, GradleDistribution.fromBuild(), null, false, false, false)
        File workspaceGradleUserHome = dir(gradleUserHome).canonicalFile

        when:
        configurationManager.saveBuildConfiguration(buildConfig)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(distribution, workspaceGradleUserHome, offlineMode, buildScansEnabled))
        buildConfig = configurationManager.loadBuildConfiguration(projectDir)

        then:
        buildConfig.overrideWorkspaceSettings == false
        buildConfig.gradleDistribution == distribution
        buildConfig.gradleUserHome == workspaceGradleUserHome
        buildConfig.buildScansEnabled == buildScansEnabled
        buildConfig.offlineMode == offlineMode

        cleanup:
        configurationManager.saveWorkspaceConfiguration(originalWsConfig)

        where:
        distribution                                                                 | gradleUserHome    | offlineMode  | buildScansEnabled
        GradleDistribution.fromBuild()                                               | 'gradleuserhome1' | false        | false
        GradleDistribution.forVersion("3.2.1")                                       | 'gradleuserhome2' | false        | true
        GradleDistribution.forLocalInstallation(new File('/').canonicalFile)         | 'gradleuserhome3' | true         | true
        GradleDistribution.forRemoteDistribution(new URI('http://example.com/gd'))   | 'gradleuserhome4' | true         | false
    }

    def "load build configuration overriding workspace settings"(GradleDistribution distribution, String gradleUserHome, boolean buildScansEnabled, boolean offlineMode) {
        setup:
        File projectDir = dir('project-dir')
        WorkspaceConfiguration originalWsConfig = configurationManager.loadWorkspaceConfiguration()
        File projectGradleUserHome = dir(gradleUserHome).canonicalFile
        BuildConfiguration buildConfig = configurationManager.createBuildConfiguration(projectDir, distribution, projectGradleUserHome, true, buildScansEnabled, offlineMode)

        when:
        configurationManager.saveBuildConfiguration(buildConfig)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(GradleDistribution.fromBuild(), null, !buildScansEnabled, !offlineMode))
        buildConfig = configurationManager.loadBuildConfiguration(projectDir)

        then:
        buildConfig.overrideWorkspaceSettings == true
        buildConfig.gradleDistribution == distribution
        buildConfig.gradleUserHome == projectGradleUserHome
        buildConfig.buildScansEnabled == buildScansEnabled
        buildConfig.offlineMode == offlineMode

        cleanup:
        configurationManager.saveWorkspaceConfiguration(originalWsConfig)

        where:
        distribution                                                                 | gradleUserHome    | offlineMode  | buildScansEnabled
        GradleDistribution.fromBuild()                                               | 'gradleuserhome1' | false        | false
        GradleDistribution.forVersion("3.2.1")                                       | 'gradleuserhome2' | false        | true
        GradleDistribution.forLocalInstallation(new File('/').canonicalFile)         | 'gradleuserhome3' | true         | true
        GradleDistribution.forRemoteDistribution(new URI('http://example.com/gd'))   | 'gradleuserhome4' | true         | false
    }

    private void setInvalidPreferenceOn(IProject project) {
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, CorePlugin.PLUGIN_ID)
        preferences.write(BuildConfigurationPersistence.PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, 'I am error.')
        preferences.flush()
    }
}
