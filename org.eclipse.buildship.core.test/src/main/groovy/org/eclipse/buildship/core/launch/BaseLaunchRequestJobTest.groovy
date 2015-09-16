package org.eclipse.buildship.core.launch

import com.gradleware.tooling.toolingclient.ToolingClient
import org.eclipse.buildship.core.console.ProcessStreams
import org.eclipse.buildship.core.console.ProcessStreamsProvider
import org.eclipse.buildship.core.test.fixtures.TestEnvironment
import org.eclipse.debug.core.ILaunchConfiguration
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class BaseLaunchRequestJobTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    ToolingClient toolingClient
    ProcessStreamsProvider processStreamsProvider

    def setup() {
        toolingClient = Mock(ToolingClient)

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

    def createLaunchConfigurationMock() {
        def launchConfiguration = Mock(ILaunchConfiguration)
        launchConfiguration.getName() >> 'name'
        launchConfiguration.getAttribute('tasks', _) >> ['clean', 'build']
        launchConfiguration.getAttribute('working_dir', _) >> tempFolder.newFolder().absolutePath
        launchConfiguration.getAttribute('gradle_distribution', _) >> 'GRADLE_DISTRIBUTION(WRAPPER)'
        launchConfiguration.getAttribute('arguments', _) >> []
        launchConfiguration.getAttribute('jvm_arguments', _) >> []
        launchConfiguration
    }

}
