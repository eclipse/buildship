package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject

import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.workspace.GradleClasspathContainer

class SynchronizingExistingWorkspaceProjectTest extends CoupledProjectSynchronizationSpecification {

    def "If the project is closed, then the project remains untouched"() {
        setup:
        IProject project = newClosedProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'settings.gradle'
        }
        File[] projectFiles = dir('sample-project').listFiles()
        Long[] modifiedTimes = dir('sample-project').listFiles().collect{ it.lastModified() }

        when:
        synchronizeAndWait(projectDir)

        then:
        !project.isOpen()
        projectFiles == dir('sample-project').listFiles()
        modifiedTimes == dir('sample-project').listFiles().collect{ it.lastModified() }
    }

    def "The Gradle classpath container is updated"() {
        setup:
        IJavaProject javaProject = newJavaProject('sample-project')
        IClasspathEntry[] entries = javaProject.rawClasspath + GradleClasspathContainer.newClasspathEntry()
        javaProject.setRawClasspath(entries, null)
        def projectDir = dir('sample-project') {
            file 'build.gradle','''apply plugin: "java"
               repositories { jcenter() }
               dependencies { compile "org.springframework:spring-beans:1.2.8"}
            '''
            dir 'src/main/java'
        }

        expect:
        !javaProject.getResolvedClasspath(false).find{ it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }

        when:
        synchronizeAndWait(projectDir)

        then:
        javaProject.getResolvedClasspath(false).find{ it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }
    }

    @Override
    protected void prepareProject(String name) {
        newOpenProject(name)
    }

    @Override
    protected void prepareJavaProject(String name) {
        newJavaProject(name)
    }
}
