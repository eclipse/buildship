/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration

import org.gradle.tooling.LongRunningOperation
import org.gradle.tooling.model.build.BuildEnvironment
import org.gradle.tooling.model.build.GradleEnvironment
import spock.lang.Specification

import org.eclipse.buildship.core.GradleDistribution

class GradleArgumentsTest extends Specification {

    def "Assigns proper arguments to target operation"(baseArgs, buildScans, offlineMode, gradleVersion, List expected) {
        setup:
        GradleArguments gradleArguments = GradleArguments.from(new File('.'), GradleDistribution.fromBuild(), null, null, buildScans, offlineMode, baseArgs, [])
        LongRunningOperation operation = Mock(LongRunningOperation)
        GradleEnvironment gradleEnvironment = Mock(GradleEnvironment)
        gradleEnvironment.gradleVersion >> gradleVersion
        BuildEnvironment buildEnvironment = Mock(BuildEnvironment)
        buildEnvironment.gradle >> gradleEnvironment
        def initScriptArgs = ['--init-script', GradleArguments.eclipsePluginInitScriptLocation.absolutePath]
        expected += initScriptArgs

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

    def "applies init script to target operation" (){
        setup:
        GradleArguments gradleArguments = GradleArguments.from(new File('.'), GradleDistribution.fromBuild(), null, null, false, false, [], [])
        LongRunningOperation operation = Mock(LongRunningOperation)
        GradleEnvironment gradleEnvironment = Mock(GradleEnvironment)
        BuildEnvironment buildEnvironment = Mock(BuildEnvironment)
        buildEnvironment.gradle >> gradleEnvironment
        def expected = ['--init-script', GradleArguments.eclipsePluginInitScriptLocation.absolutePath]

        when:
        gradleArguments.applyTo(operation, buildEnvironment)

        then:
        1 * operation.withArguments(expected)

    }
}
