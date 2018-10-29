package org.eclipse.buildship.core

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.extension.ExtensionManager
import org.eclipse.buildship.core.internal.extension.ProjectConfiguratorContribution
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

abstract class BaseProjectConfiguratorTest extends ProjectSynchronizationSpecification {

    def setup() {
        CorePlugin.instance.extensionManager = new TestExtensionManager(CorePlugin.instance.extensionManager)
    }

    def cleanup() {
        CorePlugin.instance.extensionManager = CorePlugin.instance.extensionManager.delegate
    }

    protected def registerConfigurator(ProjectConfigurator configurator) {
        ExtensionManager manager = CorePlugin.instance.extensionManager
        int id = manager.configurators.size() + 1
        manager.configurators += new ProjectConfiguratorContribution(configurator, "${id}", "custom.configurator.plugin.id")
        configurator
    }

    static class TestExtensionManager {
        @Delegate ExtensionManager delegate
        List<ProjectConfiguratorContribution> configurators = []

        TestExtensionManager(ExtensionManager delegate) {
            this.delegate = delegate
        }

        List<ProjectConfiguratorContribution> loadConfigurators() {
            configurators
        }
    }
}
