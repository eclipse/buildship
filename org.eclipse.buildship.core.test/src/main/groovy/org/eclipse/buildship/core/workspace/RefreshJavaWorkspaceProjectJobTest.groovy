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

class RefreshJavaWorkspaceProjectJobTest extends ProjectImportSpecification {

    def setup() {
        executeProjectImportAndWait(createSampleProject())
        waitForJobsToFinish()
    }

    def "Removed project doesn't have Buildship artifacts"() {
        setup:
        file('sample', 'settings.gradle').text  = "include 'moduleA'"
        IProject project = CorePlugin.workspaceOperations().findProjectByName('moduleB').get()

        expect:
        project.hasNature(GradleProjectNature.ID)
        project.filters.length > 0
        project.getFolder('.settings').exists()
        project.getFolder('.settings').getFile('gradle.prefs').exists()
        JavaCore.create(project).getResolvedClasspath(false).find{ it.path.toPortableString().contains('junit') }

        when:
        executeSynchronizeJavaWorkspaceProjectJobAndWait(project)

        then:
        !project.hasNature(GradleProjectNature.ID)
        project.filters.length == 0
        !project.getFolder('.settings').exists()
        !project.getFolder('.settings').getFile('gradle.prefs').exists()
        !JavaCore.create(project).getResolvedClasspath(false).find{ it.path.toPortableString().contains('junit') }
    }

    def "Existing project is updated"() {
        setup:
        folder('sample', 'moduleB', 'src', 'test', 'java')
        file('sample', 'moduleB', 'build.gradle') << "dependencies { compile 'org.springframework:spring-beans:1.2.8' }"

        when:
        IProject project = CorePlugin.workspaceOperations().findProjectByName('moduleB').get()
        executeSynchronizeJavaWorkspaceProjectJobAndWait(project)

        then:
        JavaCore.create(project).rawClasspath.find{ it.entryKind == IClasspathEntry.CPE_SOURCE && it.path.toPortableString() == '/moduleB/src/test/java' }
        JavaCore.create(project).getResolvedClasspath(false).find{ it.path.toPortableString().contains('spring-beans') }
    }

    def "A project without a Gradle nature should have an empty classpath container"() {
        setup:
        IProject moduleB = CorePlugin.workspaceOperations().findProjectByName('moduleB').get()
        CorePlugin.workspaceOperations().removeNature(moduleB, GradleProjectNature.ID, new NullProgressMonitor())

        when:
        executeSynchronizeJavaWorkspaceProjectJobAndWait(moduleB)

        then:
        !JavaCore.create(moduleB).getResolvedClasspath(false).find{ it.path.toPortableString().contains('junit') }
    }

    private def executeSynchronizeJavaWorkspaceProjectJobAndWait(IProject project) {
        // reload Gradle model
        ProjectConfiguration configuration = CorePlugin.projectConfigurationManager().readProjectConfiguration(project)
        CorePlugin.modelRepositoryProvider().getModelRepository(configuration.requestAttributes).fetchEclipseGradleBuild(new TransientRequestAttributes(false, System.out, System.err, System.in, [] as List, [] as List, GradleConnector.newCancellationTokenSource().token()), FetchStrategy.FORCE_RELOAD)
        // synchronize the project
        def job = new SynchronizeJavaWorkspaceProjectJob(JavaCore.create(project))
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
