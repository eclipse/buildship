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


class ConfiguringGradleBuilderOnBrokenProject extends WorkspaceSpecification {

    @Shared
    @Subject
    GradleProjectBuilder builder = GradleProjectBuilder.INSTANCE

    IProject brokenProject = Stub(IProject) {
        isOpen() >> true
        getDescription() >> { throw new CoreException(new Status(IStatus.ERROR, "unknown", "thrown on purpose")) }
    }

    def "If configuration throws exception it is logged but not rethrown"() {
        given:
        Logger logger = Mock(Logger)
        registerService(Logger, logger)

        when:
        builder.configureOnProject(brokenProject)

        then:
        1 * logger.error(_)
    }

    def "If deconfiguration throws exception it is logged but not rethrown"() {
        given:
        Logger logger = Mock(Logger)
        registerService(Logger, logger)

        when:
        builder.deconfigureOnProject(brokenProject)

        then:
        1 * logger.error(_)
    }
}
