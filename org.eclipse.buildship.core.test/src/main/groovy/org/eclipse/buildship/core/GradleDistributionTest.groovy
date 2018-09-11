package org.eclipse.buildship.core


import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification

class GradleDistributionTest extends WorkspaceSpecification {

    def "Can create a Gradle distribution referencing the wrapper"() {
        setup:
        GradleDistribution distribution = GradleDistributions.fromBuild();

        expect:
        distribution instanceof WrapperGradleDistribution
    }

    def "Can create a Gradle distribution referencing a valid local installation"() {
        setup:
        File dir = dir('existing')
        GradleDistribution distribution = GradleDistributions.forLocalInstallation(dir)

        expect:
        distribution instanceof LocalGradleDistribution
        distribution.location == dir
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
        distribution instanceof RemoteGradleDistribution
        distribution.url.toString() == 'https://example.com/gradle-dist'
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
        distribution instanceof FixedVersionGradleDistribution
        distribution.version  == '4.9'
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

    def "GradleDistrubution has human-readable toString() implementation"() {
        when:
        File file = new File('.')
        GradleDistribution distribution = GradleDistributions.forLocalInstallation(file)

        then:
        distribution.toString() == "Local installation at " + file.absolutePath

        when:
        distribution = GradleDistributions.forRemoteDistribution(file.toURI())

        then:
        distribution.toString() == "Remote distribution from " + file.toURI().toString()

        when:
        distribution = GradleDistributions.forVersion('2.1')

        then:
        distribution.toString() == "Specific Gradle version 2.1"

        when:
        distribution = GradleDistributions.fromBuild()

        then:
        distribution.toString() == "Gradle wrapper from target build"
    }
}
