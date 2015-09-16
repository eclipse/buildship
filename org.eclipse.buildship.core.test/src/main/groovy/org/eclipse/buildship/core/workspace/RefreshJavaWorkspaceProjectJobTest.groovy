package org.eclipse.buildship.core.workspace

import org.gradle.tooling.GradleConnector

import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes

import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.test.fixtures.ProjectImportSpecification

class RefreshJavaWorkspaceProjectJobTest extends ProjectImportSpecification {

    def setup() {
        executeProjectImportAndWait(createSampleProject())
    }

    def "Removed project doesn't have Builship artifacts"() {
        setup:
        file('sample', 'settings.gradle').text  = "include 'moduleA'"
        IProject moduleB = CorePlugin.workspaceOperations().findProjectByName('moduleB').get()

        expect:
        moduleB.hasNature(GradleProjectNature.ID)
        moduleB.filters.length > 0
        moduleB.getFolder('.settings').exists()
        moduleB.getFolder('.settings').getFile('gradle.prefs').exists()
        JavaCore.create(moduleB).getResolvedClasspath(false).find{ it.path.toPortableString().contains('junit') }

        when:
        // reload Gradle model
        ProjectConfiguration configuration = CorePlugin.projectConfigurationManager().readProjectConfiguration(moduleB)
        CorePlugin.modelRepositoryProvider().getModelRepository(configuration.requestAttributes).fetchEclipseGradleBuild(new TransientRequestAttributes(false, System.out, System.err, System.in, [] as List, [] as List, GradleConnector.newCancellationTokenSource().token()), FetchStrategy.FORCE_RELOAD)
        // refresh the project
        def job = new RefreshJavaWorkspaceProjectJob(JavaCore.create(moduleB))
        job.schedule()
        job.join()

        then:
        !moduleB.hasNature(GradleProjectNature.ID)
        moduleB.filters.length == 0
        !moduleB.getFolder('.settings').exists()
        !moduleB.getFolder('.settings').getFile('gradle.prefs').exists()
        !JavaCore.create(moduleB).getResolvedClasspath(false).find{ it.path.toPortableString().contains('junit') }

    }

    private def createSampleProject() {
        file('sample', 'build.gradle') <<
                '''allprojects {
               repositories { mavenCentral() }
               apply plugin: 'java'
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
