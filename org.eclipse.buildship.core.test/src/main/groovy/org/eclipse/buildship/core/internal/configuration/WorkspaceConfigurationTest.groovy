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

    }
    def "Can save workpsace configuration"(GradleDistribution distribution, String gradleUserHome, String javaHome, boolean offlineMode, boolean buildScansEnabled, boolean autoSync) {
        setup:
        WorkspaceConfiguration orignalConfiguration = configurationManager.loadWorkspaceConfiguration()

        when:
        File gradleUserHomeDir = dir(gradleUserHome)
        File javaHomeDir = dir(javaHome)
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(distribution, gradleUserHomeDir, javaHomeDir, offlineMode, buildScansEnabled, autoSync))
        WorkspaceConfiguration updatedConfiguration = configurationManager.loadWorkspaceConfiguration()

        then:
        updatedConfiguration.gradleDistribution == distribution
        updatedConfiguration.gradleUserHome == gradleUserHomeDir
        updatedConfiguration.javaHome == javaHomeDir
        updatedConfiguration.offline == offlineMode
        updatedConfiguration.buildScansEnabled == buildScansEnabled
        updatedConfiguration.autoSync == autoSync

        cleanup:
        configurationManager.saveWorkspaceConfiguration(orignalConfiguration)

        where:
        distribution                                                                 | gradleUserHome    | javaHome    | offlineMode  | buildScansEnabled | autoSync
        GradleDistribution.fromBuild()                                               | 'customUserHome1' | 'javaHome1' |  false       | false             | true
        GradleDistribution.forVersion("3.2.1")                                       | 'customUserHome2' | 'javaHome2' |  false       | true              | false
        GradleDistribution.forLocalInstallation(new File('/').canonicalFile)         | 'customUserHome3' | 'javaHome3' |  true        | true              | true
        GradleDistribution.forRemoteDistribution(new URI('http://example.com/gd'))   | 'customUserHome4' | 'javaHome4' |  true        | false             | false
    }
}
