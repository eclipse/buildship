package org.eclipse.buildship.core.internal.configuration.impl

import org.gradle.tooling.LongRunningOperation
import org.gradle.tooling.model.build.BuildEnvironment
import org.gradle.tooling.model.build.GradleEnvironment
import spock.lang.Specification

import org.eclipse.buildship.core.internal.configuration.GradleArguments
import org.eclipse.buildship.core.internal.util.gradle.GradleDistribution

class GradleArgumentsTest extends Specification {

    def "Assigns proper arguments to target operation"(baseArgs, buildScans, offlineMode, gradleVersion, List expected) {
        setup:
        GradleArguments gradleArguments = GradleArguments.from(new File('.'), GradleDistribution.fromBuild(), null, null, buildScans, offlineMode, baseArgs, [])
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
}
