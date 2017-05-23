package org.eclipse.buildship.core.configuration.internal

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class WorkspaceConfigurationTest extends WorkspaceSpecification {

    def "Can load workspace configuration"() {
        expect:
        WorkspaceConfiguration configuration = CorePlugin.configurationManager().loadWorkspaceConfiguration()
        configuration.gradleDisribution == GradleDistribution.fromBuild()
        configuration.gradleUserHome == null
        configuration.offline == false
        configuration.buildScansEnabled == false
    }

    def "Can save workpsace configuration"(GradleDistribution distribution, String gradleUserHome, boolean offlineMode, boolean buildScansEnabled) {
        setup:
        WorkspaceConfiguration orignalConfiguration = CorePlugin.configurationManager().loadWorkspaceConfiguration()

        when:
        File gradleUserHomeDir = dir(gradleUserHome)
        CorePlugin.configurationManager().saveWorkspaceConfiguration(new WorkspaceConfiguration(distribution, gradleUserHomeDir, offlineMode, buildScansEnabled))
        WorkspaceConfiguration updatedConfiguration = CorePlugin.configurationManager().loadWorkspaceConfiguration()

        then:
        updatedConfiguration.gradleUserHome == gradleUserHomeDir
        updatedConfiguration.offline == offlineMode
        updatedConfiguration.buildScansEnabled == buildScansEnabled

        cleanup:
        CorePlugin.configurationManager().saveWorkspaceConfiguration(orignalConfiguration)

        where:
        distribution                                                                 | gradleUserHome    | offlineMode  | buildScansEnabled
        GradleDistribution.fromBuild()                                               | 'customUserHome1' |  false       | false
        GradleDistribution.forVersion("3.2.1")                                       | 'customUserHome2' |  false       | true
        GradleDistribution.forLocalInstallation(new File('/'))                       | 'customUserHome3' |  true        | true
        GradleDistribution.forRemoteDistribution(new URI('http://example.com/gd'))   | 'customUserHome4' |  true        | false
    }
}
