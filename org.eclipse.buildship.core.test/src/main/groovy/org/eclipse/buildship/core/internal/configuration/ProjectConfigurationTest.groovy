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

import spock.lang.Issue

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class ProjectConfigurationTest extends ProjectSynchronizationSpecification {

    File projectDir
    File rootProjectDir
    IProject project
    IProject rootProject

    def setup() {
        projectDir = dir('project-dir').canonicalFile
        rootProjectDir = dir('root-project-dir').canonicalFile
        project = workspaceOperations.createProject("sample-project", projectDir, [], new NullProgressMonitor())
        rootProject = workspaceOperations.createProject("root-project", rootProjectDir, [], new NullProgressMonitor())
    }

    def "missing project configuration"() {
        when:
        configurationManager.loadProjectConfiguration(project)

        then:
        thrown RuntimeException
    }

    def "can save and load project configuration"() {
        given:
        BuildConfiguration buildConfig = createOverridingBuildConfiguration(rootProjectDir, GradleDistribution.forVersion('2.0'))
        ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);

        when:
        configurationManager.saveProjectConfiguration(projectConfig)
        projectConfig = configurationManager.loadProjectConfiguration(project)

        then:
        projectConfig.projectDir == projectDir
        projectConfig.buildConfiguration == buildConfig
    }

    def "can save and load project configuration if projects are closed"() {
        given:
        BuildConfiguration buildConfig = createInheritingBuildConfiguration(rootProjectDir)
        ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);
        project.close(new NullProgressMonitor())
        rootProject.close(new NullProgressMonitor())

        when:
        configurationManager.saveProjectConfiguration(projectConfig)
        ProjectConfiguration loadedConfig = configurationManager.loadProjectConfiguration(project)

        then:
        loadedConfig == projectConfig
    }

    def "can save and load project configuration if root project is not in the workspace"() {
        given:
        BuildConfiguration buildConfig = createInheritingBuildConfiguration(rootProjectDir)
        ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);
        rootProject.delete(false, false, new NullProgressMonitor())

        when:
        configurationManager.saveProjectConfiguration(projectConfig)
        ProjectConfiguration loadedConfig = configurationManager.loadProjectConfiguration(project)

        then:
        loadedConfig == projectConfig
    }

    def "project configuration can be read if project is not yet refreshed"() {
        given:
        BuildConfiguration buildConfig = createInheritingBuildConfiguration(rootProjectDir)
        ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);
        configurationManager.saveProjectConfiguration(projectConfig)

        def projectDescription = project.description
        project.delete(false, true, null)
        project.create(projectDescription, null)
        project.open(IResource.BACKGROUND_REFRESH, null)

        when:
        ProjectConfiguration loadedConfig = configurationManager.loadProjectConfiguration(project)

        then:
        loadedConfig == projectConfig
    }

    def "project configuration can be read if the project is closed"() {
        given:
        BuildConfiguration buildConfig = createInheritingBuildConfiguration(rootProjectDir)
        ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);
        configurationManager.saveProjectConfiguration(projectConfig)

        project.close(new NullProgressMonitor())

        when:
        ProjectConfiguration loadedConfig = configurationManager.loadProjectConfiguration(project)

        then:
        loadedConfig == projectConfig
    }

    def "project configuration can be read if the root project is closed"() {
        given:
        BuildConfiguration buildConfig = createInheritingBuildConfiguration(rootProjectDir)
        ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);
        configurationManager.saveProjectConfiguration(projectConfig)

        rootProject.close(new NullProgressMonitor())

        when:
        ProjectConfiguration loadedConfig = configurationManager.loadProjectConfiguration(project)

        then:
        loadedConfig == projectConfig
    }

    def "invalid project configuration results in runtime exception"() {
         when:
         configurationManager.loadProjectConfiguration(project)

         then:
         thrown RuntimeException

         when:
         BuildConfiguration buildConfig = createInheritingBuildConfiguration(rootProjectDir)
         ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);
         configurationManager.saveProjectConfiguration(projectConfig)

         then:
         configurationManager.loadProjectConfiguration(project)

         when:
         setInvalidPreferenceOn(project)
         configurationManager.loadProjectConfiguration(project)

         then:
         thrown RuntimeException
    }

    def "can read project configuration safely with tryLoadProjectConfiguration method"() {
        when:
        ProjectConfiguration projectConfiguration = configurationManager.tryLoadProjectConfiguration(project)

        then:
        projectConfiguration == null

        when:
        BuildConfiguration buildConfig = createInheritingBuildConfiguration(rootProjectDir)
        ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);
        configurationManager.saveProjectConfiguration(projectConfig)

        then:
        configurationManager.loadProjectConfiguration(project) == configurationManager.tryLoadProjectConfiguration(project)

        when:
        setInvalidPreferenceOn(project)
        projectConfiguration = configurationManager.tryLoadProjectConfiguration(project)

        then:
        projectConfiguration == null
    }

    def "load build configuration respecting workspaces settings"(GradleDistribution distribution, boolean buildScansEnabled, boolean offlineMode, boolean autoSync, boolean showConsole, boolean showExecutions) {
        setup:
        WorkspaceConfiguration originalWsConfig = configurationManager.loadWorkspaceConfiguration()
        BuildConfiguration buildConfig =  createInheritingBuildConfiguration(rootProjectDir)
        ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);
        File gradleUserHome = dir('gradle-user-home').canonicalFile
        File javaHome = dir('java-home').canonicalFile
        List<String> arguments = ['--info']
        List<String> jvmArguments = ['-Dfoo=bar']

        when:
        configurationManager.saveProjectConfiguration(projectConfig)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(distribution, gradleUserHome, javaHome, offlineMode, buildScansEnabled, autoSync, arguments, jvmArguments, showConsole, showExecutions, false))
        projectConfig = configurationManager.loadProjectConfiguration(project)

        then:
        projectConfig.buildConfiguration.overrideWorkspaceSettings == false
        projectConfig.buildConfiguration.gradleDistribution == distribution
        projectConfig.buildConfiguration.gradleUserHome == gradleUserHome
        projectConfig.buildConfiguration.buildScansEnabled == buildScansEnabled
        projectConfig.buildConfiguration.offlineMode == offlineMode
        projectConfig.buildConfiguration.autoSync == autoSync

        cleanup:
        configurationManager.saveWorkspaceConfiguration(originalWsConfig)

        where:
        distribution                         | buildScansEnabled | offlineMode | autoSync | showConsole | showExecutions
        GradleDistribution.forVersion('3.5') | false             | false       | true     | false       | true
        GradleDistribution.forVersion('3.4') | false             | true        | false    | true        | false
        GradleDistribution.forVersion('3.3') | true              | false       | false    | true        | false
        GradleDistribution.forVersion('3.2') | true              | true        | true     | false       | true
    }

    def "load project configuration overriding workspace settings"(GradleDistribution distribution, boolean buildScansEnabled, boolean offlineMode, boolean autoSync, boolean showConsole, boolean showExecutions) {
        setup:
        WorkspaceConfiguration originalWsConfig = configurationManager.loadWorkspaceConfiguration()
        File gradleUserHome = dir('gradle-user-home').canonicalFile
        File javaHome = dir('java-home').canonicalFile
        List<String> arguments = ['--info']
        List<String> jvmArguments = ['-Dfoo=bar']
        BuildConfiguration buildConfig = createOverridingBuildConfiguration(rootProjectDir, distribution, buildScansEnabled, offlineMode, autoSync, gradleUserHome, javaHome, arguments, jvmArguments, showConsole, showExecutions)
        ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);

        when:
        configurationManager.saveProjectConfiguration(projectConfig)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(GradleDistribution.fromBuild(), null, null, !buildScansEnabled, !offlineMode, !autoSync, [], [], false, false, false))
        projectConfig = configurationManager.loadProjectConfiguration(project)

        then:
        projectConfig.buildConfiguration.overrideWorkspaceSettings == true
        projectConfig.buildConfiguration.gradleDistribution == distribution
        projectConfig.buildConfiguration.gradleUserHome == gradleUserHome
        projectConfig.buildConfiguration.javaHome == javaHome
        projectConfig.buildConfiguration.buildScansEnabled == buildScansEnabled
        projectConfig.buildConfiguration.offlineMode == offlineMode
        projectConfig.buildConfiguration.autoSync == autoSync

        cleanup:
        configurationManager.saveWorkspaceConfiguration(originalWsConfig)

        where:
        distribution                         | buildScansEnabled | offlineMode | autoSync | showConsole | showExecutions
        GradleDistribution.forVersion('3.5') | false             | false       | true     | false       | true
        GradleDistribution.forVersion('3.4') | false             | true        | false    | true        | false
        GradleDistribution.forVersion('3.3') | true              | false       | false    | true        | false
        GradleDistribution.forVersion('3.2') | true              | true        | true     | false       | true
    }

    def "can delete project configuration"() {
        setup:
        BuildConfiguration buildConfig = createInheritingBuildConfiguration(rootProjectDir)
        ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);
        configurationManager.saveProjectConfiguration(projectConfig)
        configurationManager.deleteProjectConfiguration(project)

        when:
        configurationManager.loadProjectConfiguration(project)

        then:
        thrown RuntimeException
    }

    def "can delete project configuration on closed projects"() {
        setup:
        BuildConfiguration buildConfig = createInheritingBuildConfiguration(rootProjectDir)
        ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);
        configurationManager.saveProjectConfiguration(projectConfig)
        project.close(new NullProgressMonitor())
        configurationManager.deleteProjectConfiguration(project)

        when:
        configurationManager.loadProjectConfiguration(project)

        then:
        thrown RuntimeException
    }

    @Issue('https://github.com/eclipse/buildship/issues/528')
    def "can save and load project configuration if settings file contains absolute path"() {
        setup:
        configurationManager.buildConfigurationPersistence.savePathToRoot(project, rootProjectDir.absolutePath)

        when:
        ProjectConfiguration projectConfig = configurationManager.loadProjectConfiguration(project)

        then:
        projectConfig.buildConfiguration.rootProjectDirectory == rootProjectDir

        when:
        configurationManager.saveProjectConfiguration(projectConfig)

        then:
        configurationManager.buildConfigurationPersistence.readPathToRoot(project) == "../$rootProjectDir.name"
    }

    @Issue('https://github.com/eclipse/buildship/issues/528')
    def "can save and load project configuration if project is closed and settings file contains absolute path"() {
        setup:
        project.close(new NullProgressMonitor())
        configurationManager.buildConfigurationPersistence.savePathToRoot(projectDir, rootProjectDir.absolutePath)

        when:
        ProjectConfiguration projectConfig = configurationManager.loadProjectConfiguration(projectDir)

        then:
        projectConfig.buildConfiguration.rootProjectDirectory == rootProjectDir

        when:
        configurationManager.saveProjectConfiguration(projectConfig)

        then:
        configurationManager.buildConfigurationPersistence.readPathToRoot(projectDir) == "../$rootProjectDir.name"
    }

    private void setInvalidPreferenceOn(IProject project) {
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, CorePlugin.PLUGIN_ID)
        preferences.write(BuildConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, '../nonexistent-project')
        preferences.flush()
    }
}
