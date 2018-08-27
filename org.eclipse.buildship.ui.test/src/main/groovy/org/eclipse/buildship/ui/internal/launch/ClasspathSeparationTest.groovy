package org.eclipse.buildship.ui.internal.launch

import org.gradle.api.JavaVersion
import spock.lang.IgnoreIf

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.debug.core.ILaunchManager

import org.eclipse.buildship.core.internal.util.gradle.GradleDistribution
import org.eclipse.buildship.ui.internal.test.fixtures.SwtBotSpecification

class ClasspathSeparationTest extends SwtBotSpecification {

    def cleanup() {
        DebugPlugin.default.launchManager.launchConfigurations.each { it.delete() }
    }

    @IgnoreIf({ JavaVersion.current().isJava9Compatible() })
    def "All dependencies are available when Gradle doesn't supply scope information"() {
        setup:
        importAndWait(createSampleProject('sample-project'), GradleDistribution.forVersion('3.5'))

        when:
        launchAndWait(createJavaLaunchConfiguration('sample-project', 'pkg.Main'))

        then:
        assertConsoleOutputContains('pkg.Main available')
        assertConsoleOutputContains('pkg.JunitTest available')
        assertConsoleOutputContains('org.apache.commons.io.IOUtils available')
        assertConsoleOutputContains('com.google.common.collect.ImmutableList available')
        assertConsoleOutputContains('junit.framework.Test available')
        assertConsoleOutputContains('main.txt available')
        assertConsoleOutputContains('test.txt available')
    }

    def "All dependencies are available when target source folders doesn't supply scope information"() {
        setup:
        File projectDir = createSampleProject('sample-project')

        importAndWait(projectDir, GradleDistribution.forVersion('4.4'))

        when:
        launchAndWait(createJavaLaunchConfiguration('sample-project', 'pkg.CustomMain'))

        then:
        assertConsoleOutputContains('pkg.Main available')
        assertConsoleOutputContains('pkg.JunitTest available')
        assertConsoleOutputContains('org.apache.commons.io.IOUtils available')
        assertConsoleOutputContains('com.google.common.collect.ImmutableList available')
        assertConsoleOutputContains('junit.framework.Test available')
        assertConsoleOutputContains('main.txt available')
        assertConsoleOutputContains('test.txt available')
    }

    def "Source folder is included in classpath if it doesn't supply scope information"() {
        setup:
        File projectDir = createSampleProject('sample-project')

        importAndWait(projectDir, GradleDistribution.forVersion('4.4'))

        when:
        launchAndWait(createJavaLaunchConfiguration('sample-project', 'pkg.CustomMain'))

        then:
        assertConsoleOutputContains('pkg.CustomMain available')
        assertConsoleOutputContains('main.txt available')
        assertConsoleOutputContains('test.txt available')
    }

    def "Only main dependencies are available when Java application launched from src/main/java folder"() {
        setup:
        importAndWait(createSampleProject('sample-project'), GradleDistribution.forVersion('4.4'))

        when:
        launchAndWait(createJavaLaunchConfiguration('sample-project', 'pkg.Main'))

        then:
        assertConsoleOutputContains('pkg.Main available')
        assertConsoleOutputContains('pkg.JunitTest inaccessible')
        assertConsoleOutputContains('org.apache.commons.io.IOUtils inaccessible')
        assertConsoleOutputContains('com.google.common.collect.ImmutableList available')
        assertConsoleOutputContains('junit.framework.Test inaccessible')
        assertConsoleOutputContains('main.txt available')
        assertConsoleOutputContains('test.txt inaccessible')
    }

    def "Main and test dependencies are available when Java application launched from src/test/java folder"() {
        setup:
        importAndWait(createSampleProject('sample-project'), GradleDistribution.forVersion('4.4'))

        when:
        launchAndWait(createJavaLaunchConfiguration('sample-project', 'pkg.JunitTest'))

        then:
        assertConsoleOutputContains('pkg.Main available')
        assertConsoleOutputContains('pkg.JunitTest available')
        assertConsoleOutputContains('org.apache.commons.io.IOUtils inaccessible')
        assertConsoleOutputContains('com.google.common.collect.ImmutableList available')
        assertConsoleOutputContains('junit.framework.Test available')
        assertConsoleOutputContains('main.txt available')
        assertConsoleOutputContains('test.txt available')
    }

