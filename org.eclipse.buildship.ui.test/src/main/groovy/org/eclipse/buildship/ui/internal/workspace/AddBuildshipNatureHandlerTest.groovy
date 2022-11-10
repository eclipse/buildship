/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.workspace

import org.gradle.api.JavaVersion
import org.gradle.tooling.CancellationTokenSource
import org.gradle.tooling.GradleConnector
import spock.lang.IgnoreIf

import org.eclipse.core.commands.Command
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.expressions.IEvaluationContext
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jface.viewers.StructuredSelection

import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration
import org.eclipse.buildship.core.internal.configuration.WorkspaceConfiguration
import org.eclipse.buildship.core.internal.event.Event
import org.eclipse.buildship.core.internal.event.EventListener
import org.eclipse.buildship.core.internal.workspace.FetchStrategy
import org.eclipse.buildship.core.internal.workspace.GradleNatureAddedEvent
import org.eclipse.buildship.model.ExtendedEclipseModel
import org.eclipse.buildship.ui.internal.test.fixtures.EclipseProjects
import org.eclipse.buildship.ui.internal.test.fixtures.WorkspaceSpecification

class AddBuildshipNatureHandlerTest extends WorkspaceSpecification {

    @IgnoreIf({ JavaVersion.current().isJava9Compatible() }) // https://github.com/eclipse/buildship/issues/601
    def "Uses configuration from workspace settings"() {
        setup:
        WorkspaceConfiguration originalWorkspaceConfig = configurationManager.loadWorkspaceConfiguration()
        WorkspaceConfiguration config = new WorkspaceConfiguration(GradleDistribution.forVersion("3.0"), dir('custom-gradle-home'), null, false, false, false, [], [], false, false, false)
        configurationManager.saveWorkspaceConfiguration(config)

        IProject project = EclipseProjects.newProject('add-buildship-nature')
        project.getFile("settings.gradle").create(new ByteArrayInputStream("".bytes), true, new NullProgressMonitor())
        waitForResourceChangeEvents()

        when:
        AddBuildshipNatureHandler handler = new AddBuildshipNatureHandler()
        handler.execute(projectSelectionEvent(project))
        waitForGradleJobsToFinish()

        then:
        eclipseModelLoadedWithWorkspacePreferences(project.location.toFile())

        cleanup:
        configurationManager.saveWorkspaceConfiguration(originalWorkspaceConfig)
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
        GradleNatureAddedEvent event = eventListener.events.find { it instanceof GradleNatureAddedEvent }

        then:
        event.project == project

        cleanup:
        CorePlugin.listenerRegistry().removeEventListener(eventListener)
    }

    private ExecutionEvent projectSelectionEvent(IProject selection) {
        IEvaluationContext context = Mock(IEvaluationContext)
        context.getVariable(_) >> new StructuredSelection(selection)
        ExecutionEvent event = new ExecutionEvent(new Command(''), [:], null, context)
        event
    }

    private boolean eclipseModelLoadedWithWorkspacePreferences(File projectLocation) {
        BuildConfiguration buildConfig = createInheritingBuildConfiguration(projectLocation)
        CancellationTokenSource tokenSource = GradleConnector.newCancellationTokenSource()
        IProgressMonitor monitor = new NullProgressMonitor()
        return CorePlugin.internalGradleWorkspace().getGradleBuild(buildConfig).getModelProvider().fetchModels(ExtendedEclipseModel.class, FetchStrategy.FROM_CACHE_ONLY, tokenSource, monitor) != null
    }

    private class TestEventListener implements EventListener {

        List events = []

        @Override
        public void onEvent(Event event) {
            events += event
        }

    }
}
