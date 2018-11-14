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
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
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
        gradleDistribution << getSupportedGradleDistributions(">=4.10.2") +
                              GradleDistribution.fromBuild() +
                              GradleDistribution.forRemoteDistribution(new URI("http://services.gradle.org/distributions/gradle-4.10.2-bin.zip"))
    }
}
