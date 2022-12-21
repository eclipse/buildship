/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
        configurators[0].id == 'id'
    }

    def "Ignores project configurator with undefined ID"() {
        expect:
        InternalProjectConfigurator.from([configurator(null)]).empty
    }

    def "Omits configurator with duplicate ID"() {
        expect:
        InternalProjectConfigurator.from([configurator(), configurator()]).size() == 1
    }

    def "Omits uninstantiable configurator" () {
        expect:
        InternalProjectConfigurator.from([uninstantiableConfigurator()]).empty
    }

    def "Order preserved for independent configurators"() {
        setup:
        ProjectConfiguratorContribution c1 = configurator('c1')
        ProjectConfiguratorContribution c2 = configurator('c2')

        when:
        List<InternalProjectConfigurator> configurators = InternalProjectConfigurator.from([c1, c2])

        then:
        assertOrder(configurators, 'c1', 'c2')
    }

    def "Ordering respects 'runsBefore' dependency"() {
        setup:
        ProjectConfiguratorContribution c1 = configurator('c1')
        ProjectConfiguratorContribution c2 = configurator('c2', ['c1'])

        when:
        List<InternalProjectConfigurator> configurators = InternalProjectConfigurator.from([c1, c2])

        then:
        assertOrder(configurators, 'c2', 'c1')
    }

    def "Ordering respects 'runsAfter' dependency"() {
        setup:
        ProjectConfiguratorContribution c1 = configurator('c1', [], ['c2'])
        ProjectConfiguratorContribution c2 = configurator('c2')

        when:
        List<InternalProjectConfigurator> configurators = InternalProjectConfigurator.from([c1, c2])

        then:
        assertOrder(configurators, 'c2', 'c1')
    }

    def "Ordering ignores dependency that introduces cycle" () {
        setup:
        ProjectConfiguratorContribution c1 = configurator('c1', ['c2'])
        ProjectConfiguratorContribution c2 = configurator('c2', ['c3'])
        ProjectConfiguratorContribution c3 = configurator('c3', ['c1'])

        when:
        List<InternalProjectConfigurator> configurators = InternalProjectConfigurator.from([c3, c2, c1])

        then:
        assertOrder(configurators, 'c2', 'c3', 'c1')
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
