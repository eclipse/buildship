package org.eclipse.buildship.ui.workspace

import org.gradle.tooling.CancellationToken
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.model.eclipse.EclipseProject

import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes

import org.eclipse.core.commands.Command
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.expressions.IEvaluationContext
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jface.viewers.StructuredSelection

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration
import org.eclipse.buildship.ui.test.fixtures.WorkspaceSpecification

class AddBuildshipNatureHandlerTest extends WorkspaceSpecification {

    def "Uses custom Gradle user home"() {
        setup:
        WorkspaceConfiguration originalWorkspaceConfig = CorePlugin.workspaceConfigurationManager().loadWorkspaceConfiguration()

        // if not the default location is specified then the Gradle
        // distribution is downloaded every time the test is executed
        File  gradleUserHome = new File(System.getProperty('user.home'), '.gradle')
        WorkspaceConfiguration config = new WorkspaceConfiguration(gradleUserHome, false, false)
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
        FixedRequestAttributes attributes = FixedRequestAttributesBuilder.fromEmptySettings(projectLocation).gradleUserHome(gradleUserHome).build();
        CancellationToken token = GradleConnector.newCancellationTokenSource().token()
        IProgressMonitor monitor = new NullProgressMonitor()
        return CorePlugin.gradleWorkspaceManager().getGradleBuild(attributes).getModelProvider().fetchModels(EclipseProject.class, FetchStrategy.FROM_CACHE_ONLY, token, monitor) != null
    }

}
