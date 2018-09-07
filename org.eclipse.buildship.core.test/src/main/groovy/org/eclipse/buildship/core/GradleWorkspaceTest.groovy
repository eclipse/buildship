package org.eclipse.buildship.core

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification

class GradleWorkspaceTest extends WorkspaceSpecification {

    def "Cannot get reference to a null Gradle build"() {
        expect:
        !GradleCore.workspace.getBuild(null).present
    }

    def "Cannot get reference to Gradle build if target project is inaccessible"() {
        expect:
        !GradleCore.workspace.getBuild(newClosedProject("GradleWorkspaceTest")).present
    }

    def "Cannot get reference to Gradle build if target project is not a Gradle project"() {
        expect:
        !GradleCore.workspace.getBuild(newProject("GradleWorkspaceTest")).present
    }

    def "Can get reference to Gradle build via valid Gradle proejct"() {
        setup:
        IProject project = newProject("GradleWorkspaceTest")
        CorePlugin.workspaceOperations().addNature(project, GradleProjectNature.ID, new NullProgressMonitor())

        expect:
        GradleCore.workspace.getBuild(project).present
    }
}
