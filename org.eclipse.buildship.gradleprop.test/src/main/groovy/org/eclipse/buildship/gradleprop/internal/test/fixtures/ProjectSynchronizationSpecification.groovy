/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.gradleprop.internal.test.fixtures

import com.google.common.base.Optional
import com.google.common.base.Preconditions

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.BuildConfiguration
import org.eclipse.buildship.core.GradleBuild
import org.eclipse.buildship.core.GradleCore
import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.internal.CorePlugin

abstract class ProjectSynchronizationSpecification extends WorkspaceSpecification {

    protected static final GradleDistribution DEFAULT_DISTRIBUTION = GradleDistribution.fromBuild()

    protected void synchronizeAndWait(File location) {
        Optional<IProject> project = CorePlugin.workspaceOperations().findProjectByLocation(location.canonicalFile)
        Preconditions.checkState(project.present, "Workspace does not have project located at ${location.absolutePath}")
        synchronizeAndWait(project.get())
    }

    protected void synchronizeAndWait(IProject project) {
        GradleCore.workspace.getBuild(project).get().synchronize(new NullProgressMonitor());
        waitForGradleJobsToFinish()
    }

    protected void importAndWait(File location, GradleDistribution gradleDistribution = GradleDistribution.fromBuild(), List<String> arguments = [], File javaHome = null) {
        BuildConfiguration configuration = BuildConfiguration
             .forRootProjectDirectory(location)
             .gradleDistribution(gradleDistribution)
             .overrideWorkspaceConfiguration(true)
             .arguments(arguments)
             .javaHome(javaHome)
             .build()
        GradleBuild gradleBuild = GradleCore.workspace.createBuild(configuration)
        gradleBuild.synchronize(new NullProgressMonitor())
        waitForGradleJobsToFinish()
        waitForResourceChangeEvents()
    }
}
