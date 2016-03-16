package org.eclipse.buildship.core.workspace.internal

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Issue;

import com.google.common.collect.ImmutableList

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectBuilder
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.event.Event
import org.eclipse.buildship.core.event.EventListener
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.test.fixtures.TestEnvironment
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.util.variable.ExpressionUtils
import org.eclipse.buildship.core.workspace.WorkspaceGradleOperations

class ImportingProjectProjectCreatedEvent extends ProjectSynchronizationSpecification {

    def "Can receive ProjectCreatedEvent"() {
        given:
        boolean eventReceived = false

        CorePlugin.listenerRegistry().addEventListener(new EventListener() {
            public void onEvent(Event event) {
                eventReceived = true
            }
        })

        when:
        synchronizeAndWait(newSampleProject())

        then:
        eventReceived == true
    }

    def File newSampleProject() {
        workspaceDir('sample')
    }

}
