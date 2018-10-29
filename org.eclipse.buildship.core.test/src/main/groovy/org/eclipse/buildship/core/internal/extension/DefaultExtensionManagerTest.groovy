package org.eclipse.buildship.core.internal.extension

import org.eclipse.core.runtime.IConfigurationElement
import org.eclipse.core.runtime.IContributor

import org.eclipse.buildship.core.ProjectConfigurator
import org.eclipse.buildship.core.internal.Logger
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.invocation.InvocationCustomizer

class DefaultExtensionManagerTest extends WorkspaceSpecification {

    DelegatingExtensionManager manager
    Logger logger

    def setup() {
        manager = new DelegatingExtensionManager()
        logger = Mock(Logger)
        registerService(Logger, logger)
    }

    def "Can load invocation customizer"() {
        setup:
        manager.extensions = [newInvocationCustomizerExtension()]

        expect:
        manager.loadCustomizers().size() == 1
    }

    def "Can load project configurator"() {
        setup:
        manager.extensions = [newProjectConfigurationExtension()]

        expect:
        manager.loadConfigurators().size() == 1
    }

    def "Does not load project configurator with undefined ID"() {
        setup:
        manager.extensions = [newProjectConfigurationExtension(null)]

        when:
        List<ProjectConfiguratorContribution> configurators = manager.loadConfigurators()

        then:
        configurators.empty
        1 * logger.warn(_,_)
    }

    def "Does not load project configurator from same plugin with duplicate ID"() {
        setup:
        manager.extensions = [newProjectConfigurationExtension('same'), newProjectConfigurationExtension('same')]

        when:
        List<ProjectConfiguratorContribution> configurators = manager.loadConfigurators()

        then:
        configurators.size() == 1
        1 * logger.warn(_,_)
    }

    def "Can load project configurator from different plugin with duplicate ID"() {
        setup:
        manager.extensions = [newProjectConfigurationExtension('same', 'first.plugin'), newProjectConfigurationExtension('same', 'second.plugin')]

        expect:
        manager.loadConfigurators().size() == 2
    }

    def newInvocationCustomizerExtension() {
        IConfigurationElement extension = Mock(IConfigurationElement)
        extension.createExecutableExtension('class') >> Mock(InvocationCustomizer)
        extension
    }

    def newProjectConfigurationExtension(String id = 'configuratorid', String pluginId = 'pluginid') {
        IConfigurationElement extension = Mock(IConfigurationElement)
        extension.createExecutableExtension('class') >> Mock(ProjectConfigurator)
        extension.getAttribute('id') >> id
        IContributor contributor = Mock(IContributor)
        contributor.getName() >> pluginId
        extension.getContributor() >> contributor
        extension
    }

    static class DelegatingExtensionManager extends DefaultExtensionManager {

        List<IConfigurationElement> extensions

        @Override
        Collection<IConfigurationElement> loadElements(String extensionPointName) {
            extensions
        }
    }
}
