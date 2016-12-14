package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.ProjectScope
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.configuration.internal.DefaultProjectConfigurationPersistence;
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification

class UncouplingProjectFromGradleBuild extends ProjectSynchronizationSpecification {

    def "Uncoupling a project removes the Gradle nature"() {
        setup:
        def projectDir = dir('sample-project') {
            dir 'subproject-a'
            dir 'subproject-b'
            file 'settings.gradle', "include 'subproject-a', 'subproject-b'"
        }
        synchronizeAndWait(projectDir)

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
        synchronizeAndWait(projectDir)

        expect:
        IProject project = findProject('subproject-a')
        IEclipsePreferences node = new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID)
        node.get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, null)

        when:
        file ('sample-project/settings.gradle').text = "include 'subproject-b'"
        synchronizeAndWait(projectDir)

        then:
        !node.get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, null)
    }

    // TODO (donat) uncoupling a project removes persistent model
}
