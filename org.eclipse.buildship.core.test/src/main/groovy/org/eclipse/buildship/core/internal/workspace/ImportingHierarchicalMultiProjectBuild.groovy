package org.eclipse.buildship.core.internal.workspace

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.Path
import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.Logger
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature
import org.eclipse.buildship.core.internal.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class ImportingHierarchicalMultiProjectBuild extends ProjectSynchronizationSpecification {

    File rootDir
    File moduleADir

    def setup() {
        importAndWait(createSampleProject())
    }

    def "Subproject folders are marked"() {
        expect:
        def root = findProject("sample")
        def moduleA = root.getFolder("moduleA")
        !moduleA.isDerived()
        CorePlugin.modelPersistence().loadModel(root).subprojectPaths == [new Path('moduleA'), new Path('moduleA/moduleAsub')]
    }

    def "Build folders for current and nested projects are marked as derived"() {
        expect:
        def root = findProject("sample")
        root.getFolder('build').isDerived()
        root.getFolder('moduleA/build').isDerived()
        root.getFolder('moduleA/moduleAsub/build').isDerived()
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
            file 'build.gradle', """
                allprojects {
                    ${jcenterRepositoryBlock}
                    apply plugin: 'java'
                }
            """
            file 'settings.gradle', """
                include 'moduleA'
                include 'moduleA:moduleAsub'
            """
            dir 'build'
            moduleADir = moduleA {
                file 'build.gradle', "apply plugin: 'java'"
                dir 'src/main/java'
                dir 'build'
                dir 'moduleAsub/build'
            }
        }
    }

}
