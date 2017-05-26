package org.eclipse.buildship.core.configuration.internal

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.BuildConfiguration
import org.eclipse.buildship.core.configuration.ProjectConfiguration
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification

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

    def "load build configuration respecting workspaces settings"(GradleDistribution distribution, boolean buildScansEnabled, boolean offlineMode) {
        setup:
        WorkspaceConfiguration originalWsConfig = configurationManager.loadWorkspaceConfiguration()
        BuildConfiguration buildConfig =  createInheritingBuildConfiguration(rootProjectDir)
        ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);
        File gradleUserHome = dir('gradle-user-home').canonicalFile

        when:
        configurationManager.saveProjectConfiguration(projectConfig)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(distribution, gradleUserHome, offlineMode, buildScansEnabled))
        projectConfig = configurationManager.loadProjectConfiguration(project)

        then:
        projectConfig.buildConfiguration.overrideWorkspaceSettings == false
        projectConfig.buildConfiguration.gradleDistribution == distribution
        projectConfig.buildConfiguration.gradleUserHome == gradleUserHome
        projectConfig.buildConfiguration.buildScansEnabled == buildScansEnabled
        projectConfig.buildConfiguration.offlineMode == offlineMode

        cleanup:
        configurationManager.saveWorkspaceConfiguration(originalWsConfig)

        where:
        distribution                         | buildScansEnabled | offlineMode
        GradleDistribution.forVersion('3.5') | false             | false
        GradleDistribution.forVersion('3.4') | false             | true
        GradleDistribution.forVersion('3.3') | true              | false
        GradleDistribution.forVersion('3.2') | true              | true
    }

    def "load project configuration overriding workspace settings"(GradleDistribution distribution, boolean buildScansEnabled, boolean offlineMode) {
        setup:
        WorkspaceConfiguration originalWsConfig = configurationManager.loadWorkspaceConfiguration()
        File gradleUserHome = dir('gradle-user-home').canonicalFile
        BuildConfiguration buildConfig = createOverridingBuildConfiguration(rootProjectDir, distribution, buildScansEnabled, offlineMode, gradleUserHome)
        ProjectConfiguration projectConfig = configurationManager.createProjectConfiguration(buildConfig, projectDir);

        when:
        configurationManager.saveProjectConfiguration(projectConfig)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(GradleDistribution.fromBuild(), null, !buildScansEnabled, !offlineMode))
        projectConfig = configurationManager.loadProjectConfiguration(project)

        then:
        projectConfig.buildConfiguration.overrideWorkspaceSettings == true
        projectConfig.buildConfiguration.gradleDistribution == distribution
        projectConfig.buildConfiguration.gradleUserHome == gradleUserHome
        projectConfig.buildConfiguration.buildScansEnabled == buildScansEnabled
        projectConfig.buildConfiguration.offlineMode == offlineMode

        cleanup:
        configurationManager.saveWorkspaceConfiguration(originalWsConfig)

        where:
        distribution                         | buildScansEnabled | offlineMode
        GradleDistribution.forVersion('3.5') | false             | false
        GradleDistribution.forVersion('3.4') | false             | true
        GradleDistribution.forVersion('3.3') | true              | false
        GradleDistribution.forVersion('3.2') | true              | true
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

    private void setInvalidPreferenceOn(IProject project) {
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, CorePlugin.PLUGIN_ID)
        preferences.write(BuildConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, '../nonexistent-project')
        preferences.flush()
    }
}
