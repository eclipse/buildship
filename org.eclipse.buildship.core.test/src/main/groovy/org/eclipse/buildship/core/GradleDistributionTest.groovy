package org.eclipse.buildship.core


import org.eclipse.buildship.core.internal.BaseGradleDistribution.Type
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification

class GradleDistributionTest extends WorkspaceSpecification {

    def "Can create a Gradle distribution referencing the wrapper"() {
        setup:
        GradleDistribution distribution = GradleDistributions.fromBuild();

        expect:
        distribution.type == Type.WRAPPER
        distribution.configuration == ''
    }

    def "Can create a Gradle distribution referencing a valid local installation"() {
        setup:
        File dir = dir('existing')
        GradleDistribution distribution = GradleDistributions.forLocalInstallation(dir)

        expect:
        distribution.type == Type.LOCAL_INSTALLATION
        distribution.configuration == dir.absolutePath
    }

    def "Gradle distribution cannot be created with invalid local installation"() {
        when:
        GradleDistributions.forLocalInstallation(null)

        then:
        thrown(RuntimeException)

        when:
        GradleDistributions.forLocalInstallation(new File('nonexisting'))

        then:
        thrown(RuntimeException)

        when:
        GradleDistributions.forLocalInstallation(file('plainfile'))

        then:
        thrown(RuntimeException)
    }

    def "Can create a Gradle distribution referencing a valid remote installation"() {
        setup:
        GradleDistribution distribution = GradleDistributions.forRemoteDistribution(new URI('https://example.com/gradle-dist'))

        expect:
        distribution.type == Type.REMOTE_DISTRIBUTION
        distribution.configuration == 'https://example.com/gradle-dist'
    }

    def "Can create a Gradle distribution referencing an invalid remote installation"() {
        when:
        GradleDistributions.forRemoteDistribution(null)

        then:
        thrown(RuntimeException)
    }

    def "Can create a Gradle distribution referencing a valid version"() {
        setup:
        GradleDistribution distribution = GradleDistributions.forVersion("4.9")

        expect:
        distribution.type == Type.VERSION
        distribution.configuration == '4.9'
    }

    def "Can create a Gradle distribution referencing an invalid version"() {
        when:
        GradleDistributions.forVersion(null)
        GradleDistributions.forVersion(null)

        then:
        thrown(RuntimeException)

        when:
        GradleDistributions.forVersion('')

        then:
        thrown(RuntimeException)
    }
}
