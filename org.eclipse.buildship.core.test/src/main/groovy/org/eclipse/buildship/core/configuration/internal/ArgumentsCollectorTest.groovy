package org.eclipse.buildship.core.configuration.internal

import java.util.List

import org.gradle.tooling.model.build.BuildEnvironment
import org.gradle.tooling.model.build.GradleEnvironment
import spock.lang.Specification

class ArgumentsCollectorTest extends Specification {

    def "Collects proper arguments"(baseArgs, buildScans, offlineMode, gradleVersion, List expected) {
        setup:
        GradleEnvironment gradleEnvironment = Mock(GradleEnvironment)
        gradleEnvironment.gradleVersion >> gradleVersion
        BuildEnvironment buildEnvironment = Mock(BuildEnvironment)
        buildEnvironment.gradle >> gradleEnvironment

        expect:
        ArgumentsCollector.collectArguments(baseArgs, buildScans, offlineMode, buildEnvironment) == expected

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