    def "Main and test dependencies are available when JUnit test method executed"() {
        setup:
        importAndWait(createSampleProject('sample-project'), GradleDistribution.forVersion('4.4'))

        when:
        launchAndWait(createJUnitLaunchConfiguration('sample-project', 'pkg.JunitTest', 'test'))

        then:
        assertConsoleOutputContains('pkg.Main available')
        assertConsoleOutputContains('pkg.JunitTest available')
        assertConsoleOutputContains('org.apache.commons.io.IOUtils inaccessible')
        assertConsoleOutputContains('com.google.common.collect.ImmutableList available')
        assertConsoleOutputContains('junit.framework.Test available')
        assertConsoleOutputContains('main.txt available')
        assertConsoleOutputContains('test.txt available')
    }

    def "Main and test dependencies are available when JUnit test project executedt"() {
        setup:
        importAndWait(createSampleProject('sample-project'), GradleDistribution.forVersion('4.4'))

        when:
        launchAndWait(createJUnitLaunchConfiguration('sample-project'))

        then:
        assertConsoleOutputContains('pkg.Main available')
        assertConsoleOutputContains('pkg.JunitTest available')
        assertConsoleOutputContains('org.apache.commons.io.IOUtils inaccessible')
        assertConsoleOutputContains('com.google.common.collect.ImmutableList available')
        assertConsoleOutputContains('junit.framework.Test available')
        assertConsoleOutputContains('main.txt available')
        assertConsoleOutputContains('test.txt available')
    }

    private File createSampleProject(String name) {
        dir(name) {
            file 'settings.gradle', """
                include ':resource-library'
            """
            file 'build.gradle', """
                import org.gradle.plugins.ide.eclipse.model.SourceFolder

                apply plugin: 'java'
                apply plugin: 'eclipse'

                ${jcenterRepositoryBlock}

                dependencies {
                    compile project(':resource-library')
                    compile 'com.google.guava:guava:18.0'
                    compileOnly 'commons-io:commons-io:1.4'
                    testCompile 'junit:junit:4.12'
                }

                eclipse {
                    classpath {
                        file {
                            whenMerged {
                                entries += new SourceFolder('src/custom', 'customOutputFolder')
                            }
                        }
                    }
                }
            """
            dir('src/main/java/pkg') {
                file 'Main.java', '''
                    package pkg;

                    public class Main {
                        public static void main(String[] args) {
                            exists("pkg.Main");
                            exists("pkg.CustomMain");
                            exists("pkg.JunitTest");
                            exists("com.google.common.collect.ImmutableList");
                            exists("org.apache.commons.io.IOUtils");
                            exists("junit.framework.Test");
                            resourceExists("main.txt");
                            resourceExists("test.txt");
                        }

                        public static void exists(String className) {
                            try {
                                Class.forName(className);
                                System.out.println(className + " available");
                            } catch (ClassNotFoundException e) {
                                System.out.println(className + " inaccessible");
                            }
                        }

                        public static void resourceExists(String resourceName) {
                            boolean available = Main.class.getClassLoader().getResource(resourceName) != null;
                            System.out.println(resourceName + (available ? " available" : " inaccessible"));
                        }
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

            dir('src/custom/pkg') {
                file 'CustomMain.java', '''
                    package pkg;

                    public class CustomMain {

                        public static void main(String[] args) {
                            Main.main(args);
                        }
                    }
                '''
            }
            dir('resource-library') {
                file 'build.gradle', '''
                apply plugin: 'java'
                '''
                dir('src/main/resources') {
                    file 'main.txt', ''
                }
                dir('src/test/resources') {
                    file 'test.txt', ''
                }
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

    private ILaunchConfiguration createLaunchConfiguration(String id, Map attributes) {
        ILaunchConfigurationWorkingCopy workingCopy = createLaunchConfig(id)
        attributes.each { String k, String v -> workingCopy.setAttribute(k, v) }
        workingCopy.doSave()
    }

    private void launchAndWait(ILaunchConfiguration configuration) {
        ILaunch launch = configuration.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor(), true)
        waitFor { launch.terminated }
        waitFor { !consoles.activeConsoleContent.isEmpty() }
    }

    private void assertConsoleOutputContains(String text) {
        assert consoles.activeConsoleContent.contains(text)
    }
}
