package org.eclipse.buildship.core

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification

class GradleWorkspaceTest extends WorkspaceSpecification {

    def "Cannot get reference to a null Gradle build"() {
        when:
        GradleCore.workspace.getBuild(null)

        then:
        thrown(NullPointerException)
    }

    def "Cannot get reference to Gradle build if target project is inaccessible"() {
        setup:
        IProject project = newClosedProject("GradleWorkspaceTest")

        when:
        GradleCore.workspace.getBuild(project)

        then:
        thrown(IllegalArgumentException)
    }

    def "Cannot get reference to Gradle build if target project is not a Gradle project"() {
        setup:
        IProject project = newProject("GradleWorkspaceTest")

        when:
        GradleCore.workspace.getBuild(project)

        then:
        thrown(IllegalArgumentException)
    }

    def "Can get reference to Gradle build via valid Gradle proejct"() {
        setup:
        IProject project = newProject("GradleWorkspaceTest")
        CorePlugin.workspaceOperations().addNature(project, GradleProjectNature.ID, new NullProgressMonitor())

        expect:
        GradleCore.workspace.getBuild(project)
    }
}
