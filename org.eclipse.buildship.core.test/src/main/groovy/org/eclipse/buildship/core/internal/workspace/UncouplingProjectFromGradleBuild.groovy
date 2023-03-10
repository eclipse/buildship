/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.ProjectScope
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.BuildConfigurationPersistence
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class UncouplingProjectFromGradleBuild extends ProjectSynchronizationSpecification {

    def "Uncoupling a project removes the Gradle nature"() {
        setup:
        def projectDir = dir('sample-project') {
            dir 'subproject-a'
            dir 'subproject-b'
            file 'settings.gradle', "include 'subproject-a', 'subproject-b'"
        }
        importAndWait(projectDir)

        expect:
        findProject('subproject-a').hasNature(GradleProjectNature.ID)

        when:
        file ('sample-project/settings.gradle').text = "include 'subproject-b'"
        synchronizeAndWait(projectDir)

        then:
        !findProject('subproject-a').hasNature(GradleProjectNature.ID)
    }

    def "Uncoupling a project removes the settings file"() {
        setup:
        def projectDir = dir('sample-project') {
            dir 'subproject-a'
            dir 'subproject-b'
            file 'settings.gradle', "include 'subproject-a', 'subproject-b'"
        }
        importAndWait(projectDir)

        expect:
        IProject project = findProject('subproject-a')
        IEclipsePreferences node = new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID)
        node.get(BuildConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, null)

        when:
        file ('sample-project/settings.gradle').text = "include 'subproject-b'"
        synchronizeAndWait(projectDir)

        then:
        !node.get(BuildConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, null)
    }

    def "Uncoupling a project removes persistent model"() {
        setup:
        def projectDir = dir('sample-root') {
            dir 'sample-sub'
            file 'build.gradle', "apply plugin: 'java'"
            file 'settings.gradle', "include 'sample-sub'"
        }
        importAndWait(projectDir)

        expect:
        CorePlugin.modelPersistence().loadModel(findProject('sample-root')).present
        CorePlugin.modelPersistence().loadModel(findProject('sample-sub')).present

        when:
        file ('sample-root/settings.gradle').text = ''
        synchronizeAndWait(projectDir)

        then:
        CorePlugin.modelPersistence().loadModel(findProject('sample-root')).present
        !CorePlugin.modelPersistence().loadModel(findProject('sample-sub')).present
    }
}
