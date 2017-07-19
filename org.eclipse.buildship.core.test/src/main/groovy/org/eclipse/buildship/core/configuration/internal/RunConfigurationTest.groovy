package org.eclipse.buildship.core.configuration.internal

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationType
import org.eclipse.debug.core.ILaunchManager

import org.eclipse.buildship.core.configuration.BuildConfiguration
import org.eclipse.buildship.core.configuration.RunConfiguration
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes
import org.eclipse.buildship.core.launch.GradleRunConfigurationDelegate
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification

class RunConfigurationTest extends ProjectSynchronizationSpecification {

    def "create run configuration"(buildBuildScansEnabled, buildOfflineMode, runConfigOverride, runConfigBuildScansEnabled, runConfigOfflineMode, expectedRunConfigBuildScansEnabled, expectedRunConfigOfflineMode) {
        setup:
        List<String> tasks = ['compileGroovy', 'publish']
        File javaHome = dir('java-home')
        GradleDistribution gradleDistribution = GradleDistribution.forVersion('3.2')
        List arguments = ['-q', '-Pkey=value']
        List jvmArguments = ['-ea', '-Dkey=value']
        boolean showConsoleView = false
        boolean showExecutionView = false
        File rootDir = dir('projectDir').canonicalFile
        File buildGradleUserHome = dir('build-gradle-user-home').canonicalFile
        File runGradleUserHome = dir('run-gradle-user-home').canonicalFile

        when:
        BuildConfiguration buildConfig = createOverridingBuildConfiguration(rootDir,
            GradleDistribution.fromBuild(),
            buildBuildScansEnabled,
            buildOfflineMode,
            buildGradleUserHome)
        RunConfiguration runConfig = configurationManager.createRunConfiguration(buildConfig,
                tasks,
                javaHome,
                jvmArguments,
                arguments,
                showConsoleView,
                showExecutionView,
                runConfigOverride,
                gradleDistribution,
                runGradleUserHome,
                runConfigBuildScansEnabled,
                runConfigOfflineMode)

        then:
        runConfig.tasks == tasks
        runConfig.javaHome == javaHome
        runConfig.gradleDistribution == (runConfigOverride ? GradleDistribution.forVersion('3.2') : GradleDistribution.fromBuild())
        runConfig.gradleUserHome == (runConfigOverride ? runGradleUserHome : buildGradleUserHome)
        runConfig.arguments == arguments
        runConfig.jvmArguments == jvmArguments
        runConfig.showConsoleView == showConsoleView
        runConfig.showExecutionView == showExecutionView
        runConfig.buildScansEnabled == expectedRunConfigBuildScansEnabled
        runConfig.offlineMode == expectedRunConfigOfflineMode
        runConfig.projectConfiguration.projectDir == rootDir
        runConfig.projectConfiguration.buildConfiguration.rootProjectDirectory == rootDir
        runConfig.projectConfiguration.buildConfiguration.gradleDistribution == GradleDistribution.fromBuild()
        runConfig.projectConfiguration.buildConfiguration.gradleUserHome == buildGradleUserHome
        runConfig.projectConfiguration.buildConfiguration.overrideWorkspaceSettings == true
        runConfig.projectConfiguration.buildConfiguration.buildScansEnabled == buildBuildScansEnabled
        runConfig.projectConfiguration.buildConfiguration.offlineMode == buildOfflineMode
        runConfig.projectConfiguration.buildConfiguration.workspaceConfiguration.gradleUserHome == null
        runConfig.projectConfiguration.buildConfiguration.workspaceConfiguration.gradleIsOffline == false
        runConfig.projectConfiguration.buildConfiguration.workspaceConfiguration.buildScansEnabled == false

        where:
        buildBuildScansEnabled | buildOfflineMode | runConfigOverride | runConfigBuildScansEnabled | runConfigOfflineMode | expectedRunConfigBuildScansEnabled | expectedRunConfigOfflineMode
        false                  | true             | false             | false                      | false                | false                              | true
        true                   | false            | false             | false                      | false                | true                               | false
        false                  | false            | true              | false                      | true                 | false                              | true
        true                   | false            | true              | true                       | true                 | true                               | true
    }

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
        runConfig.projectConfiguration.projectDir.path == new File('').canonicalPath
        runConfig.projectConfiguration.buildConfiguration.rootProjectDirectory.path == new File('').canonicalPath
        runConfig.projectConfiguration.buildConfiguration.gradleDistribution == GradleDistribution.fromBuild()
        runConfig.projectConfiguration.buildConfiguration.overrideWorkspaceSettings == false
        runConfig.projectConfiguration.buildConfiguration.buildScansEnabled == false
        runConfig.projectConfiguration.buildConfiguration.offlineMode == false
        runConfig.projectConfiguration.buildConfiguration.workspaceConfiguration.gradleUserHome == null
        runConfig.projectConfiguration.buildConfiguration.workspaceConfiguration.gradleIsOffline == false
        runConfig.projectConfiguration.buildConfiguration.workspaceConfiguration.buildScansEnabled == false
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
        runConfig.arguments == arguments
        runConfig.jvmArguments == jvmArguments
        runConfig.showConsoleView == showConsoleView
        runConfig.showExecutionView == showExecutionView
        runConfig.projectConfiguration.projectDir == rootDir
        runConfig.projectConfiguration.buildConfiguration.rootProjectDirectory == rootDir
        runConfig.projectConfiguration.buildConfiguration.gradleDistribution == distribution
        runConfig.projectConfiguration.buildConfiguration.overrideWorkspaceSettings == overrideBuildSettings
        runConfig.projectConfiguration.buildConfiguration.buildScansEnabled == buildScansEnabled
        runConfig.projectConfiguration.buildConfiguration.offlineMode == offlineMode
        runConfig.projectConfiguration.buildConfiguration.workspaceConfiguration.gradleUserHome == null
        runConfig.projectConfiguration.buildConfiguration.workspaceConfiguration.gradleIsOffline == false
        runConfig.projectConfiguration.buildConfiguration.workspaceConfiguration.buildScansEnabled == false
    }

    private ILaunchConfiguration emptyLaunchConfig() {
        ILaunchManager launchManager = DebugPlugin.default.launchManager
        ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(GradleRunConfigurationDelegate.ID)
        type.newInstance(null, "launch-config-name")
    }
}
