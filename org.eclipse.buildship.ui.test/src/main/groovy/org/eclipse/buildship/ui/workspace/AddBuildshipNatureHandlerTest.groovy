package org.eclipse.buildship.ui.workspace

import org.gradle.tooling.CancellationToken
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.model.eclipse.EclipseProject

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy

import org.eclipse.core.commands.Command
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.expressions.IEvaluationContext
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jface.viewers.StructuredSelection

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.BuildConfiguration
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration
import org.eclipse.buildship.core.event.Event
import org.eclipse.buildship.core.event.EventListener
import org.eclipse.buildship.core.workspace.GradleNatureAddedEvent
import org.eclipse.buildship.ui.test.fixtures.EclipseProjects
import org.eclipse.buildship.ui.test.fixtures.WorkspaceSpecification

class AddBuildshipNatureHandlerTest extends WorkspaceSpecification {

    def "Uses custom Gradle user home"() {
        setup:
        WorkspaceConfiguration originalWorkspaceConfig = CorePlugin.configurationManager().loadWorkspaceConfiguration()

        // if not the default location is specified then the Gradle
        // distribution is downloaded every time the test is executed
        File  gradleUserHome = new File(System.getProperty('user.home'), '.gradle')
        WorkspaceConfiguration config = new WorkspaceConfiguration(gradleUserHome, false, false)
        CorePlugin.configurationManager().saveWorkspaceConfiguration(config)

        IProject project = EclipseProjects.newProject('add-buildship-nature')
        waitForResourceChangeEvents()

        when:
        AddBuildshipNatureHandler handler = new AddBuildshipNatureHandler()
        handler.execute(projectSelectionEvent(project))
        waitForGradleJobsToFinish()

        then:
        eclipseModelLoadedWithGradleUserHome(project.location.toFile(), gradleUserHome)

        cleanup:
        CorePlugin.configurationManager().saveWorkspaceConfiguration(originalWorkspaceConfig)
    }

    def "Publishes 'nature added' event"() {
        setup:
        IProject project = EclipseProjects.newProject('test-nature-added-event')
        waitForResourceChangeEvents()

        TestEventListener eventListener = new TestEventListener()
        CorePlugin.listenerRegistry().addEventListener(eventListener)

        when:
        AddBuildshipNatureHandler handler = new AddBuildshipNatureHandler()
        handler.execute(projectSelectionEvent(project))
        waitForGradleJobsToFinish()

        then:
        eventListener.events.size() == 1
        eventListener.events[0] instanceof GradleNatureAddedEvent
        eventListener.events[0].projects == [project] as Set

        cleanup:
        CorePlugin.listenerRegistry().removeEventListener(eventListener)
    }

    private ExecutionEvent projectSelectionEvent(IProject selection) {
        IEvaluationContext context = Mock(IEvaluationContext)
        context.getVariable(_) >> new StructuredSelection(selection)
        ExecutionEvent event = new ExecutionEvent(new Command(''), [:], null, context)
        event
    }

    private boolean eclipseModelLoadedWithGradleUserHome(File projectLocation, File gradleUserHome) {
        BuildConfiguration buildConfig = CorePlugin.configurationManager().createBuildConfiguration(projectLocation, GradleDistribution.fromBuild(), false, false, false)
        CancellationToken token = GradleConnector.newCancellationTokenSource().token()
        IProgressMonitor monitor = new NullProgressMonitor()
        return CorePlugin.gradleWorkspaceManager().getGradleBuild(buildConfig).getModelProvider().fetchModels(EclipseProject.class, FetchStrategy.FROM_CACHE_ONLY, token, monitor) != null
    }

    private class TestEventListener implements EventListener {

        List events = []

        @Override
        public void onEvent(Event event) {
            events += event
        }

    }
}
