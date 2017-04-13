package org.eclipse.buildship.core.launch

import org.eclipse.debug.core.ILaunchConfiguration

import org.eclipse.buildship.core.console.ProcessStreams
import org.eclipse.buildship.core.console.ProcessStreamsProvider
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class BaseLaunchRequestJobTest extends WorkspaceSpecification {

    private ProcessStreamsProvider processStreamsProvider
    private ByteArrayOutputStream buildOutputStream
    private ByteArrayOutputStream buildConfigurationStream

    void setup() {
        ProcessStreams processStreams = Mock(ProcessStreams)
        buildOutputStream = new ByteArrayOutputStream()
        buildConfigurationStream = new ByteArrayOutputStream()
        processStreams.output >> buildOutputStream
        processStreams.configuration >> buildConfigurationStream

        processStreamsProvider = Mock(ProcessStreamsProvider)
        processStreamsProvider.createProcessStreams(_) >> processStreams
        processStreamsProvider.getBackgroundJobProcessStreams() >> processStreams

        environment.registerService(ProcessStreamsProvider, processStreamsProvider)
    }

    String getBuildOutput() {
        buildOutputStream.toString()
    }

    String getBuildConfig() {
        buildConfigurationStream.toString()
    }

    ILaunchConfiguration createLaunchConfiguration(File projectDir, tasks = ['clean', 'build']) {
        ILaunchConfiguration launchConfiguration = Mock(ILaunchConfiguration)
        launchConfiguration.getName() >> 'name'
        launchConfiguration.getAttribute('tasks', _) >> tasks
        launchConfiguration.getAttribute('working_dir', _) >> projectDir
        launchConfiguration.getAttribute('gradle_distribution', _) >> 'GRADLE_DISTRIBUTION(WRAPPER)'
        launchConfiguration.getAttribute('arguments', _) >> []
        launchConfiguration.getAttribute('jvm_arguments', _) >> []
        launchConfiguration
    }

}
