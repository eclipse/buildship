package org.eclipse.buildship.core.configuration

import spock.lang.AutoCleanup;
import spock.lang.Shared;
import spock.lang.Subject;

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status

import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.core.test.fixtures.TestEnvironment
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification


class ConfiguringGradleBuilderOnRegularProject extends WorkspaceSpecification {

    IProject project

    def setup() {
        project = newProject("sample")
    }

    def "Can configure builder"() {
        when:
        GradleProjectBuilder.configureOnProject(project)

        then:
        builderNames(project) == [GradleProjectBuilder.ID]
    }

    def "Builder configuration is idempotent"() {
        given:
        GradleProjectBuilder.configureOnProject(project)

        when:
        GradleProjectBuilder.configureOnProject(project)

        then:
        builderNames(project) == [GradleProjectBuilder.ID]
    }

    def "Can deconfigure builder"() {
        given:
        GradleProjectBuilder.configureOnProject(project)

        when:
        GradleProjectBuilder.deconfigureOnProject(project)

        then:
        builderNames(project).empty
    }

    def "Deconfiguring is a no-op if builder is not present"() {
        when:
        GradleProjectBuilder.deconfigureOnProject(project)

        then:
        builderNames(project).empty
    }

    private List<String> builderNames(IProject project) {
        project.description.buildSpec*.builderName
    }
}
