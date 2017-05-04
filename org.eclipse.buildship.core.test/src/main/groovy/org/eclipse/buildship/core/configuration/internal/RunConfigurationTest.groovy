package org.eclipse.buildship.core.configuration.internal

import spock.lang.Shared

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationType
import org.eclipse.debug.core.ILaunchManager

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.ConfigurationManager
import org.eclipse.buildship.core.configuration.RunConfiguration
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes
import org.eclipse.buildship.core.launch.GradleRunConfigurationDelegate
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification

class RunConfigurationTest extends ProjectSynchronizationSpecification {

    @Shared
    ConfigurationManager configurationManager = CorePlugin.configurationManager()

    def "load default settings"() {
        given:
        ILaunchConfiguration launchConfig = emptyLaunchConfig()
        RunConfiguration runConfig = configurationManager.loadRunConfiguration(launchConfig)

        expect:
        runConfig.tasks == []
        runConfig.javaHome == null
        runConfig.arguments == []
        runConfig.jvmArguments == []
        runConfig.showExecutionView == true
        runConfig.showConsoleView == true
        runConfig.buildConfiguration.rootProjectDirectory.path == new File('').canonicalPath
        runConfig.buildConfiguration.gradleDistribution == GradleDistribution.fromBuild()
        runConfig.buildConfiguration.overrideWorkspaceSettings == false
        runConfig.buildConfiguration.buildScansEnabled == false
        runConfig.buildConfiguration.offlineMode == false
        runConfig.buildConfiguration.workspaceConfiguration.gradleUserHome == null
        runConfig.buildConfiguration.workspaceConfiguration.gradleIsOffline == false
        runConfig.buildConfiguration.workspaceConfiguration.buildScansEnabled == false
    }

    def "load custom settings"() {
        setup:
        List tasks = ['clean', 'build']
        File javaHome = dir('custom-java-home')
        List arguments = ['-q', '-Pkey=value']
        List jvmArguments = ['-ea', '-Dkey=value']
        boolean showConsoleView = false
        boolean showExecutionView = false
        File rootDir = dir('projectDir').canonicalFile
        GradleDistribution distribution = GradleDistribution.forVersion("3.3")
        boolean overrideBuildSettings = true
        boolean buildScansEnabled = true
        boolean offlineMode = true

        ILaunchConfiguration launchConfig = emptyLaunchConfig()
        GradleRunConfigurationAttributes.applyTasks(tasks, launchConfig)
        GradleRunConfigurationAttributes.applyJavaHomeExpression(javaHome.absolutePath, launchConfig)
        GradleRunConfigurationAttributes.applyArgumentExpressions(arguments, launchConfig)
        GradleRunConfigurationAttributes.applyJvmArgumentExpressions(jvmArguments, launchConfig)
        GradleRunConfigurationAttributes.applyShowConsoleView(showConsoleView, launchConfig)
        GradleRunConfigurationAttributes.applyShowExecutionView(showExecutionView, launchConfig)
        GradleRunConfigurationAttributes.applyWorkingDirExpression(rootDir.absolutePath, launchConfig)
        GradleRunConfigurationAttributes.applyGradleDistribution(distribution, launchConfig)
        GradleRunConfigurationAttributes.applyOverrideBuildSettings(overrideBuildSettings, launchConfig)
        GradleRunConfigurationAttributes.applyBuildScansEnabled(buildScansEnabled, launchConfig)
        GradleRunConfigurationAttributes.applyOfflineMode(offlineMode, launchConfig)

        when:
        RunConfiguration runConfig = configurationManager.loadRunConfiguration(launchConfig)

        then:
        runConfig.tasks == tasks
        runConfig.javaHome == javaHome
        runConfig.arguments == arguments + '--offline'
        runConfig.jvmArguments == jvmArguments + '-Dscan'
        runConfig.showConsoleView == showConsoleView
        runConfig.showExecutionView == showExecutionView
        runConfig.buildConfiguration.rootProjectDirectory == rootDir
        runConfig.buildConfiguration.gradleDistribution == distribution
        runConfig.buildConfiguration.overrideWorkspaceSettings == overrideBuildSettings
        runConfig.buildConfiguration.buildScansEnabled == buildScansEnabled
        runConfig.buildConfiguration.offlineMode == offlineMode
        runConfig.buildConfiguration.workspaceConfiguration.gradleUserHome == null
        runConfig.buildConfiguration.workspaceConfiguration.gradleIsOffline == false
        runConfig.buildConfiguration.workspaceConfiguration.buildScansEnabled == false
    }

    private ILaunchConfiguration emptyLaunchConfig() {
        ILaunchManager launchManager = DebugPlugin.default.launchManager
        ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(GradleRunConfigurationDelegate.ID)
        type.newInstance(null, "launch-config-name")
    }
}
