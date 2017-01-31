package org.eclipse.buildship.core.workspace.internal

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.Logger;
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.preferences.ModelPersistence
import org.eclipse.buildship.core.preferences.PersistentModel
import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification;

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.JavaCore

class ImportingHierarchicalMultiProjectBuild extends ProjectSynchronizationSpecification {

    File rootDir
    File moduleADir

    void setup() {
        importAndWait(createSampleProject())
    }

    def "Subproject folders are marked "() {
        expect:
        def root = findProject("sample")
        def moduleA = root.getFolder("moduleA")
        !moduleA.isDerived()
        CorePlugin.modelPersistence().loadModel(root).getSubprojectPaths()
    }

    def "If a new project is added to the Gradle build, it is imported into the workspace"() {
        setup:
        fileTree(rootDir) {
            file('settings.gradle').text = """
               include 'moduleA'
               include 'moduleB'
            """
            moduleB {
                file 'build.gradle', "apply plugin: 'java'"
                dir 'src/main/java'
            }
        }

        when:
        synchronizeAndWait(rootDir)

        then:
        IProject project = findProject('moduleB')
        project != null
        GradleProjectNature.isPresentOn(project)
    }

    def "An existing workspace project is transformed to a Gradle project when included in a Gradle build"() {
        setup:
        fileTree(rootDir).file('settings.gradle').text = """
           include 'moduleA'
           include 'moduleB'
        """
        def project = EclipseProjects.newProject("moduleB", new File(rootDir, "moduleB"))

        when:
        synchronizeAndWait(rootDir)

        then:
        GradleProjectNature.isPresentOn(project)
    }

    def "Nonexisting sub projects are ignored"() {
        setup:
        fileTree(rootDir).file('settings.gradle').text = """
           include 'moduleA'
           include 'moduleB'
        """
        def logger = Mock(Logger)
        environment.registerService(Logger, logger)

        when:
        synchronizeAndWait(rootDir)

        then:
        0 * logger.error(_)
    }

    private File createSampleProject() {
        rootDir = dir('sample') {
            file 'build.gradle', '''
                allprojects {
                    repositories { mavenCentral() }
                    apply plugin: 'java'
                }
            '''
            file 'settings.gradle', """
                include 'moduleA'
            """
            moduleADir = moduleA {
                file 'build.gradle', "apply plugin: 'java'"
                dir 'src/main/java'
            }
        }
    }

}
