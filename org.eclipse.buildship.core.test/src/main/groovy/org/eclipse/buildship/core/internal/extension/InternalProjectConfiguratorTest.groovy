package org.eclipse.buildship.core.internal.extension

import org.eclipse.core.runtime.IConfigurationElement
import org.eclipse.core.runtime.IContributor

import org.eclipse.buildship.core.ProjectConfigurator
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.internal.workspace.ConfiguratorBuildActions

class InternalProjectConfiguratorTest extends WorkspaceSpecification {

    def "Can handle zero project configurators"() {
        expect:
        internalConfiguratorsFrom().empty
    }

    def "Can handle basic project configurator"() {
        when:
        List<InternalProjectConfigurator> configurators = internalConfiguratorsFrom(configurator())

        then:
        configurators.size() == 1
        configurators[0].contributorPluginId == 'pluginId'
        configurators[0].id == 'id'
    }

    def "Ignores project configurator with undefined ID"() {
        expect:
        internalConfiguratorsFrom(configurator(null)).empty
    }

    def "Omits configurator with duplicate ID"() {
        expect:
        internalConfiguratorsFrom(configurator(), configurator()).size() == 1
    }

    def "Omits uninstantiable configurator" () {
        expect:
        internalConfiguratorsFrom(uninstantiableConfigurator()).empty
    }

    def "Order preserved for independent configurators"() {
        setup:
        ProjectConfiguratorContribution c1 = configurator('c1')
        ProjectConfiguratorContribution c2 = configurator('c2')
        List<InternalProjectConfigurator> configurators = internalConfiguratorsFrom(c1, c2)

        when:
        configurators = configurators.toSorted()

        then:
        assertOrder(configurators, 'c1', 'c2')

        when:
        configurators = configurators.reverse().toSorted()

        then:
        assertOrder(configurators, 'c2', 'c1')
    }

    def "Ordering respects 'runsBefore' dependency"() {
        setup:
        ProjectConfiguratorContribution c1 = configurator('c1')
        ProjectConfiguratorContribution c2 = configurator('c2', ['c1'])
        List<InternalProjectConfigurator> configurators = internalConfiguratorsFrom(c1, c2)

        when:
        configurators = configurators.toSorted()

        then:
        assertOrder(configurators, 'c2', 'c1')
    }

    def "Ordering respects 'runsAfter' dependency"() {
        setup:
        ProjectConfiguratorContribution c1 = configurator('c1', [], ['c2'])
        ProjectConfiguratorContribution c2 = configurator('c2')
        List<InternalProjectConfigurator> configurators = internalConfiguratorsFrom(c1, c2)

        when:
        configurators = configurators.toSorted()

        then:
        assertOrder(configurators, 'c2', 'c1')
    }

    def "Ordering ignores dependency that introduces cycle" () {
        setup:
        ProjectConfiguratorContribution c1 = configurator('c1', ['c2'])
        ProjectConfiguratorContribution c2 = configurator('c2', ['c3'])
        ProjectConfiguratorContribution c3 = configurator('c3', ['c1'])
        List<InternalProjectConfigurator> configurators = internalConfiguratorsFrom(c3, c2, c1)

        when:
        configurators = configurators.toSorted()

        then:
        assertOrder(configurators, 'c2', 'c3', 'c1')
    }

    private List<InternalProjectConfigurator> internalConfiguratorsFrom(ProjectConfiguratorContribution... contributions) {
        InternalProjectConfigurator.from(contributions as List, new ConfiguratorBuildActions([:]))
    }

    private void assertOrder(List<InternalProjectConfigurator> configurators, String... ids) {
        assert configurators.collect { it.id } == ids
    }

    private ProjectConfiguratorContribution configurator(id = 'id', runsBefore = [], runsAfter = []) {
        IConfigurationElement extension = Mock(IConfigurationElement)
        extension.createExecutableExtension('class') >> Mock(ProjectConfigurator)
        extension.getAttribute('id') >> id
        extension.getAttribute('runsBefore') >> runsBefore.join(',')
        extension.getAttribute('runsAfter') >> runsAfter.join(',')
        IContributor contributor = Mock(IContributor)
        contributor.getName() >> 'pluginId'
        extension.getContributor() >> contributor
        ProjectConfiguratorContribution.from(extension)
    }

    private ProjectConfiguratorContribution uninstantiableConfigurator() {
        IConfigurationElement extension = Mock(IConfigurationElement)
        extension.createExecutableExtension('class') >> { throw new Exception() }
        extension.getAttribute('id') >> 'id'
        IContributor contributor = Mock(IContributor)
        contributor.getName() >> 'pluginId'
        extension.getContributor() >> contributor
        ProjectConfiguratorContribution.from(extension)
    }
}
