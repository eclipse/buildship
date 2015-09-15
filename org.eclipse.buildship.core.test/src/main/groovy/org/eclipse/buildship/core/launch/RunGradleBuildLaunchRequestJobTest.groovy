package org.eclipse.buildship.core.launch


class RunGradleBuildLaunchRequestJobTest extends BaseLaunchRequestJobTest {

    def "Job launches a Gradle build"() {
        setup:
        def job = new RunGradleBuildLaunchRequestJob(createLaunchMock())

        when:
        job.schedule()
        job.join()

        then:
        job.getResult().isOK()
        1 * buildRequest.executeAndWait()
    }

    def "Job prints its configuration"() {
        setup:
        def job = new RunGradleTestLaunchRequestJob(createTestOperationDescriptorsMock(), createRunConfigurationAttribuetesMock())

        when:
        job.schedule()
        job.join()

        then:
        job.getResult().isOK()
        1 * processStreamsProvider.createProcessStreams(null).getConfiguration().flush()
    }
}
