package org.eclipse.buildship.core.launch

import com.gradleware.tooling.toolingclient.BuildLaunchRequest
import com.gradleware.tooling.toolingclient.TestLaunchRequest
import com.gradleware.tooling.toolingclient.ToolingClient
import org.eclipse.buildship.core.console.ProcessStreams
import org.eclipse.buildship.core.console.ProcessStreamsProvider
import org.eclipse.buildship.core.test.fixtures.TestEnvironment
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchConfiguration

import org.gradle.tooling.events.test.TestOperationDescriptor
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class BaseLaunchRequestJobTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    BuildLaunchRequest buildRequest
    TestLaunchRequest testRequest
    ProcessStreamsProvider processStreamsProvider

    def setup() {
        buildRequest = Mock(BuildLaunchRequest)
        testRequest = Mock(TestLaunchRequest)
        ToolingClient toolingClient = Mock(ToolingClient)
        toolingClient.newBuildLaunchRequest(_) >> buildRequest
        toolingClient.newTestLaunchRequest(_) >> testRequest

        OutputStream configurationStream = Mock(OutputStream)
        ProcessStreams processStreams = Mock(ProcessStreams)
        processStreams.getConfiguration() >> configurationStream

        processStreamsProvider = Mock(ProcessStreamsProvider)
        processStreamsProvider.createProcessStreams(_) >> processStreams
        processStreamsProvider.getBackgroundJobProcessStreams() >> processStreams

        TestEnvironment.registerService(ToolingClient, toolingClient)
        TestEnvironment.registerService(ProcessStreamsProvider, processStreamsProvider)
    }

    def cleanup() {
        TestEnvironment.cleanup()
    }

    GradleRunConfigurationAttributes createRunConfigurationAttributesMock() {
        def launchConfiguration = createLaunchConfigurationMock()
        GradleRunConfigurationAttributes.from(launchConfiguration)
    }

    def createLaunchConfigurationMock() {
        def launchConfiguration = Mock(ILaunchConfiguration)
        launchConfiguration.getName() >> 'name'
        launchConfiguration.getAttribute('tasks', _) >> ['clean', 'build']
        launchConfiguration.getAttribute('gradle_distribution', _) >> 'GRADLE_DISTRIBUTION(WRAPPER)'
        launchConfiguration.getAttribute('working_dir', _) >> tempFolder.newFolder().absolutePath
        launchConfiguration.getAttribute('arguments', _) >> []
        launchConfiguration.getAttribute('jvm_arguments', _) >> []
        launchConfiguration
    }
}
