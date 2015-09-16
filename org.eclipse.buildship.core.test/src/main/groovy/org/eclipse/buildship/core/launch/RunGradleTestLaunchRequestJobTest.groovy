package org.eclipse.buildship.core.launch

import com.gradleware.tooling.toolingclient.TestLaunchRequest
import org.gradle.tooling.events.test.TestOperationDescriptor

class RunGradleTestLaunchRequestJobTest extends BaseLaunchRequestJobTest {

    TestLaunchRequest testLaunchRequest

    def setup(){
        testLaunchRequest = Mock(TestLaunchRequest)
        toolingClient.newTestLaunchRequest(_) >> testLaunchRequest
    }

    def "Job launches a Gradle test"() {
        setup:
        def job = new RunGradleTestLaunchRequestJob(createTestOperationDescriptorsMock(), createRunConfigurationAttributesMock())

        when:
        job.schedule()
        job.join()

        then:
        job.getResult().isOK()
        1 * testLaunchRequest.executeAndWait()
    }

    def "Job prints its configuration"() {
        setup:
        def job = new RunGradleTestLaunchRequestJob(createTestOperationDescriptorsMock(), createRunConfigurationAttributesMock())

        when:
        job.schedule()
        job.join()

        then:
        job.getResult().isOK()
        1 * processStreamsProvider.createProcessStreams(null).getConfiguration().flush()
    }

    GradleRunConfigurationAttributes createRunConfigurationAttributesMock() {
        def launchConfiguration = createLaunchConfigurationMock()
        GradleRunConfigurationAttributes.from(launchConfiguration)
    }

    def createTestOperationDescriptorsMock() {
        TestOperationDescriptor descriptor = Mock(TestOperationDescriptor)
        descriptor.getName() >> 'testName'
        descriptor.getDisplayName() >> 'display name'
        [descriptor]
    }

}
