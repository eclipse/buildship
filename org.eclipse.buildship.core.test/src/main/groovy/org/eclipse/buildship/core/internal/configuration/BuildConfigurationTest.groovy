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

package org.eclipse.buildship.core.internal.configuration

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification;
import org.eclipse.buildship.core.GradleDistribution

@SuppressWarnings("GroovyAccessibility")
class BuildConfigurationTest extends ProjectSynchronizationSpecification {

    def "can create new build configuration"() {
        setup:
        File projectDir = dir('project-dir').canonicalFile
        File gradleUserHome = dir('gradle-user-home').canonicalFile
        File javaHome = dir('java-home').canonicalFile

        BuildConfiguration configuration = createOverridingBuildConfiguration(dir('project-dir'), GradleDistribution.forVersion('2.0'), false, false, false, gradleUserHome, javaHome)

        expect:
        configuration.rootProjectDirectory == projectDir
        configuration.gradleDistribution == GradleDistribution.forVersion('2.0')
        configuration.gradleUserHome == gradleUserHome
        configuration.javaHome == javaHome
        configuration.overrideWorkspaceSettings == true
        configuration.buildScansEnabled == false
        configuration.offlineMode == false
        configuration.autoSync == false
    }

    def "new build configuration can inherit workspace settings"(GradleDistribution distribution, boolean buildScansEnabled, boolean offlineMode, boolean autoSync) {
        setup:
        File projectDir = dir('project-dir')
        File workspaceGradleUserHome = dir('workspace-gradle-user-home').canonicalFile
        File workspaceJavaHome = dir('workspace-java-home').canonicalFile
        WorkspaceConfiguration orignalConfiguration = configurationManager.loadWorkspaceConfiguration()

        when:
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(distribution, workspaceGradleUserHome, workspaceJavaHome, offlineMode, buildScansEnabled, autoSync, [], [], false, false))
        BuildConfiguration configuration = createInheritingBuildConfiguration(projectDir)

        then:
        configuration.gradleDistribution == distribution
        configuration.gradleUserHome == workspaceGradleUserHome
        configuration.javaHome == workspaceJavaHome
        configuration.overrideWorkspaceSettings == false
        configuration.buildScansEnabled == buildScansEnabled
        configuration.offlineMode == offlineMode
        configuration.autoSync == autoSync

        cleanup:
        configurationManager.saveWorkspaceConfiguration(orignalConfiguration)

