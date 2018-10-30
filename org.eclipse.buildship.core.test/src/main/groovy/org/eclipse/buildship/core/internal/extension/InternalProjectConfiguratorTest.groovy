package org.eclipse.buildship.core.internal.extension

import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IConfigurationElement
import org.eclipse.core.runtime.IContributor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status

import org.eclipse.buildship.core.ProjectConfigurator
import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.Logger
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.invocation.InvocationCustomizer

class InternalProjectConfiguratorTest extends WorkspaceSpecification {

    def "Can handle zero project configurators"() {
        expect:
        InternalProjectConfigurator.from([]).empty
    }

    def "Can handle basic project configurator"() {
        when:
        List<InternalProjectConfigurator> configurators = InternalProjectConfigurator.from([configurator()])

        then:
        configurators.size() == 1
        configurators[0].contributorPluginId == 'pluginId'
        configurators[0].fullyQualifiedId == 'pluginId.id'
    }

    def "Ignores project configurator with undefined ID"() {
        expect:
        InternalProjectConfigurator.from([configurator("pluginId", null)]).empty
    }

    def "Omits configurator with duplicate ID"() {
        expect:
        InternalProjectConfigurator.from([configurator(), configurator()]).size() == 1
    }

    def "Omits uninstantiable configurator" () {
        expect:
        InternalProjectConfigurator.from([uninstantiableConfigurator()]).empty
    }

    def "Can load project configurator from different plugin with duplicate ID"() {
        when:
        List<InternalProjectConfigurator> configurators = InternalProjectConfigurator.from([configurator('firstPlugin', 'sameId'), configurator('secondPlugin', 'sameId')])

        then:
        configurators.size() == 2
        configurators[0].fullyQualifiedId == 'firstPlugin.sameId'
        configurators[1].fullyQualifiedId == 'secondPlugin.sameId'
    }

    def "Order preserved for independent configurators"() {
        setup:
        ProjectConfiguratorContribution c1 = configurator('p1', 'c1')
        ProjectConfiguratorContribution c2 = configurator('p2', 'c2')
        List<InternalProjectConfigurator> configurators = InternalProjectConfigurator.from([c1, c2])

        when:
        configurators = configurators.toSorted()

        then:
        assertOrder(configurators, 'p1.c1', 'p2.c2')

        when:
        configurators = configurators.reverse().toSorted()

        then:
        assertOrder(configurators, 'p2.c2', 'p1.c1')
    }

    def "Ordering respects 'runsBefore' dependency"() {
        setup:
        ProjectConfiguratorContribution c1 = configurator('p1', 'c1')
        ProjectConfiguratorContribution c2 = configurator('p2', 'c2', ['p1.c1'])
        List<InternalProjectConfigurator> configurators = InternalProjectConfigurator.from([c1, c2])

        when:
        configurators = configurators.toSorted()

        then:
        assertOrder(configurators, 'p2.c2', 'p1.c1')
    }

    def "Ordering respects 'runsAfter' dependency"() {
        setup:
        ProjectConfiguratorContribution c1 = configurator('p1', 'c1', [], ['p2.c2'])
        ProjectConfiguratorContribution c2 = configurator('p2', 'c2')
        List<InternalProjectConfigurator> configurators = InternalProjectConfigurator.from([c1, c2])

        when:
        configurators = configurators.toSorted()

        then:
        assertOrder(configurators, 'p2.c2', 'p1.c1')
    }

    def "Ordering ignores dependency that introduces cycle" () {
        setup:
        ProjectConfiguratorContribution c1 = configurator('p1', 'c1', ['p2.c2'])
        ProjectConfiguratorContribution c2 = configurator('p2', 'c2', ['p3.c3'])
        ProjectConfiguratorContribution c3 = configurator('p3', 'c3', ['p1.c1'])
        List<InternalProjectConfigurator> configurators = InternalProjectConfigurator.from([c3, c2, c1])

        when:
        configurators = configurators.toSorted()

        then:
        assertOrder(configurators, 'p2.c2', 'p3.c3', 'p1.c1')
    }

    private void assertOrder(List<InternalProjectConfigurator> configurators, String... ids) {
        assert configurators.collect { it.fullyQualifiedId } == ids
    }

    private ProjectConfiguratorContribution configurator(pluginId = 'pluginId', id = 'id', runsBefore = [], runsAfter = []) {
        IConfigurationElement extension = Mock(IConfigurationElement)
        extension.createExecutableExtension('class') >> Mock(ProjectConfigurator)
        extension.getAttribute('id') >> id
        extension.getAttribute('runsBefore') >> runsBefore.join(',')
        extension.getAttribute('runsAfter') >> runsAfter.join(',')
        IContributor contributor = Mock(IContributor)
        contributor.getName() >> pluginId
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
