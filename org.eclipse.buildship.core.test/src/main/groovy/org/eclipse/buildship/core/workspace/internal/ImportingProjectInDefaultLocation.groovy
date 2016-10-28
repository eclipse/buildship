package org.eclipse.buildship.core.workspace.internal

import spock.lang.Ignore;
import spock.lang.Issue

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IWorkspace
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification

class ImportingProjectInDefaultLocation extends ProjectSynchronizationSpecification {

    def "Can import project located in default location"() {
        when:
        synchronizeAndWait(newSampleProject())

        then:
        workspace.root.projects.length == 1
    }

    @Issue("https://bugs.eclipse.org/bugs/show_bug.cgi?id=472223")
    def "Can import project located in workspace folder and with custom root name"() {
        setup:
        File rootProject = fileTree(newSampleProject()) {
            file 'settings.gradle', "rootProject.name = 'my-project-name-is-different-than-the-folder'"
        }

        when:
        synchronizeAndWait(rootProject)

        then : "The project is imported and stays in the same folder"
        workspace.root.projects.length == 1
        def project = workspace.root.projects[0]
        project.location.toFile() == rootProject
    }

    @Issue("https://bugs.eclipse.org/bugs/show_bug.cgi?id=476921")
    @Ignore
    // TODO FIXME (donat) Buildship should disallow projects located in the default location
    def "Can depend on project located in workspace folder and with custom root name"() {
        setup:
        File rootProject = fileTree(newSampleProject()) {
            file 'settings.gradle', """
                rootProject.name = 'my-project-name-is-different-than-the-folder'
                include 'sub'
            """
            file 'build.gradle', "apply plugin: 'java'"
            dir('sub') {
                file 'build.gradle', """
                    apply plugin: 'java'
                    dependencies {
                        compile rootProject
                    }
                """
            }
        }

        when:
        synchronizeAndWait(rootProject)

        then :
        IProject sub = findProject("sub")
        IJavaProject javaProject = JavaCore.create(sub)
        javaProject.getResolvedClasspath(true).find {
            it.path.toString() == "/sample"
        }
    }

    def File newSampleProject() {
        workspaceDir('sample')
    }

}
