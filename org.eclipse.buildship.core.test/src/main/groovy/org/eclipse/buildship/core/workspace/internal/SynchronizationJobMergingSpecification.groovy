package org.eclipse.buildship.core.workspace.internal

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import com.google.common.collect.ImmutableList

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes

import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectBuilder
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification;
import org.eclipse.buildship.core.test.fixtures.TestEnvironment
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.util.variable.ExpressionUtils
import org.eclipse.buildship.core.workspace.WorkspaceGradleOperations;;

class SynchronizationJobMergingSpecification extends ProjectSynchronizationSpecification {

    def "Mutliple jobs with the same configuration are merged"() {
        setup:
        File projectLocation = dir("sample-project") {
            file 'settings.gradle'
        }
        def jobs = (1..5).collect { newSynchronizationJob(projectLocation) }
        WorkspaceGradleOperations workspaceOperations = Mock(WorkspaceGradleOperations)
        registerService(WorkspaceGradleOperations, workspaceOperations)

        when:
        jobs.each { it.schedule() }
        waitForGradleJobsToFinish()

        then:
        1 * workspaceOperations.synchronizeGradleBuildWithWorkspace(*_)
    }

}
