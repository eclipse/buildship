package org.eclipse.buildship.core.workspace.internal

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification;

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.JavaCore

class ImportingMultiProjectBuild extends ProjectSynchronizationSpecification {

    File sampleDir
    File moduleADir
    File moduleBDir

    def setup() {
        importAndWait(createSampleProject())
    }

    def "Imported parent projects have filters to hide the content of the children"() {
        expect:
        def filters = findProject("sample").getFilters()
        filters.length == 2
        (filters[0].fileInfoMatcherDescription.arguments as String).endsWith('moduleA')
        (filters[1].fileInfoMatcherDescription.arguments as String).endsWith('moduleB')
    }

    def "Importing a project twice won't result in duplicate filters"() {
        setup:
        deleteAllProjects(false)

        when:
        synchronizeAndWait(sampleDir)

        then:
        def filters = findProject("sample").getFilters()
        filters.length == 2
        (filters[0].fileInfoMatcherDescription.arguments as String).endsWith('moduleA')
        (filters[1].fileInfoMatcherDescription.arguments as String).endsWith('moduleB')
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
        GradleProjectNature.INSTANCE.isPresentOn(project)
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
