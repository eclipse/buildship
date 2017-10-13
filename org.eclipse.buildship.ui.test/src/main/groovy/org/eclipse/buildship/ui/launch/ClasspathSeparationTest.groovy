package org.eclipse.buildship.ui.launch

import org.junit.Rule
import org.junit.rules.ExternalResource
import spock.lang.IgnoreIf
import spock.util.environment.OperatingSystem

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationType
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.debug.core.ILaunchManager
import org.eclipse.ui.console.ConsolePlugin
import org.eclipse.ui.console.IConsole
import org.eclipse.ui.console.IConsoleListener
import org.eclipse.ui.console.IConsoleManager

import org.eclipse.buildship.ui.console.GradleConsole
import org.eclipse.buildship.ui.test.fixtures.SwtBotSpecification

// TODO (donat) enable the test when there's a Gradle snapshot release that includes the classpath separation changes
// TODO (donat) adjust the Gradle distributions used by the test methods
@IgnoreIf({ !OperatingSystem.current.isMacOs() })
class ClasspathSeparationTest extends SwtBotSpecification {

    @Rule
    TestConsoleHandler consoles = new TestConsoleHandler()

    def cleanup() {
        DebugPlugin.default.launchManager.launchConfigurations.each { it.delete() }
    }

    def "Gradle doesn't supply scope information"() {
        setup:
        importAndWait(createSampleProject('sample-project'), GradleDistribution.forVersion('3.5'))

        when:
        executeAndWait(createJavaLaunchConfiguration('sample-project', 'pkg.Main'))

        then:
        assertConsoleOutputContains('pkg.Main available')
        assertConsoleOutputContains('pkg.JunitTest available')
        assertConsoleOutputContains('org.apache.commons.io.IOUtils available')
        assertConsoleOutputContains('com.google.common.collect.ImmutableList available')
        assertConsoleOutputContains('junit.framework.Test available')
    }

    def "Launch Java application from src/main/java"() {
        setup:
        importAndWait(createSampleProject('sample-project'), GradleDistribution.forLocalInstallation(new File('/Development/git/gradle/gradle/build/distributions/gradle-4.3-20171011160000+0000')))

        when:
        executeAndWait(createJavaLaunchConfiguration('sample-project', 'pkg.Main'))

        then:
        assertConsoleOutputContains('pkg.Main available')
        assertConsoleOutputContains('pkg.JunitTest inaccessible')
        assertConsoleOutputContains('org.apache.commons.io.IOUtils available')
        assertConsoleOutputContains('com.google.common.collect.ImmutableList available')
        assertConsoleOutputContains('junit.framework.Test inaccessible')
    }

    def "Launch Java application from src/test/java"() {
        setup:
        importAndWait(createSampleProject('sample-project'), GradleDistribution.forLocalInstallation(new File('/Development/git/gradle/gradle/build/distributions/gradle-4.3-20171011160000+0000')))

        when:
        executeAndWait(createJavaLaunchConfiguration('sample-project', 'pkg.JunitTest'))

        then:
        assertConsoleOutputContains('pkg.Main available')
        assertConsoleOutputContains('pkg.JunitTest available')
        assertConsoleOutputContains('org.apache.commons.io.IOUtils inaccessible')
        assertConsoleOutputContains('com.google.common.collect.ImmutableList available')
        assertConsoleOutputContains('junit.framework.Test available')
    }

    def "Launch JUnit test with test method"() {
        setup:
        importAndWait(createSampleProject('sample-project'), GradleDistribution.forLocalInstallation(new File('/Development/git/gradle/gradle/build/distributions/gradle-4.3-20171011160000+0000')))

        when:
        executeAndWait(createJUnitLaunchConfiguration('sample-project', 'pkg.JunitTest', 'test'))

        then:
        assertConsoleOutputContains('pkg.Main available')
        assertConsoleOutputContains('pkg.JunitTest available')
        assertConsoleOutputContains('org.apache.commons.io.IOUtils inaccessible')
        assertConsoleOutputContains('com.google.common.collect.ImmutableList available')
        assertConsoleOutputContains('junit.framework.Test available')
    }

    def "Launch JUnit test with project"() {
        setup:
        importAndWait(createSampleProject('sample-project'), GradleDistribution.forLocalInstallation(new File('/Development/git/gradle/gradle/build/distributions/gradle-4.3-20171011160000+0000')))

        when:
        executeAndWait(createJUnitLaunchConfiguration('sample-project'))

        then:
        assertConsoleOutputContains('pkg.Main available')
        assertConsoleOutputContains('pkg.JunitTest available')
        assertConsoleOutputContains('org.apache.commons.io.IOUtils available')
        assertConsoleOutputContains('com.google.common.collect.ImmutableList available')
        assertConsoleOutputContains('junit.framework.Test available')
    }

