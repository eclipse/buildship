/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core

import org.eclipse.core.runtime.IConfigurationElement
import org.eclipse.core.runtime.IContributor

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.extension.ExtensionManager
import org.eclipse.buildship.core.internal.extension.ProjectConfiguratorContribution
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

abstract class BaseProjectConfiguratorTest extends ProjectSynchronizationSpecification {

    int numOfInternalConfigurators

    def setup() {
        ExtensionManager orignalManager = CorePlugin.instance.extensionManager
        CorePlugin.instance.extensionManager = new TestExtensionManager(CorePlugin.instance.extensionManager)
        orignalManager.loadConfigurators()
            .findAll{ c -> c.contributorPluginId.startsWith('org.eclipse.buildship') }
            .each { c -> numOfInternalConfigurators++; CorePlugin.instance.extensionManager.configurators += c }
    }

    def cleanup() {
        CorePlugin.instance.extensionManager = CorePlugin.instance.extensionManager.delegate
    }

    protected def registerConfigurator(ProjectConfigurator configurator) {
        ExtensionManager manager = CorePlugin.instance.extensionManager
        int id = manager.configurators.size() + 1 - numOfInternalConfigurators
        manager.configurators += contribution(id, configurator)
        configurator
    }


    private ProjectConfiguratorContribution contribution(id, configurator) {
        IConfigurationElement extension = Mock(IConfigurationElement)
        extension.createExecutableExtension('class') >> { configurator }
        extension.getAttribute('id') >> "configurator$id"
        IContributor contributor = Mock(IContributor)
        contributor.getName() >> 'pluginId'
        extension.getContributor() >> contributor
        ProjectConfiguratorContribution.from(extension)
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
