package org.eclipse.buildship.core.launch

import org.gradle.tooling.events.test.TestOperationDescriptor

class RunGradleTestLaunchRequestJobTest extends BaseLaunchRequestJobTest {

    def "Job launches a Gradle test"() {
        setup:
        def job = new RunGradleTestLaunchRequestJob(createTestOperationDescriptorsMock(), createRunConfigurationAttributesMock())

        when:
        job.schedule()
        job.join()

        then:
        job.getResult().isOK()
        1 * testRequest.executeAndWait()
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

    def createTestOperationDescriptorsMock() {
        TestOperationDescriptor descriptor = Mock(TestOperationDescriptor)
        descriptor.getName() >> 'testName'
        descriptor.getDisplayName() >> 'display name'
        [descriptor]
    }

}
