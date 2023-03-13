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

import org.eclipse.core.runtime.Platform
import org.eclipse.core.variables.IStringVariableManager
import org.eclipse.core.variables.IValueVariable
import org.eclipse.core.variables.VariablesPlugin
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationType
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.debug.core.ILaunchManager

import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException
import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationAttributes
import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationDelegate
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class RunConfigurationTest extends ProjectSynchronizationSpecification {

    def "load default settings"() {
        given:
        ILaunchConfigurationWorkingCopy launchConfig = createGradleLaunchConfig()
        // when executed from the IDE, the working directory is set to the core.test plugin folder
        // which makes this test read the run configuration from the `.settings` folder
        boolean isDev = Platform.inDevelopmentMode()
        File tmpDir
        if (isDev) {
            tmpDir = dir("tmpDir")
            launchConfig.setAttribute(GradleRunConfigurationAttributes.WORKING_DIR, tmpDir.absolutePath)
            launchConfig.doSave()
        }

        RunConfiguration runConfig = configurationManager.loadRunConfiguration(launchConfig)

        expect:
        runConfig.tasks == []
        runConfig.javaHome == null
        runConfig.arguments == []
        runConfig.jvmArguments == []
        runConfig.showExecutionView == true
        runConfig.showConsoleView == true
        runConfig.projectConfiguration.projectDir.path == (isDev ? tmpDir.canonicalPath : new File('').canonicalPath)
        runConfig.projectConfiguration.buildConfiguration.rootProjectDirectory.path == (isDev ? tmpDir.canonicalPath : new File('').canonicalPath)
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

        ILaunchConfiguration launchConfig = createGradleLaunchConfig()
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

    @Issue('https://github.com/eclipse/buildship/issues/572')
    def "load attributes from valid expressions"() {
        setup:
        IStringVariableManager variableManager = VariablesPlugin.default.stringVariableManager
        IValueVariable[] variables = [
            variableManager.newValueVariable('buildship_test_var1', 'Variable to test run config substitution', true, 'test_value1'),
            variableManager.newValueVariable('buildship_test_var2', 'Variable to test run config substitution', true, 'test_value2'),
            variableManager.newValueVariable('buildship_test_var3', 'Variable to test run config substitution', true, 'test_value3'),
            variableManager.newValueVariable('buildship_test_var4', 'Variable to test run config substitution', true, 'test_value4')
        ]
        variableManager.addVariables(variables)

        File projectDir = dir('sample-project') {
            file 'settings.gradle', ''
        }
        projectDir = projectDir.canonicalFile
        importAndWait(projectDir)

        ILaunchConfiguration launchConfig = emptyLaunchConfig()
        GradleRunConfigurationAttributes.applyOverrideBuildSettings(true, launchConfig)
        GradleRunConfigurationAttributes.applyWorkingDirExpression('${workspace_loc:/sample-project}', launchConfig)
        GradleRunConfigurationAttributes.applyGradleUserHomeExpression('/gradleUserHome/${buildship_test_var1}', launchConfig)
        GradleRunConfigurationAttributes.applyJavaHomeExpression('/javaHome/${buildship_test_var2}', launchConfig)
        GradleRunConfigurationAttributes.applyArgumentExpressions(['-PsampleProjectProperty=${buildship_test_var3}'], launchConfig)
        GradleRunConfigurationAttributes.applyJvmArgumentExpressions(['-DsampleJvmProperty=${buildship_test_var4}'], launchConfig)

        when:
        RunConfiguration runConfig = configurationManager.loadRunConfiguration(launchConfig)

        then:
        runConfig.projectConfiguration.projectDir == projectDir
        runConfig.gradleUserHome.path.contains 'test_value1'
        runConfig.javaHome.path.contains 'test_value2'
        runConfig.arguments == ['-PsampleProjectProperty=test_value3']
        runConfig.jvmArguments == ['-DsampleJvmProperty=test_value4']

        cleanup:
        variableManager.removeVariables(variables)
    }

    @Issue('https://github.com/eclipse/buildship/issues/572')
    def "load attributes from invaild expressions"() {
        setup:
        File projectDir = dir('sample-project') {
            file 'settings.gradle', ''
        }
        projectDir = projectDir.canonicalFile
        importAndWait(projectDir)

        ILaunchConfiguration launchConfig = emptyLaunchConfig()
        GradleRunConfigurationAttributes.applyOverrideBuildSettings(true, launchConfig)
        GradleRunConfigurationAttributes.applyWorkingDirExpression('${nonexisting}', launchConfig)

        when:
        configurationManager.loadRunConfiguration(launchConfig)

        then:
        thrown GradlePluginsRuntimeException

        when:
        launchConfig = emptyLaunchConfig()
        GradleRunConfigurationAttributes.applyWorkingDirExpression('${workspace_loc:/sample-project}', launchConfig)
        GradleRunConfigurationAttributes.applyGradleUserHomeExpression('${nonexisting}', launchConfig)
        configurationManager.loadRunConfiguration(launchConfig)

        then:
        thrown GradlePluginsRuntimeException

        when:
        launchConfig = emptyLaunchConfig()
        GradleRunConfigurationAttributes.applyWorkingDirExpression('${workspace_loc:/sample-project}', launchConfig)
        GradleRunConfigurationAttributes.applyJavaHomeExpression('${nonexisting}', launchConfig)
        configurationManager.loadRunConfiguration(launchConfig)

        then:
        thrown GradlePluginsRuntimeException

        when:
        launchConfig = emptyLaunchConfig()
        GradleRunConfigurationAttributes.applyWorkingDirExpression('${workspace_loc:/sample-project}', launchConfig)
        GradleRunConfigurationAttributes.applyArgumentExpressions(['${nonexisting}'], launchConfig)
        configurationManager.loadRunConfiguration(launchConfig)

        then:
        thrown GradlePluginsRuntimeException

        when:
        launchConfig = emptyLaunchConfig()
        GradleRunConfigurationAttributes.applyWorkingDirExpression('${workspace_loc:/sample-project}', launchConfig)
        GradleRunConfigurationAttributes.applyJvmArgumentExpressions(['${nonexisting}'], launchConfig)
        configurationManager.loadRunConfiguration(launchConfig)

        then:
        thrown GradlePluginsRuntimeException
    }

    private ILaunchConfiguration emptyLaunchConfig() {
        ILaunchManager launchManager = DebugPlugin.default.launchManager
        ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(GradleRunConfigurationDelegate.ID)
        type.newInstance(null, launchManager.generateLaunchConfigurationName('launch-config-name'))
    }
}
