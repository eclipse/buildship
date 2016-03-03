package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.resources.IProject

import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.test.fixtures.GradleModel

class UncouplingProjectFromGradleBuildTest extends ProjectSynchronizationSpecification {

    def "Uncoupling a project removes the Gradle nature"() {
        setup:
        fileTree('sample-project') {
            dir 'subproject-a'
            dir 'subproject-b'
            file 'settings.gradle', "include 'subproject-a', 'subproject-b'"
        }
        GradleModel gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        expect:
        findProject('subproject-a').hasNature(GradleProjectNature.ID)

        when:
        file ('sample-project/settings.gradle').text = "include 'subproject-b'"
        gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        !findProject('subproject-a').hasNature(GradleProjectNature.ID)
    }

    def "Uncoupling a project removes the resource filters"() {
        setup:
        fileTree('sample-project') {
            dir 'subproject-a'
            dir 'subproject-b'
            file 'settings.gradle', "include 'subproject-a', 'subproject-b'"
        }
        GradleModel gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        expect:
        IProject project = findProject('subproject-a')
        project.filters.length == 2
        project.filters[0].fileInfoMatcherDescription.arguments.contains("build")
        project.filters[1].fileInfoMatcherDescription.arguments.contains("gradle")

        when:
        file ('sample-project/settings.gradle').text = "include 'subproject-b'"
        gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        project == findProject('subproject-a')
        project.filters.length == 0
    }

    def "Uncoupling a project removes the settings file"() {
        setup:
        fileTree('sample-project') {
            dir 'subproject-a'
            dir 'subproject-b'
            file 'settings.gradle', "include 'subproject-a', 'subproject-b'"
        }
        GradleModel gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        expect:
        IProject project = findProject('subproject-a')
        new File(project.location.toFile(), '.settings/gradle.prefs').exists()

        when:
        file ('sample-project/settings.gradle').text = "include 'subproject-b'"
        gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        project == findProject('subproject-a')
        !new File(project.location.toFile(), '.settings/gradle.prefs').exists()
    }
}
