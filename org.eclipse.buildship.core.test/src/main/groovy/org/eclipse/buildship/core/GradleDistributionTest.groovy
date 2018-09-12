package org.eclipse.buildship.core


import org.gradle.tooling.GradleConnector

import org.eclipse.buildship.core.internal.configuration.GradleArguments
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
        GradleDistribution.forLocalInstallation(file('plainfile'))

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

    def "GradleDistrubution has human-readable toString() implementation"() {
        when:
        File file = new File('.')
        GradleDistribution distribution = GradleDistribution.forLocalInstallation(file)

        then:
        distribution.toString() == "Local installation at " + file.absolutePath

        when:
        distribution = GradleDistribution.forRemoteDistribution(file.toURI())

        then:
        distribution.toString() == "Remote distribution from " + file.toURI().toString()

        when:
        distribution = GradleDistribution.forVersion('2.1')

        then:
        distribution.toString() == "Specific Gradle version 2.1"

        when:
        distribution = GradleDistribution.fromBuild()

        then:
        distribution.toString() == "Gradle wrapper from target build"
    }

    def "GradleDistrubution configures GradleConnector to use local installation"() {
        setup:
        File file = new File('.')
        GradleConnector connector = Mock(GradleConnector.class)

        when:
        GradleDistribution.forLocalInstallation(file).apply(connector)

        then:
        1 * connector.useInstallation(file)
    }

    def "GradleDistrubution configures GradleConnector to use remote distribution"() {
        setup:
        URI uri = new File('.').toURI()
        GradleConnector connector = Mock(GradleConnector.class)

        when:
        GradleDistribution.forRemoteDistribution(uri).apply(connector)

        then:
        1 * connector.useDistribution(uri)
    }

    def "GradleDistrubution configures GradleConnector to use version number"() {
        setup:
        GradleConnector connector = Mock(GradleConnector.class)

        when:
        GradleDistribution.forVersion('2.0').apply(connector)

        then:
        1 * connector.useGradleVersion('2.0')
    }

    def "GradleDistrubution configures GradleConnector to use default distibution defined by the Tooling API library"() {
        setup:
        GradleConnector connector = Mock(GradleConnector.class)

        when:
        GradleDistribution.fromBuild().apply(connector)

        then:
        1 * connector.useBuildDistribution()
    }
}
