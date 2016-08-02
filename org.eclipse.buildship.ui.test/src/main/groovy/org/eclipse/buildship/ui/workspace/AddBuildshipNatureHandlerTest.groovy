package org.eclipse.buildship.ui.workspace

import org.gradle.tooling.GradleConnector

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes

import org.eclipse.core.commands.Command
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.expressions.IEvaluationContext
import org.eclipse.core.resources.IProject
import org.eclipse.jface.viewers.StructuredSelection

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration
import org.eclipse.buildship.ui.test.fixtures.EclipseProjects
import org.eclipse.buildship.ui.test.fixtures.WorkspaceSpecification

class AddBuildshipNatureHandlerTest extends WorkspaceSpecification {

    def "Uses custom Gradle user home"() {
        setup:
        WorkspaceConfiguration originalWorkspaceConfig = CorePlugin.workspaceConfigurationManager().loadWorkspaceConfiguration()

        // if not the default location is specified then the Gradle
        // distribution is downloaded every time the test is executed
        File  gradleUserHome = new File(System.getProperty('user.home'), '.gradle')
        WorkspaceConfiguration config = new WorkspaceConfiguration(gradleUserHome)
        CorePlugin.workspaceConfigurationManager().saveWorkspaceConfiguration(config)

        IProject project = EclipseProjects.newProject('add-buildship-nature')
        waitForResourceChangeEvents()

        when:
        AddBuildshipNatureHandler handler = new AddBuildshipNatureHandler()
        handler.execute(projectSelectionEvent(project))
        waitForGradleJobsToFinish()

        then:
        eclipseModelLoadedWithGradleUserHome(project.location.toFile(), gradleUserHome)

        cleanup:
        CorePlugin.workspaceConfigurationManager().saveWorkspaceConfiguration(originalWorkspaceConfig)
    }

    private ExecutionEvent projectSelectionEvent(IProject selection) {
        IEvaluationContext context = Mock(IEvaluationContext)
        context.getVariable(_) >> new StructuredSelection(selection)
        ExecutionEvent event = new ExecutionEvent(new Command(''), [:], null, context)
        event
    }

    private boolean eclipseModelLoadedWithGradleUserHome(File projectLocation, File gradleUserHome) {
        FixedRequestAttributes attributes = new FixedRequestAttributes(projectLocation, gradleUserHome, GradleDistribution.fromBuild(), null, [], [])
        TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, System.out, System.err, System.in, [], [], GradleConnector.newCancellationTokenSource().token())
        return CorePlugin.modelRepositoryProvider().getModelRepository(attributes).fetchEclipseGradleBuild(transientAttributes, FetchStrategy.FROM_CACHE_ONLY) != null
    }

}
