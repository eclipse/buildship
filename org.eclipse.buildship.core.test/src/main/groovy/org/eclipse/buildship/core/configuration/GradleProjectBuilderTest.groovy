package org.eclipse.buildship.core.configuration

import spock.lang.Shared;
import spock.lang.Subject;

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status

import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.core.test.fixtures.TestEnvironment
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification


class GradleProjectBuilderTest extends WorkspaceSpecification {

    @Shared
    @Subject
    GradleProjectBuilder builder = GradleProjectBuilder.INSTANCE

    def "Can configure builder on simple project"() {
        given:
        def project = newProject("sample-project")

        when:
        builder.configureOnProject(project)

        then:
        builderNames(project) == [GradleProjectBuilder.ID]
    }

    def "Builder configuration is idempotent"() {
        given:
        def project = newProject("sample-project")
        builder.configureOnProject(project)

        when:
        builder.configureOnProject(project)

        then:
        builderNames(project) == [GradleProjectBuilder.ID]
    }

    def "Can deconfigure builder on simple project"() {
        given:
        def project = newProject("sample-project")
        builder.configureOnProject(project)

        when:
        builder.deconfigureOnProject(project)

        then:
        builderNames(project).empty
    }

    def "Deconfiguring is a no-op if builder is not present"() {
        given:
        def project = newProject("sample-project")

        when:
        builder.deconfigureOnProject(project)

        then:
        builderNames(project).empty
    }

    def "If configuration throws exception it is logged but not rethrown"() {
        given:
        Logger logger = Mock(Logger)
        TestEnvironment.registerService(Logger, logger)

        when:
        builder.configureOnProject(bogusProject)

        then:
        1 * logger.error(_)

        cleanup:
        TestEnvironment.cleanup()
    }

    def "If deconfiguration throws exception it is logged but not rethrown"() {
        given:
        Logger logger = Mock(Logger)
        TestEnvironment.registerService(Logger, logger)

        when:
        builder.deconfigureOnProject(bogusProject)

        then:
        1 * logger.error(_)

        cleanup:
        TestEnvironment.cleanup()
    }

    private List<String> builderNames(IProject project) {
        project.description.buildSpec*.builderName
    }

    private IProject getBogusProject() {
        return Stub(IProject) {
            isOpen() >> true
            getDescription() >> { throw new CoreException(new Status(IStatus.ERROR, "unknown", "thrown on purpose")) }
        }
    }
}
