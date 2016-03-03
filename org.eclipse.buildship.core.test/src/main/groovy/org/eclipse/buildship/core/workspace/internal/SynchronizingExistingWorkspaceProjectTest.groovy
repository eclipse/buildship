package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject

import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.test.fixtures.GradleModel
import org.eclipse.buildship.core.workspace.GradleClasspathContainer

class SynchronizingExistingWorkspaceProjectTest extends CoupledProjectSynchronizationSpecification {

    def "If the project is closed, then the project remains untouched"() {
        setup:
        IProject project = newClosedProject('sample-project')
        fileTree('sample-project') {
            file 'settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')
        File[] projectFiles = folder('sample-project').listFiles()
        Long[] modifiedTimes = folder('sample-project').listFiles().collect{ it.lastModified() }

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        !project.isOpen()
        projectFiles == folder('sample-project').listFiles()
        modifiedTimes == folder('sample-project').listFiles().collect{ it.lastModified() }
    }

    def "The Gradle classpath container is updated"() {
        setup:
        IJavaProject javaProject = newJavaProject('sample-project')
        IClasspathEntry[] entries = javaProject.rawClasspath + GradleClasspathContainer.newClasspathEntry()
        javaProject.setRawClasspath(entries, null)
        fileTree('sample-project') {
            file 'build.gradle','''apply plugin: "java"
               repositories { jcenter() }
               dependencies { compile "org.springframework:spring-beans:1.2.8"}
            '''
            dir 'src/main/java'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        expect:
        !javaProject.getResolvedClasspath(false).find{ it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

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
