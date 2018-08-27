package org.eclipse.buildship.core.internal

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IProgressMonitor

import org.eclipse.buildship.core.GradleBuild
import org.eclipse.buildship.core.GradleCore
import org.eclipse.buildship.core.SynchronizationResult
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class GradleBuildSynchronizationTest extends ProjectSynchronizationSpecification {

    IProject project

    def setup() {
        // (TODO) donat import a Gradle project
    }

    def "Synchronizing a Gradle project"() {
        setup:
        IProgressMonitor monitor = Mock(IProgressMonitor)
        GradleBuild build = GradleCore.workspace.getBuild(project)

        when:
        SynchronizationResult result = build.synchronize(monitor)

        then:
        true
    }

    def "Synchronizing non-Gradle project"() {
    }

    def "Sychronizing project with invalid build script"() {
    }

    def "Sychronizing project with invalid configuration"() {
    }

    def "Sychronizing project with name that clashes with workspace project"() {
    }

    def "Progress monitor is null"() {
    }

    def "Cancelling synhronization"() {
    }

    def "Synchronizing multiple projects concurrently"() {
    }

    def "Using no job rules"() {
    }

    def "Using custom job rule"() {
    }

    def "Workspace job rule"() {
    }
}
