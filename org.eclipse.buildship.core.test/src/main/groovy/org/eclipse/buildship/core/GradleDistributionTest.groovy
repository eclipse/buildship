/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core


import org.gradle.api.JavaVersion
import org.gradle.tooling.GradleConnector
import spock.lang.IgnoreIf

import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification

class GradleDistributionTest extends WorkspaceSpecification {

    def "Can create a Gradle distribution referencing the wrapper"() {
        setup:
        GradleDistribution distribution = GradleDistribution.fromBuild();

        expect:
        distribution instanceof WrapperGradleDistribution
    }

    def "Can create a Gradle distribution referencing a valid local installation"() {
        setup:
        File dir = dir('existing')
        GradleDistribution distribution = GradleDistribution.forLocalInstallation(dir)

        expect:
        distribution instanceof LocalGradleDistribution
        distribution.location == dir
    }

    def "Gradle distribution cannot be created with null local installation"() {
        when:
        GradleDistribution.forLocalInstallation(null)

        then:
        thrown(RuntimeException)
    }

    def "Can create a Gradle distribution referencing a valid remote installation"() {
        setup:
        GradleDistribution distribution = GradleDistribution.forRemoteDistribution(new URI('https://example.com/gradle-dist'))

        expect:
        distribution instanceof RemoteGradleDistribution
        distribution.url.toString() == 'https://example.com/gradle-dist'
    }

    def "Can create a Gradle distribution referencing an invalid remote installation"() {
        when:
        GradleDistribution.forRemoteDistribution(null)

        then:
        thrown(RuntimeException)
    }

    def "Can create a Gradle distribution referencing a valid version"() {
        setup:
        GradleDistribution distribution = GradleDistribution.forVersion("4.9")

        expect:
        distribution instanceof FixedVersionGradleDistribution
        distribution.version  == '4.9'
    }

    def "Can create a Gradle distribution referencing an invalid version"() {
        when:
        GradleDistribution.forVersion(null)
        GradleDistribution.forVersion(null)

        then:
        thrown(RuntimeException)

        when:
        GradleDistribution.forVersion('')

        then:
        thrown(RuntimeException)
    }

    @IgnoreIf({ JavaVersion.current().isJava9Compatible() }) // TODO update cglib and re-enable the test
    def "GradleDistribution configures GradleConnector to use local installation"() {
        setup:
        File file = new File('.')
        GradleConnector connector = Mock(GradleConnector.class)

        when:
        GradleDistribution.forLocalInstallation(file).apply(connector)

        then:
        1 * connector.useInstallation(file)
    }

    @IgnoreIf({ JavaVersion.current().isJava9Compatible() }) // TODO update cglib and re-enable the test
    def "GradleDistribution configures GradleConnector to use remote distribution"() {
        setup:
        URI uri = new File('.').toURI()
        GradleConnector connector = Mock(GradleConnector.class)

        when:
        GradleDistribution.forRemoteDistribution(uri).apply(connector)

        then:
        1 * connector.useDistribution(uri)
    }

    @IgnoreIf({ JavaVersion.current().isJava9Compatible() }) // TODO update cglib and re-enable the test
    def "GradleDistribution configures GradleConnector to use version number"() {
        setup:
        GradleConnector connector = Mock(GradleConnector.class)

        when:
        GradleDistribution.forVersion('2.0').apply(connector)

        then:
        1 * connector.useGradleVersion('2.0')
    }

    @IgnoreIf({ JavaVersion.current().isJava9Compatible() }) // TODO update cglib and re-enable the test
    def "GradleDistribution configures GradleConnector to use default distibution defined by the Tooling API library"() {
        setup:
        GradleConnector connector = Mock(GradleConnector.class)

        when:
        GradleDistribution.fromBuild().apply(connector)

        then:
        1 * connector.useBuildDistribution()
    }

    def "GradleDistrubution has specific deserializable toString() implementation"() {
        when:
        File file = new File('.')
        GradleDistribution distribution = GradleDistribution.forLocalInstallation(file)
        String distributionString = distribution.toString()

        then:
        distributionString == "GRADLE_DISTRIBUTION(LOCAL_INSTALLATION(${file.path}))"
        distribution == GradleDistribution.fromString(distributionString)

        when:
        distribution = GradleDistribution.forRemoteDistribution(file.toURI())
        distributionString = distribution.toString()

        then:
        distributionString == "GRADLE_DISTRIBUTION(REMOTE_DISTRIBUTION(${file.toURI().toString()}))"
        distribution == GradleDistribution.fromString(distributionString)

        when:
        distribution = GradleDistribution.forVersion('2.1')
        distributionString = distribution.toString()

        then:
        distributionString == "GRADLE_DISTRIBUTION(VERSION(2.1))"
        distribution == GradleDistribution.fromString(distributionString)

        when:
        distribution = GradleDistribution.fromBuild()
        distributionString = distribution.toString()

        then:
        distributionString == "GRADLE_DISTRIBUTION(WRAPPER)"
        distribution == GradleDistribution.fromString(distributionString)
    }

    def "Deserializing an invalid distribution throws a runtime exception"() {
        when:
        GradleDistribution.fromString(null)

        then:
        thrown(NullPointerException)

        when:
        GradleDistribution.fromString("")

        then:
        thrown(IllegalArgumentException)

        when:
         GradleDistribution.fromString("GRADLE_DISTRIBUTION(REMOTE_DISTRIBUTION(invalid url))")

        then:
        thrown(IllegalArgumentException)
    }
}
