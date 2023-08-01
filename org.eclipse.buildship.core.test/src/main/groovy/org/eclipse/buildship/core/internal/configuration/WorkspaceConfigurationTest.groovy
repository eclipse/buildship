/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration

import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.GradleDistribution

class WorkspaceConfigurationTest extends WorkspaceSpecification {

    def "Can load workspace configuration"() {
        expect:
        WorkspaceConfiguration configuration = configurationManager.loadWorkspaceConfiguration()
        configuration.gradleDistribution == GradleDistribution.fromBuild()
        configuration.gradleUserHome == null
        configuration.offline == false
        configuration.buildScansEnabled == false
        configuration.autoSync == false
        configuration.arguments == []
        configuration.jvmArguments == []
        configuration.showConsoleView == true
        configuration.showExecutionsView == true
        configuration.experimentalModuleSupportEnabled == false
    }

    def "Can save workpsace configuration"(GradleDistribution distribution, String gradleUserHome, String javaHome, boolean offlineMode, boolean buildScansEnabled, boolean autoSync, List args, List jvmArgs, boolean showConsole, boolean showExecutions, moduleSupportEnabled) {
        setup:
        WorkspaceConfiguration orignalConfiguration = configurationManager.loadWorkspaceConfiguration()

        when:
        File gradleUserHomeDir = dir(gradleUserHome)
        File javaHomeDir = dir(javaHome)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(distribution, gradleUserHomeDir, javaHomeDir, offlineMode, buildScansEnabled, autoSync, args, jvmArgs, showConsole, showExecutions, moduleSupportEnabled))
        WorkspaceConfiguration updatedConfiguration = configurationManager.loadWorkspaceConfiguration()

        then:
        updatedConfiguration.gradleDistribution == distribution
        updatedConfiguration.gradleUserHome == gradleUserHomeDir
        updatedConfiguration.javaHome == javaHomeDir
        updatedConfiguration.offline == offlineMode
        updatedConfiguration.buildScansEnabled == buildScansEnabled
        updatedConfiguration.autoSync == autoSync
        updatedConfiguration.arguments == args
        updatedConfiguration.jvmArguments == jvmArgs
        updatedConfiguration.showConsoleView == showConsole
        updatedConfiguration.showExecutionsView == showExecutions
        updatedConfiguration.experimentalModuleSupportEnabled == moduleSupportEnabled

        cleanup:
        configurationManager.saveWorkspaceConfiguration(orignalConfiguration)

        where:
        distribution                                                                 | gradleUserHome    | javaHome    | offlineMode  | buildScansEnabled | autoSync | args   | jvmArgs | showConsole | showExecutions | moduleSupportEnabled
        GradleDistribution.fromBuild()                                               | 'customUserHome1' | 'javaHome1' |  false       | false             | true     | ['a1'] | ['j1']  | false       | true           | false
        GradleDistribution.forVersion("3.2.1")                                       | 'customUserHome2' | 'javaHome2' |  false       | true              | false    | ['a2'] | ['j2']  | true        | false          | false
        GradleDistribution.forLocalInstallation(new File('/').canonicalFile)         | 'customUserHome3' | 'javaHome3' |  true        | true              | true     | ['a3'] | ['j3']  | false       | true           | true
        GradleDistribution.forRemoteDistribution(new URI('http://example.com/gd'))   | 'customUserHome4' | 'javaHome4' |  true        | false             | false    | ['a4'] | ['j4']  | true        | false          | true
    }
}