    private File createSampleProject(String name) {
        dir(name) {
            file 'build.gradle', '''
                apply plugin: 'java'

                repositories {
                    jcenter()
                }

                dependencies {
                    compile 'com.google.guava:guava:18.0'
                    compileOnly 'commons-io:commons-io:1.4'
                    testCompile 'junit:junit:4.12'
                }
            '''
            dir('src/main/java/pkg') {
                file 'Main.java', '''
                    package pkg;

                    public class Main {
                        public static void main(String[] args) {
                            exists("pkg.Main");
                            exists("pkg.JunitTest");
                            exists("com.google.common.collect.ImmutableList");
                            exists("org.apache.commons.io.IOUtils");
                            exists("junit.framework.Test");
                        }

                        public static void exists(String className) {
                            try {
                                Class.forName(className);
                                System.out.println(className + " available");
                            } catch (ClassNotFoundException e) {
                                System.out.println(className + " inaccessible");
                            }
                        }
                    }
                '''
                file 'TestInMain.java', '''
                    package pkg;

                    public class TestInMain {
                        public @org.junit.Test void test() { }
                    }
                '''
            }

            dir('src/test/java/pkg') {
                file 'JunitTest.java', '''
                    package pkg;

                    public class JunitTest {

                        public static void main(String[] args) {
                            Main.main(args);
                        }

                        public @org.junit.Test void test() {
                            System.out.println("pkg.JunitTest.test executed");
                            Main.main(new String[0]);
                        }
                    }
                '''
            }
        }
    }

    private ILaunchConfiguration createJavaLaunchConfiguration(String projectName, String mainType) {
         createLaunchConfiguration('org.eclipse.jdt.launching.localJavaApplication', [
             'org.eclipse.jdt.launching.PROJECT_ATTR' : projectName,
             'org.eclipse.jdt.launching.MAIN_TYPE' : mainType
         ])
    }

    private ILaunchConfiguration createJUnitLaunchConfiguration(String projectName, String className = "", String methodName = "") {
        Map arguments = [
            'org.eclipse.jdt.junit.TEST_KIND' : 'org.eclipse.jdt.junit.loader.junit4',
            'org.eclipse.jdt.launching.PROJECT_ATTR' : projectName
        ]

        if (className || methodName) {
            arguments['org.eclipse.jdt.launching.MAIN_TYPE'] = className
            arguments['org.eclipse.jdt.junit.TESTNAME'] = methodName
        } else {
            arguments['org.eclipse.jdt.junit.CONTAINER'] = "=$projectName"
        }

        createLaunchConfiguration('org.eclipse.jdt.junit.launchconfig',arguments)
    }

    // TODO (donat) merge task launch fixtures from other classes

    private ILaunchConfiguration createLaunchConfiguration(String id, Map attributes) {
        ILaunchConfigurationWorkingCopy workingCopy = createLaunchConfig(id)
        attributes.each { String k, String v -> workingCopy.setAttribute(k, v) }
        workingCopy.doSave()
    }

    private ILaunchConfigurationWorkingCopy createLaunchConfigWorkingCopy(String id, String name) {
        ILaunchManager launchManager = DebugPlugin.default.launchManager
        ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(id)
        type.newInstance(null, launchManager.generateLaunchConfigurationName(name))
    }

    private void executeAndWait(ILaunchConfiguration configuration) {
        launchAndWait(configuration)
        consoles.waitForConsoleOutput()
    }

    private void launchAndWait(ILaunchConfiguration configuration) {
        ILaunch launch = configuration.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor(), true)
        waitFor { launch.terminated }
    }

    private void assertConsoleOutputContains(String text) {
        assert consoles.activeConsoleContent.contains(text)
    }

    // TODO (donat) extract and reuse it in other tests
    class TestConsoleHandler extends ExternalResource implements IConsoleListener {
        IConsole activeConsole

        @Override
        public void consolesAdded(IConsole[] consoles) {
            activeConsole = consoles[0]
        }

        @Override
        public void consolesRemoved(IConsole[] consoles) {
        }

        @Override
        protected void before() throws Throwable {
            ConsolePlugin.default.consoleManager.addConsoleListener(this)
        }

        @Override
        protected void after() {
            ConsolePlugin.default.consoleManager.removeConsoleListener(this)
            removeConsoles()
        }

        protected void waitForConsoleOutput() {
            ClasspathSeparationTest.this.waitFor {
                activeConsole != null && activeConsole.partitioner.pendingPartitions.empty
            }
        }

        public String getActiveConsoleContent() {
            activeConsole.document.get().trim()
        }

        protected void removeConsoles() {
            IConsoleManager consoleManager = ConsolePlugin.default.consoleManager
            List<IConsole> consoles = consoleManager.consoles.findAll { console -> !(console instanceof GradleConsole) || console.isCloseable()}
            consoleManager.removeConsoles(consoles as IConsole[])
        }
    }
}
