package org.eclipse.buildship.core.launch

import java.util.List

import org.gradle.tooling.events.ProgressListener
import org.gradle.tooling.events.test.TestOperationDescriptor
import org.gradle.tooling.events.ProgressEvent

import com.google.common.collect.Lists

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.core.resources.IProject
import org.eclipse.debug.core.ILaunch

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.ProjectConfiguration
import org.eclipse.buildship.core.event.Event
import org.eclipse.buildship.core.test.fixtures.ProjectImportSpecification
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.event.EventListener
import org.eclipse.buildship.core.launch.ExecuteLaunchRequestEvent


class RunGradleTestLaunchRequestJobTest extends ProjectImportSpecification {

    def "Can execute a test launch using test operation descriptors"() {
        setup:
        // import the project
        executeProjectImportAndWait(sampleProject())
        IProject project = CorePlugin.workspaceOperations().findProjectByName('sample-project').get()

        // setup project collection
        List descriptors = Lists.newCopyOnWriteArrayList()
        collectTestDescriptorsInto(descriptors)

        // execute a test build to obtain test operation descriptors
        ProjectConfiguration configuration = CorePlugin.projectConfigurationManager().readProjectConfiguration(project)
        GradleRunConfigurationAttributes attributes = GradleRunConfigurationAttributes.with(['clean', 'test'] as List,
                                                                                            configuration.projectDir.absolutePath,
                                                                                            GradleDistribution.fromBuild(),
                                                                                            null,
                                                                                            null,
                                                                                            [] as List,
                                                                                            [] as List,
                                                                                            false,
                                                                                            false)
        executeCleanTestAndWait(attributes)

        when:
        // execute only the tests containing the word 'test1'
        def testJob = new RunGradleTestLaunchRequestJob(descriptors.findAll{ it.name.contains('test1') }, attributes)
        descriptors.clear()
        testJob.schedule()
        testJob.join()

        then:
        // the test descriptors from the second test run should contain only 'test1' tests
        !descriptors.findAll{ it.name.contains('test1') }.isEmpty()
        descriptors.findAll{ it.name.contains('test2') }.isEmpty()
    }

    private def sampleProject() {
        file('sample-project', 'build.gradle') <<
                '''apply plugin: "java"
           repositories { jcenter() }
           dependencies { testCompile 'junit:junit:4.10' }
        '''
        file('sample-project', 'settings.gradle') << ''
        file('sample-project', 'src', 'test', 'java', 'SampleTest.java') <<
                '''import org.junit.Test;
           import static org.junit.Assert.*;
           public class SampleTest {
               @Test public void test1() { assertTrue(true); }
               @Test public void test2() { assertTrue(true); }
           }
        '''
        folder('sample-project')
    }

    private def collectTestDescriptorsInto(List descriptors) {
        ProgressListener progressListener = new ProgressListener() {
            public void statusChanged(ProgressEvent event) {
                if (event.descriptor instanceof TestOperationDescriptor) {
                    println event.getDescriptor()
                    descriptors.add(event.getDescriptor())
                }
            }
        }
        CorePlugin.listenerRegistry().addEventListener(new EventListener() {
            void onEvent(Event event) {
                if (event instanceof ExecuteLaunchRequestEvent) {
                    ((ExecuteLaunchRequestEvent)event).request.addTypedProgressListeners(progressListener)
                }
            }
        })
    }

    private def executeCleanTestAndWait(GradleRunConfigurationAttributes attributes) {
        ILaunch launch = Mock()
        launch.getLaunchConfiguration() >> CorePlugin.gradleLaunchConfigurationManager().getOrCreateRunConfiguration(attributes)
        RunGradleBuildLaunchRequestJob job = new RunGradleBuildLaunchRequestJob(launch)
        job.schedule()
        job.join()
    }
}
