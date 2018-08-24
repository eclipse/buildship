/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.buildship.core

import org.gradle.tooling.GradleConnector
import spock.lang.Specification

import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.internal.DefaultGradleDistribution
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.test.fixtures.GradleVersionParameterization.DistributionLocator

class GradleDistributionTest extends WorkspaceSpecification {

    def "Can create a Gradle distribution referencing the wrapper"() {
        setup:
        GradleDistribution distribution = GradleDistribution.fromBuild();

        expect:
        distribution.distributionInfo.type == GradleDistributionType.WRAPPER
        distribution.distributionInfo.configuration == ''
    }

    def "Can create a Gradle distribution referencing a valid local installation"() {
        setup:
        File dir = dir('existing')
        DefaultGradleDistribution distribution = GradleDistribution.forLocalInstallation(dir)

        expect:
        distribution.distributionInfo.type == GradleDistributionType.LOCAL_INSTALLATION
        distribution.distributionInfo.configuration == dir.absolutePath
    }

    def "Gradle distribution cannot be created with invalid local installation"() {
        when:
        GradleDistribution.forLocalInstallation(null)

        then:
        thrown(RuntimeException)

        when:
        GradleDistribution.forLocalInstallation(new File('nonexisting'))

        then:
        thrown(RuntimeException)

        when:
        GradleDistribution.forLocalInstallation(file('nondir'))

        then:
        thrown(RuntimeException)
    }

    def "Can create a Gradle distribution referencing a valid remote installation"() {
        setup:
        DefaultGradleDistribution distribution = GradleDistribution.forRemoteDistribution(new URI('https://example.com/gradle-dist'))

        expect:
        distribution.distributionInfo.type == GradleDistributionType.REMOTE_DISTRIBUTION
        distribution.distributionInfo.configuration == 'https://example.com/gradle-dist'
    }

    def "Can create a Gradle distribution referencing an invalid remote installation"() {
        when:
        GradleDistribution.forRemoteDistribution(null)

        then:
        thrown(RuntimeException)
    }

    def "Can create a Gradle distribution referencing a valid version"() {
        setup:
        DefaultGradleDistribution distribution = GradleDistribution.forVersion("4.9")

        expect:
        distribution.distributionInfo.type == GradleDistributionType.VERSION
        distribution.distributionInfo.configuration == '4.9'
    }

    def "Can create a Gradle distribution referencing an invalid version"() {
        when:
        GradleDistribution.forVersion(null)

        then:
        thrown(RuntimeException)

        when:
        GradleDistribution.forVersion('')

        then:
        thrown(RuntimeException)
    }
}