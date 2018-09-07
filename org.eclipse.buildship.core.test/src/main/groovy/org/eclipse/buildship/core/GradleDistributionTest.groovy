package org.eclipse.buildship.core

import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.internal.util.gradle.GradleDistribution.Type

class GradleDistributionTest extends WorkspaceSpecification {

    def "Can create a Gradle distribution referencing the wrapper"() {
        setup:
        GradleDistribution distribution = GradleDistribution.fromBuild();

        expect:
        distribution.distributionType == Type.WRAPPER
        distribution.configuration == ''
    }

    def "Can create a Gradle distribution referencing a valid local installation"() {
        setup:
        File dir = dir('existing')
        GradleDistribution distribution = GradleDistribution.forLocalInstallation(dir)

        expect:
        distribution.distributionType == Type.LOCAL_INSTALLATION
        distribution.configuration == dir.absolutePath
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
        distribution.distributionType == Type.REMOTE_DISTRIBUTION
        distribution.configuration == 'https://example.com/gradle-dist'
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
        distribution.distributionType == Type.VERSION
        distribution.configuration == '4.9'
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
}
