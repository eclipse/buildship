package org.eclipse.buildship.core

import spock.lang.Specification;

class CorePluginTest extends Specification {

    def "Services exposed from core plugin are available"() {
        expect:
        CorePlugin.getInstance() != null
        CorePlugin.gradleLaunchConfigurationManager() != null
        CorePlugin.logger() != null
        CorePlugin.modelRepositoryProvider() != null
        CorePlugin.processStreamsProvider() != null
        CorePlugin.projectConfigurationManager() != null
        CorePlugin.publishedGradleVersions() !=null
        CorePlugin.workbenchOperations() != null
        CorePlugin.workspaceOperations() != null
    }

}
