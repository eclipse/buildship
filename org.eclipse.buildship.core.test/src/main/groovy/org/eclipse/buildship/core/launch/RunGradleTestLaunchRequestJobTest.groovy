package org.eclipse.buildship.core.launch

import com.gradleware.tooling.toolingclient.TestLaunchRequest

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.RunConfiguration

import org.gradle.tooling.events.test.TestOperationDescriptor

class RunGradleTestLaunchRequestJobTest extends BaseLaunchRequestJobTest {

    TestLaunchRequest testLaunchRequest

    def setup(){
        testLaunchRequest = Mock(TestLaunchRequest)
        toolingClient.newTestLaunchRequest(_) >> testLaunchRequest
    }

    def "Job launches a Gradle test"() {
        setup:
        def job = new RunGradleTestLaunchRequestJob(createTestOperationDescriptorsMock(), createRunConfigurationMock())

        when:
        job.schedule()
        job.join()

        then:
        job.getResult().isOK()
        1 * testLaunchRequest.executeAndWait()
    }

    def "Job prints its configuration"() {
        setup:
        def job = new RunGradleTestLaunchRequestJob(createTestOperationDescriptorsMock(), createRunConfigurationMock())

        when:
        job.schedule()
        job.join()

        then:
        job.getResult().isOK()
        1 * processStreamsProvider.createProcessStreams(null).getConfiguration().flush()
    }

    RunConfiguration createRunConfigurationMock() {
        def attributes = GradleRunConfigurationAttributes.from(createLaunchConfigurationMock())
        CorePlugin.configurationManager().loadRunConfiguration(attributes)
    }

    def createTestOperationDescriptorsMock() {
        TestOperationDescriptor descriptor = Mock(TestOperationDescriptor)
        descriptor.getName() >> 'testName'
        descriptor.getDisplayName() >> 'display name'
        [descriptor]
    }

}
