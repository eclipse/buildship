package org.eclipse.buildship.core.workspace

import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.configuration.ProjectConfiguration
import org.eclipse.buildship.core.test.fixtures.ProjectImportSpecification
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore
import org.gradle.tooling.GradleConnector

class UpdateClasspathContainerJobTest extends ProjectImportSpecification {

    def setup() {
        executeProjectImportAndWait(createSampleProject())
        waitForJobsToFinish()
    }

    def "If a project is removed from the Gradle model, the container updater does nothing"() {
        setup:
        file('sample', 'settings.gradle').text = "include 'moduleA'"
        IProject project = CorePlugin.workspaceOperations().findProjectByName('moduleB').get()

        expect:
        JavaCore.create(project).getResolvedClasspath(false).find { it.path.toPortableString().contains('junit') }

        when:
        executeUpdateClasspathContainerJobAndWait(project)

        then:
        JavaCore.create(project).getResolvedClasspath(false).find { it.path.toPortableString().contains('junit') }
    }

    def "Existing project is updated"() {
        setup:
        file('sample', 'moduleB', 'build.gradle') << "dependencies { compile 'org.springframework:spring-beans:1.2.8' }"
        IProject project = CorePlugin.workspaceOperations().findProjectByName('moduleB').get()

        expect:
        !JavaCore.create(project).getResolvedClasspath(false).find { it.path.toPortableString().contains('spring-beans') }

        when:
        executeUpdateClasspathContainerJobAndWait(project)

        then:
        JavaCore.create(project).getResolvedClasspath(false).find { it.path.toPortableString().contains('spring-beans') }
    }

    private def executeUpdateClasspathContainerJobAndWait(IProject project) {
        def job = new UpdateClasspathContainerJob(JavaCore.create(project), FetchStrategy.FORCE_RELOAD)
        job.schedule()
        job.join()
    }

    private def createSampleProject() {
        file('sample', 'build.gradle') <<
                '''allprojects {
               repositories { mavenCentral() }
           }
        '''
        file('sample', 'settings.gradle') <<
                """
           include 'moduleA'
           include 'moduleB'
        """
        file('sample', 'moduleA', 'build.gradle') << "apply plugin: 'java'"
        folder('sample', 'moduleA', 'src', 'main', 'java')
        file('sample', 'moduleB', 'build.gradle') <<
                """apply plugin: 'java'
           dependencies { testCompile "junit:junit:4.12" }
        """
        folder('sample', 'moduleB', 'src', 'main', 'java')
        folder('sample')
    }
}
