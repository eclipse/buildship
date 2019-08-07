package org.eclipse.buildship.core.internal.launch


import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchListener

import org.eclipse.buildship.core.GradleDistribution

class RunGradleTestLaunchRequestJobTest extends BaseLaunchRequestJobTest {

    File projectDir
    File subDir

    def setup() {
        projectDir = dir('java-launch-config') {
            file 'build.gradle', """
                allprojects {
                    apply plugin: 'java'
                    ${jcenterRepositoryBlock}
                    dependencies.testCompile 'junit:junit:4.12'

                    tasks.withType(Test) {
                        onOutput { descriptor, event ->
                            logger.lifecycle("\$descriptor: \$event.message")
                        }
                    }
                }
            """
            file 'settings.gradle', 'include "sub"'
            dir('sub/src/test/java').mkdirs()
            file 'sub/src/test/java/MyTest1.java', """
                public class MyTest1 {
                    public @org.junit.Test void test1_1() { System.out.println("test1_1"); org.junit.Assert.assertTrue(true); }
                    public @org.junit.Test void test1_2() { System.out.println("test1_2"); org.junit.Assert.assertTrue(true); }
                    public @org.junit.Test void test1_3() { System.out.println("test1_3"); org.junit.Assert.assertTrue(true); }
                }
            """
            file 'sub/src/test/java/MyTest2.java', """
                public class MyTest2 {
                    public @org.junit.Test void test2_1() { System.out.println("test2_1"); org.junit.Assert.assertTrue(true); }
                    public @org.junit.Test void test2_2() { System.out.println("test2_2"); org.junit.Assert.assertTrue(true); }
                    public @org.junit.Test void test2_3() { System.out.println("test2_3"); org.junit.Assert.assertTrue(true); }
                }
            """
            file 'sub/src/test/java/MyTest3.java', """
                public class MyTest3 {
                    public @org.junit.Test void test3_1() { System.out.println("test3_1"); org.junit.Assert.assertTrue(true); }
                    public @org.junit.Test void test3_2() { System.out.println("test3_2"); org.junit.Assert.assertTrue(true); }
                    public @org.junit.Test void test3_3() {
                        System.out.println("test3_3");
                        org.junit.Assert.assertTrue(true);
                    }
                }
            """
        }
        importAndWait(projectDir)
        subDir = new File(projectDir, 'sub')
    }

    def "Job launches a Gradle test"() {
        when:
        IStatus result = scheduleTestLaunchAndWait('MyTest1')

        then:
        result.isOK()
        buildOutput.contains ':test'
        buildOutput.contains 'BUILD SUCCESSFUL'
    }

    def "Job prints its configuration"() {
        when:
        scheduleTestLaunchAndWait('MyTest1')

        then:
        buildConfig.contains 'Working Directory'
        buildConfig.contains 'Tests: MyTest1'
    }

    def "Executes a single test class"() {
        when:
        scheduleTestLaunchAndWait('MyTest1')

        then:
        assertTestExecuted('MyTest1#test1_1')
        assertTestExecuted('MyTest1#test1_2')
        assertTestExecuted('MyTest1#test1_3')
        assertTestNotExecuted('MyTest2#test2_1')
        assertTestNotExecuted('MyTest2#test2_2')
        assertTestNotExecuted('MyTest2#test2_3')
        assertTestNotExecuted('MyTest3#test3_1')
        assertTestNotExecuted('MyTest3#test3_2')
        assertTestNotExecuted('MyTest3#test3_3')
    }

    def "Executes a single test method"() {
        when:
        scheduleTestLaunchAndWait('MyTest1#test1_1')

        then:
        assertTestExecuted('MyTest1#test1_1')
        assertTestNotExecuted('MyTest1#test1_2')
        assertTestNotExecuted('MyTest1#test1_3')
        assertTestNotExecuted('MyTest2#test2_1')
        assertTestNotExecuted('MyTest2#test2_2')
        assertTestNotExecuted('MyTest2#test2_3')
        assertTestNotExecuted('MyTest3#test3_1')
        assertTestNotExecuted('MyTest3#test3_2')
        assertTestNotExecuted('MyTest3#test3_3')
    }

