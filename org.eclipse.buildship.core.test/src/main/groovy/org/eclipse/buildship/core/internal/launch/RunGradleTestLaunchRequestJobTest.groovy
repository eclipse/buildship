package org.eclipse.buildship.core.internal.launch


import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.IType

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.configuration.LaunchConfiguration

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
        // TODO reimplement
        def job = null

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
        // TODO reimplement
        def job = null

        when:
        job.schedule()
        job.join()

        then:
        job.getResult().isOK()
        buildConfig.contains 'Working Directory'
        buildConfig.contains 'Tests: MyTest'
    }

    LaunchConfiguration createRunConfigurationMock() {
        CorePlugin.configurationManager().loadRunConfiguration(createLaunchConfiguration(projectDir))
    }
}
