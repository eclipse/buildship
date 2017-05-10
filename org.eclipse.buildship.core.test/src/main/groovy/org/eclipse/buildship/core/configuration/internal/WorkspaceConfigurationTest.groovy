package org.eclipse.buildship.core.configuration.internal

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class WorkspaceConfigurationTest extends WorkspaceSpecification {

    def "Can load workspace configuration"() {
        expect:
        WorkspaceConfiguration configuration = CorePlugin.configurationManager().loadWorkspaceConfiguration()
        configuration.gradleUserHome == null
        configuration.offline == false
        configuration.buildScansEnabled == false
    }

    def "Can save workpsace configuration"(String gradleUserHome, boolean offlineMode, boolean buildScansEnabled) {
        setup:
        WorkspaceConfiguration orignalConfiguration = CorePlugin.configurationManager().loadWorkspaceConfiguration()

        when:
        File gradleUserHomeDir = dir(gradleUserHome)
        CorePlugin.configurationManager().saveWorkspaceConfiguration(new WorkspaceConfiguration(gradleUserHomeDir, offlineMode, buildScansEnabled))
        WorkspaceConfiguration updatedConfiguration = CorePlugin.configurationManager().loadWorkspaceConfiguration()

        then:
        updatedConfiguration.gradleUserHome == gradleUserHomeDir
        updatedConfiguration.offline == offlineMode
        updatedConfiguration.buildScansEnabled == buildScansEnabled

        cleanup:
        CorePlugin.configurationManager().saveWorkspaceConfiguration(orignalConfiguration)

        where:
        gradleUserHome    | offlineMode | buildScansEnabled
        'customUserHome1' |  false       | false
        'customUserHome2' |  false       | true
        'customUserHome3' |  true        | true
        'customUserHome4' |  true        | false
    }
}
