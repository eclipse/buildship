/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace

import java.util.concurrent.TimeUnit

import spock.lang.Timeout
import spock.lang.Unroll

import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification

class InitializeNewProjectOperationTest extends WorkspaceSpecification {

    @Unroll
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    def "Can initialize a new project with #gradleDistribution"(GradleDistribution gradleDistribution) {
        setup:
        File location = new File(testDir, 'initializeNewProjectOperationTest1')
        BuildConfiguration buildConfiguration = createOverridingBuildConfiguration(location, gradleDistribution)
        InitializeNewProjectOperation operation = new InitializeNewProjectOperation(buildConfiguration)

        when:
        CorePlugin.operationManager().run(operation, new NullProgressMonitor())

        then:
        noExceptionThrown()

        where:
        gradleDistribution << [
            GradleDistribution.fromBuild(),
              // https://github.com/eclipse/buildship/issues/1187
              // getSupportedGradleDistributions(">=4.10.2"),
              // GradleDistribution.forRemoteDistribution(new URI("https://services.gradle.org/distributions/gradle-4.10.2-bin.zip"))
        ]
    }
}
