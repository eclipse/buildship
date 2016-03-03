package org.eclipse.buildship.core.configuration

import com.google.common.collect.ImmutableList
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.test.fixtures.TestEnvironment
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification;

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.ISchedulingRule
import org.eclipse.core.runtime.jobs.Job

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.Specification


class GradleProjectBuilderTest extends WorkspaceSpecification {

    def "Can configure builder on simple project"() {
        given:
        def project = newProject("sample-project")

        when:
        modifyWorkspace { GradleProjectBuilder.INSTANCE.configureOnProject(project) }

        then:
        project.getDescription().getBuildSpec().length == 1
        project.getDescription().getBuildSpec()[0].getBuilderName() == GradleProjectBuilder.ID
    }

    def "Builder configuration is idempotent"() {
        given:
        def project = newProject("sample-project")

        when:
        modifyWorkspace { GradleProjectBuilder.INSTANCE.configureOnProject(project) }
        modifyWorkspace { GradleProjectBuilder.INSTANCE.configureOnProject(project) }

        then:
        project.getDescription().getBuildSpec().length == 1
        project.getDescription().getBuildSpec()[0].getBuilderName() == GradleProjectBuilder.ID
    }

    def "Can deconfigure builder on simple project"() {
        given:
        def project = newProject("sample-project")

        when:
        modifyWorkspace { GradleProjectBuilder.INSTANCE.configureOnProject(project) }
        modifyWorkspace { GradleProjectBuilder.INSTANCE.deconfigureOnProject(project) }

        then:
        project.getDescription().getBuildSpec().length == 0
    }

    def "Can deconfigure if builder is not present"() {
        given:
        def project = newProject("sample-project")

        when:
        modifyWorkspace { GradleProjectBuilder.INSTANCE.deconfigureOnProject(project) }

        then:
        project.getDescription().getBuildSpec().length == 0
    }

    def "If configuration throws exception it is logged but not rethrown"() {
        given:
        Logger logger = Mock(Logger)
        TestEnvironment.registerService(Logger, logger)

        when:
        modifyWorkspace { GradleProjectBuilder.INSTANCE.configureOnProject(bogusProject) }

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
        modifyWorkspace { GradleProjectBuilder.INSTANCE.deconfigureOnProject(bogusProject) }

        then:
        1 * logger.error(_)

        cleanup:
        TestEnvironment.cleanup()
    }

    private IProject getBogusProject() {
        IProject project = Mock(IProject)
        project.isOpen() >> true
        project.getDescription() >> { throw new CoreException(new Status(IStatus.ERROR, "unknown", "thrown on purpose")) }
        project
    }

    private void modifyWorkspace(Closure closure) {
        Job job = new WorkspaceJob("Test") {
            IStatus runInWorkspace(IProgressMonitor arg0) throws CoreException {
                closure()
                Status.OK_STATUS
            };
        }

        job.schedule()
        job.join()
    }

}
