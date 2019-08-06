package org.eclipse.buildship.core.internal.launch


import org.eclipse.core.runtime.jobs.Job
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.jdt.core.IType

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.configuration.RunConfiguration

class RunGradleTestLaunchRequestJobTest extends BaseLaunchRequestJobTest {

    File projectDir

    def setup() {
        projectDir = dir('java-launch-config') {
            file 'build.gradle', """
                apply plugin: 'java'
                ${jcenterRepositoryBlock}
                dependencies.testCompile 'junit:junit:4.12'
            """
            dir('src/test/java').mkdirs()
            file 'src/test/java/MyTest.java', """
                public class MyTest {
                    public @org.junit.Test void test() { org.junit.Assert.assertTrue(true); }
                }
            """
        }
    }

    def "Job launches a Gradle test"() {
        setup:
        def job = new RunGradleJvmTestLaunchRequestJob(createRunConfigurationMock(), "run")

        when:
        job.schedule()
        job.join()

        then:
        job.getResult().isOK()
        buildOutput.contains ':test'
        buildOutput.contains 'BUILD SUCCESSFUL'
    }

    def "Job prints its configuration"() {
        setup:
        def job = new RunGradleJvmTestLaunchRequestJob(createRunConfigurationMock(), "run")

        when:
        job.schedule()
        job.join()

        then:
        job.getResult().isOK()
        buildConfig.contains 'Working Directory'
        buildConfig.contains 'Tests: MyTest'
    }

    def createRunConfigurationMock() {
        ILaunchConfiguration configuration = createLaunchConfiguration(projectDir)
        configuration.getAttribute('tests', _) >> ['MyTest']
        configuration
    }
}