    def "Executes multiple test classes"() {
        when:
        scheduleTestLaunchAndWait('MyTest1', 'MyTest2')

        then:
        assertTestExecuted('MyTest1#test1_1')
        assertTestExecuted('MyTest1#test1_2')
        assertTestExecuted('MyTest1#test1_3')
        assertTestExecuted('MyTest2#test2_1')
        assertTestExecuted('MyTest2#test2_2')
        assertTestExecuted('MyTest2#test2_3')
        assertTestNotExecuted('MyTest3#test3_1')
        assertTestNotExecuted('MyTest3#test3_2')
        assertTestNotExecuted('MyTest3#test3_3')
    }

    def "Executes multiple test methods"() {
        when:
        scheduleTestLaunchAndWait('MyTest1#test1_1', 'MyTest1#test1_2')

        then:
        assertTestExecuted('MyTest1#test1_1')
        assertTestExecuted('MyTest1#test1_2')
        assertTestNotExecuted('MyTest1#test1_3')
        assertTestNotExecuted('MyTest2#test2_1')
        assertTestNotExecuted('MyTest2#test2_2')
        assertTestNotExecuted('MyTest2#test2_3')
        assertTestNotExecuted('MyTest3#test3_1')
        assertTestNotExecuted('MyTest3#test3_2')
        assertTestNotExecuted('MyTest3#test3_3')
    }

    def "Executes test classes and methods at the same time"() {
        when:
        scheduleTestLaunchAndWait('MyTest1', 'MyTest2#test2_2')

        then:
        assertTestExecuted('MyTest1#test1_1')
        assertTestExecuted('MyTest1#test1_2')
        assertTestExecuted('MyTest1#test1_3')
        assertTestNotExecuted('MyTest2#test2_1')
        assertTestExecuted('MyTest2#test2_2')
        assertTestNotExecuted('MyTest2#test2_3')
        assertTestNotExecuted('MyTest3#test3_1')
        assertTestNotExecuted('MyTest3#test3_2')
        assertTestNotExecuted('MyTest3#test3_3')
    }

    def "Cannnot do test debugging with old Gradle version"() {
        setup:
        importAndWait(projectDir, GradleDistribution.forVersion('5.5'))
        scheduleTestDebugAndWait('MyTest1')

        expect:
        buildConfig.contains('[WARN] Current Gradle distribution does not support test debugging. Upgrade to 5.6 to make use of this feature')
    }

    def "Can debug tests"() {
        setup:
        DebugListener debugListener = new DebugListener()
        DebugPlugin.default.launchManager.addLaunchListener(debugListener)

        when:
        scheduleTestDebugAndWait('MyTest1')

        then:
        debugListener.debugLaunches == 1

        cleanup:
        DebugPlugin.default.launchManager.removeLaunchListener(debugListener)
    }

    def "Warning message is printed if target project is closed"() {
        setup:
        DebugListener debugListener = new DebugListener()
        findProject(subDir.name).close(new NullProgressMonitor())

        when:
        scheduleTestDebugAndWait('MyTest1')

        then:
        buildConfig.contains("[WARN] Cannot initate debugging as no accessible project present at $subDir.canonicalPath")
        debugListener.debugLaunches == 0
    }

    private void assertTestExecuted(String test) {
        assert testExecuted(test.split('#'))
    }

    private void assertTestNotExecuted(String test) {
          assert !testExecuted(test.split('#'))
    }

    private boolean testExecuted(String[] parts) {
        // output format: Test methodName(className): testOutput
        return buildOutput.contains(parts.length > 1 ? "${parts[1]}(${parts[0]}):" : "(${parts[0]}):")
    }

    private IStatus scheduleTestLaunchAndWait(String... tests) {
        ILaunchConfiguration configuration = testRunConfiguration(tests)
        def job = new RunGradleJvmTestLaunchRequestJob(configuration, "run")
        job.schedule()
        job.join()
        job.result
    }

    private IStatus scheduleTestDebugAndWait(String... tests) {
        ILaunchConfiguration configuration = testRunConfiguration(tests)
        def job = new RunGradleJvmTestLaunchRequestJob(configuration, "debug")
        job.schedule()
        job.join()
        job.result
    }

    def testRunConfiguration(String... tests = ['MyTest']) {
        ILaunchConfiguration configuration = createLaunchConfiguration(subDir)
        configuration.getAttribute('tests', _) >> tests

        configuration
    }

    class DebugListener implements ILaunchListener {
        def debugLaunches = 0
        void launchAdded(ILaunch launch) {
            if (launch.launchConfiguration.type.identifier == 'org.eclipse.jdt.launching.remoteJavaApplication') {
                debugLaunches++
            }
        }
        void launchChanged(ILaunch launch) {}
        void launchRemoved(ILaunch launch) {}
    }
}
