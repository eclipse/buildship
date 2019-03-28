package org.eclipse.buildship.core.internal.workspace

import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.console.ProcessStreamsProvider
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.internal.test.fixtures.TestProcessStreamProvider

class RunEclipseSynchronizationTasksTest extends ProjectSynchronizationSpecification {

    def setup() {
        environment.registerService(ProcessStreamsProvider, new TestProcessStreamProvider() {})
    }

    String getSyncConsoleOutput() {
        TestProcessStreamProvider testStreams = CorePlugin.processStreamsProvider()
        testStreams.backroundStream.out
    }

    def "executes tasks from eclipse plugin configuration"() {
        setup:
        def location = dir('run-eclipse-sync-task-1') {
            file 'build.gradle', '''
                plugins {
                    id 'eclipse'
                }

                task foo {
                    doLast {
                        println 'foo'
                    }
                }

                eclipse {
                    synchronizationTasks foo
                }
             '''
         }

         when:
         importAndWait(location)

         then:
         findProject('run-eclipse-sync-task-1')
         syncConsoleOutput.contains "Task :foo"
    }
}
