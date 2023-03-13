/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core

import java.util.function.Function

import org.gradle.tooling.CancellationTokenSource
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import spock.lang.Ignore
import spock.lang.Issue

import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job

import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class GradleBuildConnectionConcurrencyTest extends ProjectSynchronizationSpecification {

    @Ignore("TODO create Buildship issue")
    /* > Task :nothing UP-TO-DATE

        BUILD SUCCESSFUL in 39ms
        ***WARNING: Display must be created on main thread due to Cocoa restrictions. Use vmarg -XstartOnFirstThread

        Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler in thread "Worker-2: Java indexing... "

        Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler in thread "Active Thread: Equinox Container: bbf985fd-e145-47d0-ae6f-e5354e34c7d2"
     */
    @Issue("https://github.com/eclipse/buildship/issues/818")
    def "Can use continuous task execution while editing workspace files"() {
        setup:
        File mainJava = null
        File location = dir('GradleBuildConnectionConcurrencyTest') {
            file 'build.gradle', """
                plugins {
                    id 'java'
                }
            """
            dir ('src/main/java') { mainJava = file 'Main.java', """
                    public class Main { }
                """ }
        }
        importAndWait(location)

        when:
        ExecuteJarTaskInContinuousModeJob job = new ExecuteJarTaskInContinuousModeJob(location)
        job.schedule()
        waitFor { job.out.toString().count("BUILD SUCCESSFUL") == 1 }
        IFile file = findProject('GradleBuildConnectionConcurrencyTest').getFile('src/main/java/Main.java')
        file.setContents(new ByteArrayInputStream("public class Main { int i = 0; }".bytes),  IResource.FORCE, new NullProgressMonitor())

        then:
        waitFor(10000, 500) { job.out.toString().count("BUILD SUCCESSFUL") == 2 }

        cleanup:
        job.cleanup()
    }

    class ExecuteJarTaskInContinuousModeJob extends Job {
        private GradleBuild gradleBuild
        private CancellationTokenSource cancellation
        private OutputStream out = new ByteArrayOutputStream()
        private OutputStream err = new ByteArrayOutputStream()

        ExecuteJarTaskInContinuousModeJob(File location) {
            super('ExecuteJarTaskInContinuousModeJob')
            BuildConfiguration buildConfiguration = BuildConfiguration.forRootProjectDirectory(location).build()
            this.gradleBuild = GradleCore.workspace.createBuild(buildConfiguration)
            this.cancellation = GradleConnector.newCancellationTokenSource()
        }

        protected IStatus run(IProgressMonitor monitor) {
            Function action = { ProjectConnection c -> c.newBuild().forTasks("jar").addArguments("--continuous").withCancellationToken(cancellation.token()).setStandardOutput(out).setStandardError(err).setStandardInput(mockedInput()).run() }
            gradleBuild.withConnection(action, monitor)
            Status.OK_STATUS
        }

        protected void canceling() {
            cancellation.cancel()
        }

        public void cleanup() {
            canceling();
        }
    }

    private InputStream mockedInput() {
        InputStream input = Mock(InputStream)
        input.read(*_) >> 1
        input.available() >> true
        input
    }
}
