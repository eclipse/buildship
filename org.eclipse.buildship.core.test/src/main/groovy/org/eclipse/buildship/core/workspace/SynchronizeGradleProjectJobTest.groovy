package org.eclipse.buildship.core.workspace

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.test.fixtures.EclipseProjects;
import org.eclipse.buildship.core.workspace.internal.ProjectSynchronizationSpecification;

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.JavaCore

class SynchronizeGradleProjectJobTest extends ProjectSynchronizationSpecification {

    File sampleDir
    File moduleADir
    File moduleBDir

    def setup() {
        importAndWait(createSampleProject())
    }

    def "Updates the dependency list" () {
        setup:
        fileTree(moduleADir).file('build.gradle').text = """
            apply plugin: 'java'
            dependencies { testCompile 'junit:junit:4.12' }
        """

        when:
        synchronizeAndWait(findProject('moduleA'))

        then:
        JavaCore.create(findProject('moduleA')).resolvedClasspath.find{ it.path.toPortableString().endsWith('junit-4.12.jar') }
    }

    def "Gradle nature and Gradle settings file are discarded when the project is excluded from a Gradle build"() {
        setup:
        fileTree(sampleDir).file('settings.gradle').text = "include 'moduleA'"

        when:
        synchronizeAndWait(findProject('moduleB'))

        then:
        IProject project = findProject('moduleB')
        project != null
        !GradleProjectNature.INSTANCE.isPresentOn(project)
        project.getFolder('.settings').exists()
        !project.getFolder('.settings').getFile('gradle.prefs').exists()
    }

    def "A new Gradle module is imported into the workspace"() {
        setup:
        fileTree(sampleDir) {
            file('settings.gradle').text = """
               include 'moduleA'
               include 'moduleB'
               include 'moduleC'
            """
            moduleC {
                file 'build.gradle', "apply plugin: 'java'"
                dir 'src/main/java'
            }
        }

        when:
        synchronizeAndWait(findProject('moduleB'))

        then:
        IProject project = findProject('moduleC')
        project != null
        GradleProjectNature.INSTANCE.isPresentOn(project)
    }

    def "Project is transformed to a Gradle project when included in a Gradle build"() {
        setup:
        fileTree(sampleDir).file('settings.gradle').text = """
           include 'moduleA'
           include 'moduleB'
           include 'moduleC'
        """
        def project = EclipseProjects.newProject("moduleC", new File(sampleDir, "moduleC"))

        when:
        synchronizeAndWait(findProject('sample'))

        then:
        GradleProjectNature.INSTANCE.isPresentOn(project)
    }

    private File createSampleProject() {
        sampleDir = dir('sample') {
            file 'build.gradle', '''
                allprojects {
                    repositories { mavenCentral() }
                    apply plugin: 'java'
                }
            '''
            file 'settings.gradle', """
                include 'moduleA'
                include 'moduleB'
            """
            moduleADir = moduleA {
                file 'build.gradle', "apply plugin: 'java'"
                dir 'src/main/java'
            }
            moduleBDir = moduleB {
                file 'build.gradle', "apply plugin: 'java'"
                dir 'src/main/java'
            }
        }
    }

}
