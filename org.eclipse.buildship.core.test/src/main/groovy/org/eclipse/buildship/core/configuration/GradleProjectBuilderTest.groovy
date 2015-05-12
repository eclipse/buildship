package org.eclipse.buildship.core.configuration

import com.google.common.collect.ImmutableList

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import org.eclipse.core.resources.ICommand
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Status

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.core.test.fixtures.TestEnvironment

import spock.lang.Specification


class GradleProjectBuilderTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    def projectFolder

    def setup() {
        projectFolder = tempFolder.newFolder("sample-project-folder")
    }

    def cleanup() {
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
    }

    def "Can configure builder on simple project"() {
        setup:
        IProject project = CorePlugin.workspaceOperations().createProject("sample-project", projectFolder, ImmutableList.of(), ImmutableList.of(), null)

        when:
        GradleProjectBuilder.INSTANCE.configureOnProject(project)

        then:
        project.getDescription().getBuildSpec().length == 1
        project.getDescription().getBuildSpec()[0].getBuilderName() == GradleProjectBuilder.ID
    }

    def "Builder configuration is idempotent"() {
        setup:
        IProject project = CorePlugin.workspaceOperations().createProject("sample-project", projectFolder, ImmutableList.of(), ImmutableList.of(), null)

        when:
        GradleProjectBuilder.INSTANCE.configureOnProject(project)
        GradleProjectBuilder.INSTANCE.configureOnProject(project)

        then:
        project.getDescription().getBuildSpec().length == 1
        project.getDescription().getBuildSpec()[0].getBuilderName() == GradleProjectBuilder.ID
    }


    def "Can deconfigure builder on simple project"() {
        setup:
        IProject project = CorePlugin.workspaceOperations().createProject("sample-project", projectFolder, ImmutableList.of(), ImmutableList.of(), null)

        when:
        GradleProjectBuilder.INSTANCE.configureOnProject(project)
        GradleProjectBuilder.INSTANCE.deconfigureOnProject(project)

        then:
        project.getDescription().getBuildSpec().length == 0
    }

    def "Can deconfigure if builder is not present"() {
        setup:
        IProject project = CorePlugin.workspaceOperations().createProject("sample-project", projectFolder, ImmutableList.of(), ImmutableList.of(), null)

        when:
        GradleProjectBuilder.INSTANCE.deconfigureOnProject(project)

        then:
        project.getDescription().getBuildSpec().length == 0
    }

    def "If configuration throws exception it is logged but not rethrown" () {
        setup:
        Logger logger = Mock(Logger)
        TestEnvironment.testService(Logger, logger)

        when:
        GradleProjectBuilder.INSTANCE.configureOnProject(bogusProject)

        then:
        1 * logger.error(_)

        cleanup:
        TestEnvironment.cleanup()
    }

    def "If deconfiguration throws exception it is logged but not rethrown" () {
        setup:
        Logger logger = Mock(Logger)
        TestEnvironment.testService(Logger, logger)

        when:
        GradleProjectBuilder.INSTANCE.deconfigureOnProject(bogusProject)

        then:
        1 * logger.error(_)

        cleanup:
        TestEnvironment.cleanup()
    }



    private IProject getBogusProject() {
        IProject project = Mock(IProject)
        project.isOpen() >> true
        project.getDescription() >> { throw new CoreException(new Status(IStatus.ERROR, "unknown", "errormessage")) }

        project
    }
}
