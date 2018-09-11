package org.eclipse.buildship.core.internal.configuration

import org.gradle.tooling.GradleConnector
import org.gradle.tooling.LongRunningOperation
import org.gradle.tooling.model.build.BuildEnvironment
import org.gradle.tooling.model.build.GradleEnvironment
import spock.lang.Specification

import org.eclipse.buildship.core.GradleDistributions

class GradleArgumentsTest extends Specification {

    def "Assigns proper arguments to target operation"(baseArgs, buildScans, offlineMode, gradleVersion, List expected) {
        setup:
        GradleArguments gradleArguments = GradleArguments.from(new File('.'), GradleDistributions.fromBuild(), null, null, buildScans, offlineMode, baseArgs, [])
        LongRunningOperation operation = Mock(LongRunningOperation)
        GradleEnvironment gradleEnvironment = Mock(GradleEnvironment)
        gradleEnvironment.gradleVersion >> gradleVersion
        BuildEnvironment buildEnvironment = Mock(BuildEnvironment)
        buildEnvironment.gradle >> gradleEnvironment

        when:
        gradleArguments.applyTo(operation, buildEnvironment)

        then:
        1 * operation.withArguments(expected)

        where:
        baseArgs           | buildScans | offlineMode | gradleVersion | expected
        ['b', 'a']         | false      | false       | '3.5'         | ['b', 'a']
        ['c']              | false      | true        | '3.5'         | ['c', '--offline']
        ['d']              | true       | false       | '3.5'         | ['d', '--scan']
        ['d']              | true       | false       | '3.4.1'       | ['d', '-Dscan']
        ['e']              | true       | true        | '3.5'         | ['e', '--scan', '--offline']
        ['e']              | true       | true        | '3.3'         | ['e', '-Dscan', '--offline']
        ['f', '--offline'] | false      | true        | '3.5'         | ['f', '--offline']
        ['g', '--scan']    | true       | false       | '3.5'         | ['g', '--scan']
        ['g', '-Dscan']    | true       | false       | '3.2'         | ['g', '-Dscan']
    }

    def "Configures GradleConnector to use local installation"() {
        setup:
        File file = new File('.')
        GradleArguments gradleArguments = GradleArguments.from(new File('.'), GradleDistributions.forLocalInstallation(file), null, null, false, false, [], [])
        GradleConnector connector = Mock(GradleConnector.class)

        when:
        gradleArguments.applyTo(connector)

        then:
        1 * connector.useInstallation(_)
    }

    def "Configures GradleConnector to use remote distribution"() {
        setup:
        URI uri = new File('.').toURI()
        GradleArguments gradleArguments = GradleArguments.from(new File('.'), GradleDistributions.forRemoteDistribution(uri), null, null, false, false, [], [])
        GradleConnector connector = Mock(GradleConnector.class)

        when:
        gradleArguments.applyTo(connector)

        then:
        1 * connector.useDistribution(uri)
    }

    def "Configures GradleConnector to use version number"() {
        setup:
        GradleArguments gradleArguments = GradleArguments.from(new File('.'), GradleDistributions.forVersion('2.0'), null, null, false, false, [], [])
        GradleConnector connector = Mock(GradleConnector.class)

        when:
        gradleArguments.applyTo(connector)

        then:
        1 * connector.useGradleVersion('2.0')
    }

    def "Configures GradleConnector to use default distibution defined by the Tooling API library"() {
        setup:
        GradleArguments gradleArguments = GradleArguments.from(new File('.'), GradleDistributions.fromBuild(), null, null, false, false, [], [])
        GradleConnector connector = Mock(GradleConnector.class)

        when:
        gradleArguments.applyTo(connector)

        then:
        1 * connector.useBuildDistribution()
    }
}
