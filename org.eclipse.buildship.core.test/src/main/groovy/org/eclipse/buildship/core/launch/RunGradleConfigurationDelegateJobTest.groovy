package org.eclipse.buildship.core.launch

import com.gradleware.tooling.toolingclient.BuildLaunchRequest
import com.gradleware.tooling.toolingclient.ToolingClient
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import com.google.common.collect.ImmutableList

import org.eclipse.core.resources.IProject
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.jdt.core.IAccessRule

import org.eclipse.buildship.core.console.ProcessStreams
import org.eclipse.buildship.core.console.ProcessStreamsProvider
import org.eclipse.buildship.core.gradle.ToolingClientProvider
import org.eclipse.buildship.core.test.fixtures.TestEnvironment
import org.eclipse.buildship.core.workspace.WorkspaceOperations

import spock.lang.Specification


class RunGradleConfigurationDelegateJobTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    ToolingClientProvider toolingClientProvider
    ProcessStreamsProvider procesStramsProvider

    def setup() {
        TestEnvironment.testService(ToolingClientProvider, toolingClientProvider = mockedToolingClientProvider)
        TestEnvironment.testService(ProcessStreamsProvider, procesStramsProvider = mockedProcessStreamsProvider)
    }

    def cleanup() {
        TestEnvironment.cleanup()
    }

    def "Job launches the Gradle build"() {
        setup:
        def job = new RunGradleConfigurationDelegateJob(mockedLaunch, mockedLaunchConfiguration)

        when:
        job.schedule()
        job.join()

        then:
        job.getResult().isOK()
        1 * toolingClientProvider.newClient().newBuildLaunchRequest().executeAndWait()
    }

    def "Job displays its configuration"() {
        setup:
        def job = new RunGradleConfigurationDelegateJob(mockedLaunch, mockedLaunchConfiguration)

        when:
        job.schedule()
        job.join()

        then:
        1 * procesStramsProvider.createProcessStreams(null).getConfiguration().flush()
    }

    private def getMockedLaunch() {
        Mock(ILaunch)
    }

    private def getMockedLaunchConfiguration() {
        def launchConfiguration = Mock(ILaunchConfiguration)
        launchConfiguration.getAttribute('tasks', _) >> ['clean', 'build']
        launchConfiguration.getAttribute('gradle_distribution', _) >> 'GRADLE_DISTRIBUTION(WRAPPER)'
        launchConfiguration.getAttribute('working_dir', _) >> tempFolder.newFolder().absolutePath
        launchConfiguration.getAttribute('arguments', _) >> []
        launchConfiguration.getAttribute('jvm_arguments', _) >> []
        launchConfiguration
    }

    private def getMockedToolingClientProvider() {
        BuildLaunchRequest request = Mock(BuildLaunchRequest)

        ToolingClient toolingClient = Mock(ToolingClient)
        toolingClient.newBuildLaunchRequest(_) >> request

        ToolingClientProvider toolingClientProvider = Mock(ToolingClientProvider)
        toolingClientProvider.newClient() >> toolingClient
        toolingClientProvider
    }

    private def getMockedProcessStreamsProvider() {
        OutputStream configurationStream = Mock(OutputStream)

        ProcessStreams processStreams = Mock(ProcessStreams)
        processStreams.getConfiguration() >> configurationStream

        ProcessStreamsProvider provider = Mock(ProcessStreamsProvider)
        provider.createProcessStreams(_) >> processStreams
        provider.getBackgroundJobProcessStreams() >> processStreams
        provider
    }

}
