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
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(GradleDistribution.fromBuild(), dir('gradle-user-home'), offlineMode, buildScansEnabled))
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

    def "can't load invalid build configuration"() {
        when:
        configurationManager.loadBuildConfiguration(new File('nonexistent'))

        then:
        thrown RuntimeException

        when:
        def projectDir = dir('project-dir').canonicalFile
        configurationManager.loadBuildConfiguration(projectDir)

        then:
        thrown RuntimeException

        when:
        new File(projectDir,"${CorePlugin.PLUGIN_ID}.prefs").text = "connection.gradle.distribution=INVALID_GRADLE_DISTRO"
        configurationManager.loadBuildConfiguration(projectDir)

        then:
        thrown RuntimeException
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

    def "can load build configuration from closed projects"() {
        setup:
        IProject project = newProject('project')
        File projectDir = project.location.toFile()
        BuildConfiguration configuration = configurationManager.createBuildConfiguration(projectDir, GradleDistribution.forVersion('2.0'), false, false, false)

        when:
        project.close(new NullProgressMonitor())
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
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(GradleDistribution.fromBuild(), null, offlineMode, buildScansEnabled))
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
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(GradleDistribution.fromBuild(), null, !buildScansEnabled, !offlineMode))
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
