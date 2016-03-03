package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature
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
        IProject project = findProject('subproject-a')
        project.filters.length == 2
        project.filters[0].fileInfoMatcherDescription.arguments.contains("build")
        project.filters[1].fileInfoMatcherDescription.arguments.contains("gradle")

        when:
        fileStructure().create { file 'sample-project/settings.gradle', "'subproject-b'" }
        gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        project == findProject('subproject-a')
        project.filters.length == 0
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
        new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID).get(ProjectConfigurationProperties.CONNECTION_PROJECT_DIR.key, null)

        when:
        fileStructure().create { file 'sample-project/settings.gradle', "'subproject-b'" }
        gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        project == findProject('subproject-a')
        !new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID).get(ProjectConfigurationProperties.CONNECTION_PROJECT_DIR.key, null)
    }
}
