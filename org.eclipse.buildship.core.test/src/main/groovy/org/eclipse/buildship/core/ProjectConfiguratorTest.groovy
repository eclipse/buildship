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

class ProjectConfiguratorTest extends BaseProjectConfiguratorTest {

    ProjectConfigurator configurator

    def setup() {
        configurator = registerConfigurator(Mock(ProjectConfigurator))
    }

    def "Configurator initialized once per synchronization"() {
        setup:
        File location = dir('ProjectConfiguratorTest_1') {
            file 'settings.gradle', ''
        }

        when:
        importAndWait(location)

        then:
        1 * configurator.init(_, _)
    }

    def "configure() called for each project"() {
        setup:
        File location = dir('ProjectConfiguratorTest') {
            file "settings.gradle", """
                rootProject.name = 'root'
                include 'sub'
            """
            dir 'sub'
        }

        when:
        importAndWait(location)

        then:
        1 * configurator.configure({ ProjectContext pc -> pc.project.name == 'root' }, _)
        1 * configurator.configure({ ProjectContext pc -> pc.project.name == 'sub' }, _)
    }

    def "unconfigure() called for each removed project"() {
        setup:
        File settingsFile = null
        File location = dir('ProjectConfiguratorTest') {
            settingsFile = file "settings.gradle", """
                rootProject.name = 'root'
                include 'sub1'
                include 'sub2'
            """
            dir 'sub1'
            dir 'sub2'
        }
        importAndWait(location)
        new File(location, 'settings.gradle').text = "rootProject.name = 'root'"

        when:
        synchronizeAndWait(location)

        then:
        1 * configurator.unconfigure({ ProjectContext pc -> pc.project.name == 'sub1' }, _)
        1 * configurator.unconfigure({ ProjectContext pc -> pc.project.name == 'sub2' }, _)
    }
}
