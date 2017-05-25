package org.eclipse.buildship.core.launch

import org.gradle.tooling.events.ProgressEvent
import org.gradle.tooling.events.ProgressListener
import org.gradle.tooling.events.test.TestOperationDescriptor

import com.google.common.collect.Lists

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.core.resources.IProject
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchConfiguration

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.RunConfiguration
import org.eclipse.buildship.core.event.Event
import org.eclipse.buildship.core.event.EventListener
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification;
import org.eclipse.buildship.core.util.gradle.GradleDistributionSerializer

class RunGradleTestLaunchRequestJobComplexTest extends ProjectSynchronizationSpecification {

    def "Can execute a test launch using test operation descriptors"() {
        setup:
        // import the project
        importAndWait(sampleProject())
        IProject project = findProject('sample-project')

        // setup project collection
        List descriptors = Lists.newCopyOnWriteArrayList()
        collectTestDescriptorsInto(descriptors)

        // execute a test build to obtain test operation descriptors
        GradleRunConfigurationAttributes attributes = new GradleRunConfigurationAttributes(
            ['clean', 'test'],
            project.getLocation().toFile().absolutePath,
            GradleDistributionSerializer.INSTANCE.serializeToString(GradleDistribution.fromBuild()),
            "",
            null,
            [],
            [],
            true,
            true,
            false,
            false,
            false)
        ILaunchConfiguration launchConfiguration = CorePlugin.gradleLaunchConfigurationManager().getOrCreateRunConfiguration(attributes)
        RunConfiguration runConfig = CorePlugin.configurationManager().loadRunConfiguration(launchConfiguration)
        executeCleanTestAndWait(runConfig)

        when:
        // execute only the tests containing the word 'test1'
        def testJob = new RunGradleTestLaunchRequestJob(descriptors.findAll { it.name.contains('test1') }, runConfig)
        descriptors.clear()
        testJob.schedule()
        testJob.join()

        then:
        // the test descriptors from the second test run should contain only 'test1' tests
        !descriptors.findAll { it.name.contains('test1') }.isEmpty()
        descriptors.findAll { it.name.contains('test2') }.isEmpty()
    }

    private def sampleProject() {
        dir('sample-project') {
            file 'build.gradle',  '''
                apply plugin: "java"
                repositories { jcenter() }
                dependencies { testCompile 'junit:junit:4.10' }
            '''
            dir('src/test/java') {
                file 'SampleTest.java', '''
                    import org.junit.Test;
                    import static org.junit.Assert.*;
                    public class SampleTest {
                        @Test public void test1() { assertTrue(true); }
                        @Test public void test2() { assertTrue(true); }
                    }
                '''
            }

        }
    }

    private def collectTestDescriptorsInto(List descriptors) {
        ProgressListener progressListener = new ProgressListener() {
            public void statusChanged(ProgressEvent event) {
                if (event.descriptor instanceof TestOperationDescriptor) {
                    descriptors.add(event.getDescriptor())
                }
            }
        }
        CorePlugin.listenerRegistry().addEventListener(new EventListener() {
            void onEvent(Event event) {
                if (event instanceof ExecuteLaunchRequestEvent) {
                    ((ExecuteLaunchRequestEvent) event).operation.addProgressListener(progressListener)
                }
            }
        })
    }

    private def executeCleanTestAndWait(RunConfiguration runConfig) {
        GradleRunConfigurationAttributes attributes = new GradleRunConfigurationAttributes(
            runConfig.tasks,
            runConfig.buildConfiguration.rootProjectDirectory.absolutePath,
            GradleDistributionSerializer.INSTANCE.serializeToString(runConfig.buildConfiguration.gradleDistribution),
            runConfig.gradleUserHome,
            runConfig.javaHome,
            runConfig.jvmArguments,
            runConfig.arguments,
            runConfig.showExecutionView,
            runConfig.showConsoleView,
            runConfig.buildConfiguration.overrideWorkspaceSettings,
            runConfig.buildConfiguration.offlineMode,
            runConfig.buildConfiguration.buildScansEnabled)
        ILaunch launch = Mock()
        launch.getLaunchConfiguration() >> CorePlugin.gradleLaunchConfigurationManager().getOrCreateRunConfiguration(attributes)
        RunGradleBuildLaunchRequestJob job = new RunGradleBuildLaunchRequestJob(launch)
        job.schedule()
        job.join()
    }
}
