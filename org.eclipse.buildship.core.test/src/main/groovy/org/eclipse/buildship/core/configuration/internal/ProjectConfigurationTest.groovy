package org.eclipse.buildship.core.configuration.internal

import spock.lang.Shared

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.BuildConfiguration
import org.eclipse.buildship.core.configuration.ConfigurationManager
import org.eclipse.buildship.core.configuration.ProjectConfiguration
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.workspace.WorkspaceOperations

class ProjectConfigurationTest extends ProjectSynchronizationSpecification {

    @Shared
    ConfigurationManager configurationManager = CorePlugin.configurationManager()

    @Shared
    WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations()

    File projectDir
    File rootProjectDir
    IProject project
    IProject rootProject

    void setup() {
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
        BuildConfiguration buildConfig = configurationManager.createBuildConfiguration(rootProjectDir, GradleDistribution.forVersion('2.0'), null, true, false, false)
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
        BuildConfiguration buildConfig = configurationManager.createBuildConfiguration(rootProjectDir, GradleDistribution.fromBuild(), null, false, false, false)
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
        BuildConfiguration buildConfig = configurationManager.createBuildConfiguration(rootProjectDir, GradleDistribution.fromBuild(), null, false, false, false)
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
        BuildConfiguration buildConfig = configurationManager.createBuildConfiguration(rootProjectDir, GradleDistribution.fromBuild(), null, false, false, false)
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
        BuildConfiguration buildConfig = configurationManager.createBuildConfiguration(rootProjectDir, GradleDistribution.fromBuild(), null, false, false, false)
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
        BuildConfiguration buildConfig = configurationManager.createBuildConfiguration(rootProjectDir, GradleDistribution.fromBuild(), null, false, false, false)
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
         BuildConfiguration buildConfig = configurationManager.createBuildConfiguration(rootProjectDir, GradleDistribution.fromBuild(), null, false, false, false)
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

    def "load build configuration respecting workspaces settings"(boolean buildScansEnabled, boolean offlineMode) {
        setup:
        WorkspaceConfiguration originalWsConfig = configurationManager.loadWorkspaceConfiguration()
        BuildConfiguration buildConfig = configurationManager.createBuildConfiguration(rootProjectDir, GradleDistribution.fromBuild(), null, false, false, false)
        ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);

        when:
        configurationManager.saveProjectConfiguration(projectConfig)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(GradleDistribution.fromBuild(), null, offlineMode, buildScansEnabled))
        projectConfig = configurationManager.loadProjectConfiguration(project)

        then:
        projectConfig.buildConfiguration.overrideWorkspaceSettings == false
        projectConfig.buildConfiguration.buildScansEnabled == buildScansEnabled
        projectConfig.buildConfiguration.offlineMode == offlineMode

        cleanup:
        configurationManager.saveWorkspaceConfiguration(originalWsConfig)

        where:
        buildScansEnabled | offlineMode
        false             | false
        false             | true
        true              | true
        true              | false
    }

    def "load project configuration overriding workspace settings"(boolean buildScansEnabled, boolean offlineMode) {
        setup:
        WorkspaceConfiguration originalWsConfig = configurationManager.loadWorkspaceConfiguration()
        BuildConfiguration buildConfig = configurationManager.createBuildConfiguration(rootProjectDir, GradleDistribution.fromBuild(), null, true, buildScansEnabled, offlineMode)
        ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);

        when:
        configurationManager.saveProjectConfiguration(projectConfig)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(GradleDistribution.fromBuild(), null, !buildScansEnabled, !offlineMode))
        projectConfig = configurationManager.loadProjectConfiguration(project)

        then:
        projectConfig.buildConfiguration.overrideWorkspaceSettings == true
        projectConfig.buildConfiguration.buildScansEnabled == buildScansEnabled
        projectConfig.buildConfiguration.offlineMode == offlineMode

        cleanup:
        configurationManager.saveWorkspaceConfiguration(originalWsConfig)

        where:
        buildScansEnabled | offlineMode
        false             | false
        false             | true
        true              | true
        true              | false
    }

    def "can delete project configuration"() {
        setup:
        BuildConfiguration buildConfig = configurationManager.createBuildConfiguration(rootProjectDir, GradleDistribution.forVersion('2.0'), null, false, false, false)
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
        BuildConfiguration buildConfig = configurationManager.createBuildConfiguration(rootProjectDir, GradleDistribution.forVersion('2.0'), null, false, false, false)
        ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);
        configurationManager.saveProjectConfiguration(projectConfig)
        project.close(new NullProgressMonitor())
        configurationManager.deleteProjectConfiguration(project)

        when:
        configurationManager.loadProjectConfiguration(project)

        then:
        thrown RuntimeException
    }

    private void setInvalidPreferenceOn(IProject project) {
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, CorePlugin.PLUGIN_ID)
        preferences.write(BuildConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, '../nonexistent-project')
        preferences.flush()
    }
}
