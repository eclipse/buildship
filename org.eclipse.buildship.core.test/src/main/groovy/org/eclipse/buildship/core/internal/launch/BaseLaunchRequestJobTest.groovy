package org.eclipse.buildship.core.internal.launch

import org.eclipse.debug.core.ILaunchConfiguration

import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.console.ProcessStreamsProvider
import org.eclipse.buildship.core.internal.test.fixtures.TestProcessStreamProvider
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification

class BaseLaunchRequestJobTest extends WorkspaceSpecification {

    def setup() {
        environment.registerService(ProcessStreamsProvider, new TestProcessStreamProvider() {})
    }

    String getBuildOutput() {
        TestProcessStreamProvider testStreams = CorePlugin.processStreamsProvider()
        testStreams.processStreams.last().out
    }

    String getBuildConfig() {
        TestProcessStreamProvider testStreams = CorePlugin.processStreamsProvider()
        testStreams.processStreams.last().conf
    }

    ILaunchConfiguration createLaunchConfiguration(File projectDir, tasks = ['clean', 'build'], GradleDistribution distribution = GradleDistribution.fromBuild(), arguments = []) {
        ILaunchConfiguration launchConfiguration = Mock(ILaunchConfiguration)
        launchConfiguration.getName() >> 'name'
        launchConfiguration.getAttribute('override_workspace_settings', _) >> 'true'
        launchConfiguration.getAttribute('tasks', _) >> tasks
        launchConfiguration.getAttribute('working_dir', _) >> projectDir
        launchConfiguration.getAttribute('gradle_distribution', _) >> distribution.toString()
        launchConfiguration.getAttribute('arguments', _) >> arguments
        launchConfiguration.getAttribute('jvm_arguments', _) >> []
        launchConfiguration
    }

}
