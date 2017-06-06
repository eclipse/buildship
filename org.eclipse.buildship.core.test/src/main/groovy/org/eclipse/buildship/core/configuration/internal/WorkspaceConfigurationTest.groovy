package org.eclipse.buildship.core.configuration.internal

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.buildship.core.configuration.WorkspaceConfiguration
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class WorkspaceConfigurationTest extends WorkspaceSpecification {

    def "Can load workspace configuration"() {
        expect:
        WorkspaceConfiguration configuration = configurationManager.loadWorkspaceConfiguration()
        configuration.gradleDistribution == GradleDistribution.fromBuild()
        configuration.gradleUserHome == null
        configuration.offline == false
        configuration.buildScansEnabled == false

    }
    def "Can save workpsace configuration"(GradleDistribution distribution, String gradleUserHome, boolean offlineMode, boolean buildScansEnabled) {
        setup:
        WorkspaceConfiguration orignalConfiguration = configurationManager.loadWorkspaceConfiguration()

        when:
        File gradleUserHomeDir = dir(gradleUserHome)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(distribution, gradleUserHomeDir, offlineMode, buildScansEnabled))
        WorkspaceConfiguration updatedConfiguration = configurationManager.loadWorkspaceConfiguration()

        then:
        updatedConfiguration.gradleDistribution == distribution
        updatedConfiguration.gradleUserHome == gradleUserHomeDir
        updatedConfiguration.offline == offlineMode
        updatedConfiguration.buildScansEnabled == buildScansEnabled

        cleanup:
        configurationManager.saveWorkspaceConfiguration(orignalConfiguration)

        where:
        distribution                                                                 | gradleUserHome    | offlineMode  | buildScansEnabled
        GradleDistribution.fromBuild()                                               | 'customUserHome1' |  false       | false
        GradleDistribution.forVersion("3.2.1")                                       | 'customUserHome2' |  false       | true
        GradleDistribution.forLocalInstallation(new File('/').canonicalFile)         | 'customUserHome3' |  true        | true
        GradleDistribution.forRemoteDistribution(new URI('http://example.com/gd'))   | 'customUserHome4' |  true        | false
    }
}