        where:
        distribution                                                                 | offlineMode  | buildScansEnabled | autoSync
        GradleDistribution.fromBuild()                                               | false        | false             | true
        GradleDistribution.forVersion("3.2.1")                                       | false        | true              | false
        GradleDistribution.forLocalInstallation(new File('/').canonicalFile)         | true         | true              | false
        GradleDistribution.forRemoteDistribution(new URI('http://example.com/gd'))   | true         | false             | true
    }

    def "new build configuration can override workspace settings"(GradleDistribution distribution, boolean buildScansEnabled, boolean offlineMode, boolean autoSync) {
        setup:
        File projectDir = dir('project-dir')
        File projectGradleUserHome = dir('gradle-user-home').canonicalFile
        File projectJavaHome = dir('java-home').canonicalFile

        when:
        BuildConfiguration configuration = createOverridingBuildConfiguration(projectDir, distribution, buildScansEnabled, offlineMode, autoSync, projectGradleUserHome, projectJavaHome)

        then:
        configuration.gradleDistribution == distribution
        configuration.gradleUserHome == projectGradleUserHome
        configuration.javaHome == projectJavaHome
        configuration.overrideWorkspaceSettings == true
        configuration.buildScansEnabled == buildScansEnabled
        configuration.offlineMode == offlineMode
        configuration.autoSync == autoSync

        where:
        distribution                                                                 | offlineMode  | buildScansEnabled | autoSync
        GradleDistribution.fromBuild()                                               | false        | false             | true
        GradleDistribution.forVersion("3.2.1")                                       | false        | true              | false
        GradleDistribution.forLocalInstallation(new File('/').canonicalFile)         | true         | true              | false
        GradleDistribution.forRemoteDistribution(new URI('http://example.com/gd'))   | true         | false             | true
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
    }

    def "load build configuration respecting workspaces settings"(GradleDistribution distribution, boolean buildScansEnabled, boolean offlineMode, boolean autoSync) {
        setup:
        File projectDir = dir('project-dir')
        WorkspaceConfiguration originalWsConfig = configurationManager.loadWorkspaceConfiguration()
        BuildConfiguration buildConfig = createInheritingBuildConfiguration(projectDir)
        File workspaceGradleUserHome = dir('gradle-user-home').canonicalFile
        File workspaceJavaHome = dir('java-home').canonicalFile

        when:
        configurationManager.saveBuildConfiguration(buildConfig)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(distribution, workspaceGradleUserHome, workspaceJavaHome, offlineMode, buildScansEnabled, autoSync, [], [], false, false))
        buildConfig = configurationManager.loadBuildConfiguration(projectDir)

        then:
        buildConfig.overrideWorkspaceSettings == false
        buildConfig.gradleDistribution == distribution
        buildConfig.gradleUserHome == workspaceGradleUserHome
        buildConfig.javaHome == workspaceJavaHome
        buildConfig.buildScansEnabled == buildScansEnabled
        buildConfig.offlineMode == offlineMode
        buildConfig.autoSync == autoSync

        cleanup:
        configurationManager.saveWorkspaceConfiguration(originalWsConfig)

        where:
        distribution                                                                 | offlineMode  | buildScansEnabled | autoSync
        GradleDistribution.fromBuild()                                               | false        | false             | true
        GradleDistribution.forVersion("3.2.1")                                       | false        | true              | false
        GradleDistribution.forLocalInstallation(new File('/').canonicalFile)         | true         | true              | false
        GradleDistribution.forRemoteDistribution(new URI('http://example.com/gd'))   | true         | false             | true
    }

    def "load build configuration overriding workspace settings"(GradleDistribution distribution, boolean buildScansEnabled, boolean offlineMode, boolean autoSync) {
        setup:
        File projectDir = dir('project-dir')
        WorkspaceConfiguration originalWsConfig = configurationManager.loadWorkspaceConfiguration()
        File projectGradleUserHome = dir('gradle-user-home').canonicalFile
        File projectJavaHome = dir('java-home').canonicalFile
        BuildConfiguration buildConfig = createOverridingBuildConfiguration(projectDir, distribution, buildScansEnabled, offlineMode, autoSync, projectGradleUserHome, projectJavaHome)

        when:
        configurationManager.saveBuildConfiguration(buildConfig)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(GradleDistribution.fromBuild(), null, null, !buildScansEnabled, !offlineMode, !autoSync, [], [], false, false))
        buildConfig = configurationManager.loadBuildConfiguration(projectDir)

        then:
        buildConfig.overrideWorkspaceSettings == true
        buildConfig.gradleDistribution == distribution
        buildConfig.gradleUserHome == projectGradleUserHome
        buildConfig.javaHome == projectJavaHome
        buildConfig.buildScansEnabled == buildScansEnabled
        buildConfig.offlineMode == offlineMode
        buildConfig.autoSync == autoSync

        cleanup:
        configurationManager.saveWorkspaceConfiguration(originalWsConfig)

        where:
        distribution                                                                 | offlineMode  | buildScansEnabled | autoSync
        GradleDistribution.fromBuild()                                               | false        | false             | true
        GradleDistribution.forVersion("3.2.1")                                       | false        | true              | false
        GradleDistribution.forLocalInstallation(new File('/').canonicalFile)         | true         | true              | false
        GradleDistribution.forRemoteDistribution(new URI('http://example.com/gd'))   | true         | false             | true
    }

    private void setInvalidPreferenceOn(IProject project) {
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, CorePlugin.PLUGIN_ID)
        preferences.write(BuildConfigurationPersistence.PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, 'I am error.')
        preferences.flush()
    }
}
