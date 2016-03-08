package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.ProjectScope
import org.eclipse.core.runtime.preferences.IEclipsePreferences

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.configuration.internal.DefaultProjectConfigurationPersistence
import org.eclipse.buildship.core.configuration.internal.ProjectConfigurationPersistence
import org.eclipse.buildship.core.configuration.internal.ProjectConfigurationProperties
import org.eclipse.buildship.core.test.fixtures.GradleModel

class UncouplingProjectFromGradleBuildTest extends ProjectSynchronizationSpecification {

    def "Uncoupling a project removes the Gradle nature"() {
        setup:
        fileStructure().create {
            file 'sample-project/subproject-a/build.gradle'
            file 'sample-project/subproject-b/build.gradle'
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle', "include 'subproject-a', 'subproject-b'"
        }
        GradleModel gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        expect:
        findProject('subproject-a').hasNature(GradleProjectNature.ID)

        when:
        fileStructure().create { file 'sample-project/settings.gradle', "'subproject-b'" }
        gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        !findProject('subproject-a').hasNature(GradleProjectNature.ID)
    }

    def "Uncoupling a project removes the resource filters"() {
        setup:
        fileStructure().create {
            file 'sample-project/subproject-a/build.gradle'
            file 'sample-project/subproject-b/build.gradle'
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle', "include 'subproject-a', 'subproject-b'"
        }
        GradleModel gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        expect:
        IProject project = findProject('sample-project')
        project.filters.length == 2

        when:
        fileStructure().create { file 'sample-project/settings.gradle', "include 'subproject-b'" }
        gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        project.filters.length == 1
    }

    def "Uncoupling a project removes project configuration"() {
        setup:
        fileStructure().create {
            file 'sample-project/subproject-a/build.gradle'
            file 'sample-project/subproject-b/build.gradle'
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle', "include 'subproject-a', 'subproject-b'"
        }
        GradleModel gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        expect:
        IProject project = findProject('subproject-a')
        new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID).get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, null)

        when:
        fileStructure().create { file 'sample-project/settings.gradle', "'subproject-b'" }
        gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        project == findProject('subproject-a')
        !new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID).get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, null)
    }
}
