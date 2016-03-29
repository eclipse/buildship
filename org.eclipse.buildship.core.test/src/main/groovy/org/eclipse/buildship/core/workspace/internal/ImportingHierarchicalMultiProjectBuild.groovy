package org.eclipse.buildship.core.workspace.internal

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.Logger;
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification;

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.JavaCore

class ImportingHierarchicalMultiProjectBuild extends ProjectSynchronizationSpecification {

    File sampleDir
    File moduleADir
    File moduleBDir

    void setup() {
        importAndWait(createSampleProject())
    }

    def "Subproject folders are marked as derived"() {
        expect:
        def root = findProject("sample")
        root.getFolder("moduleA").isDerived()
        root.getFolder("moduleB").isDerived()
    }

    def "Removing a subproject removes the derived marker"() {
        setup:
        fileTree(sampleDir) {
            file('settings.gradle').text = """
                include 'moduleA'
            """
        }

        when:
        synchronizeAndWait(sampleDir)

        then:
        def root = findProject("sample")
        !root.getFolder("moduleB").isDerived()
    }

    def "If a new project is added to the Gradle build, it is imported into the workspace"() {
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
        GradleProjectNature.isPresentOn(project)
    }

    def "An existing workspace project is transformed to a Gradle project when included in a Gradle build"() {
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
        GradleProjectNature.isPresentOn(project)
    }

    def "Nonexisting sub projects are ignored"() {
        setup:
        fileTree(sampleDir).file('settings.gradle').text = """
           include 'moduleA'
           include 'moduleB'
           include 'moduleC'
        """
        def logger = Mock(Logger)
        environment.registerService(Logger, logger)

        when:
        synchronizeAndWait(findProject('sample'))

        then:
        0 * logger.error(_)
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
