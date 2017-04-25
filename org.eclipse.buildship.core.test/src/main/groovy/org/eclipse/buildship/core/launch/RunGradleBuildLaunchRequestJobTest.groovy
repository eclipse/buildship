package org.eclipse.buildship.core.launch

import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchConfiguration

class RunGradleBuildLaunchRequestJobTest extends BaseLaunchRequestJobTest {

    File projectDir

    void setup() {
        projectDir = dir('java-launch-config') {
            file 'build.gradle', "apply plugin: 'java'"
        }
    }

    def "Job launches a Gradle build"() {
        setup:
        def job = new RunGradleBuildLaunchRequestJob(createLaunch(projectDir))

        when:
        job.schedule()
        job.join()

        then:
            job.getResult().isOK()
        buildOutput.contains 'BUILD SUCCESSFUL'
    }

    def "Job prints its configuration"() {
        setup:
        def job = new RunGradleBuildLaunchRequestJob(createLaunch(projectDir))

        when:
        job.schedule()
        job.join()

        then:
        job.getResult().isOK()
        buildConfig.contains 'Working Directory'
        buildConfig.contains 'Gradle Tasks: clean build'
    }

    ILaunch createLaunch(File projectDir) {
        ILaunchConfiguration launchConfiguration = createLaunchConfiguration(projectDir)
        ILaunch launch = Mock(ILaunch)
        launch.launchConfiguration >> launchConfiguration
        launch
    }

}
