/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.launch

import spock.lang.Ignore
import spock.lang.Timeout
import spock.lang.Unroll

import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchConfiguration

import org.eclipse.buildship.core.GradleDistribution

class RunGradleBuildLaunchRequestJobTest extends BaseLaunchRequestJobTest {

    File projectDir

    def setup() {
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

    @Unroll
    def "Can launch task with Gradle #distribution.versionersion"(GradleDistribution distribution) {
        setup:
        def job = new RunGradleBuildLaunchRequestJob(createLaunch(projectDir, distribution))

        when:
        job.schedule()
        job.join()

        then:
        job.getResult().isOK()
        buildConfig.contains "Gradle Version: $distribution.version"

        where:
        distribution << supportedGradleDistributions
    }

    @Timeout(60)
    def "Can launch continuous builds"() {
        setup:
        def RunGradleBuildLaunchRequestJob continousJob = new RunGradleBuildLaunchRequestJob(createLaunch(projectDir, GradleDistribution.fromBuild(), ['clean','build'], ['-t']))
        def RunGradleBuildLaunchRequestJob normalJob = new RunGradleBuildLaunchRequestJob(createLaunch(projectDir, GradleDistribution.fromBuild(), ['build'], ['']))

        when:
        continousJob.schedule()

        normalJob.schedule()
        normalJob.join()

        then:
        normalJob.getResult().isOK()

        cleanup:
        continousJob.cancel()
    }

    ILaunch createLaunch(File projectDir, GradleDistribution distribution = GradleDistribution.fromBuild(), tasks = ['clean', 'build'], arguments = []) {
        ILaunchConfiguration launchConfiguration = createLaunchConfiguration(projectDir, tasks, distribution, arguments)
        ILaunch launch = Mock(ILaunch)
        launch.launchConfiguration >> launchConfiguration
        launch
    }

}
