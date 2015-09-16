package org.eclipse.buildship.core.launch

import com.gradleware.tooling.toolingclient.BuildLaunchRequest
import org.eclipse.debug.core.ILaunch

class RunGradleBuildLaunchRequestJobTest extends BaseLaunchRequestJobTest {

    BuildLaunchRequest buildLaunchRequest

    def setup() {
        buildLaunchRequest = Mock(BuildLaunchRequest)
        toolingClient.newBuildLaunchRequest(_) >> buildLaunchRequest
    }

    def "Job launches a Gradle build"() {
        setup:
        def job = new RunGradleBuildLaunchRequestJob(createLaunchMock())

        when:
        job.schedule()
        job.join()

        then:
        job.getResult().isOK()
        1 * buildLaunchRequest.executeAndWait()
    }

    def "Job prints its configuration"() {
        setup:
        def job = new RunGradleBuildLaunchRequestJob(createLaunchMock())

        when:
        job.schedule()
        job.join()

        then:
        job.getResult().isOK()
        1 * processStreamsProvider.createProcessStreams(null).getConfiguration().flush()
    }

    def createLaunchMock() {
        def launchConfiguration = createLaunchConfigurationMock()
        ILaunch launch = Mock(ILaunch)
        launch.getLaunchConfiguration() >> launchConfiguration
        launch
    }

}
