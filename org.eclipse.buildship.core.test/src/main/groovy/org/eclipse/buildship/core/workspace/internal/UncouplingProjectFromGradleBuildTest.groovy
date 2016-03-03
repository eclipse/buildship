package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.resources.IProject

import org.eclipse.buildship.core.configuration.GradleProjectNature

class UncouplingProjectFromGradleBuildTest extends ProjectSynchronizationSpecification {

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

    def "Uncoupling a project removes the resource filters"() {
        setup:
        def projectDir = dir('sample-project') {
            dir 'subproject-a'
            dir 'subproject-b'
            file 'settings.gradle', "include 'subproject-a', 'subproject-b'"
        }
        synchronizeAndWait(projectDir)

        expect:
        IProject project = findProject('subproject-a')
        project.filters.length == 2
        project.filters[0].fileInfoMatcherDescription.arguments.contains("build")
        project.filters[1].fileInfoMatcherDescription.arguments.contains("gradle")

        when:
        file ('sample-project/settings.gradle').text = "include 'subproject-b'"
        synchronizeAndWait(projectDir)

        then:
        project == findProject('subproject-a')
        project.filters.length == 0
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
        new File(project.location.toFile(), '.settings/gradle.prefs').exists()

        when:
        file ('sample-project/settings.gradle').text = "include 'subproject-b'"
        synchronizeAndWait(projectDir)

        then:
        project == findProject('subproject-a')
        !new File(project.location.toFile(), '.settings/gradle.prefs').exists()
    }
}
