/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task

import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.build.BuildEnvironment
import org.gradle.tooling.model.eclipse.EclipseProject

import com.google.common.base.Optional

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration
import org.eclipse.buildship.core.internal.util.gradle.Path
import org.eclipse.buildship.ui.internal.test.fixtures.WorkspaceSpecification

/**
 * Base class for tests related to Gradle Views.
 */
abstract class ViewSpecification extends WorkspaceSpecification {

    protected def newProjectNode(ProjectNode parent, String projectLocation) {
        EclipseProject eclipseProject = newEclipseProject(parent, projectLocation)
        new ProjectNode(parent, newBuildNode(eclipseProject, projectLocation), Optional.absent(), eclipseProject)
    }

    protected ProjectTaskNode newProjectTaskNode(ProjectNode parent, String taskPath) {
        ProjectTask projectTask = Stub(ProjectTask) {
            getPath() >> Path.from(taskPath)
        }
        new ProjectTaskNode(parent, projectTask)
    }

    protected TaskSelectorNode newTaskSelectorNode(ProjectNode parent) {
        TaskSelector taskSelector = Stub(TaskSelector)
        new TaskSelectorNode(parent, taskSelector)
    }

    private EclipseProject newEclipseProject(ProjectNode parentNode, String path) {
        File projectDir = dir(path)
        BuildConfiguration buildConfiguration = createInheritingBuildConfiguration(projectDir)
        CorePlugin.configurationManager().saveBuildConfiguration(buildConfiguration)
        EclipseProject eclipseProject = Stub(EclipseProject) {
            getProjectDirectory() >> projectDir
            getParent() >> parentNode?.eclipseProject
            getGradleProject() >> Stub(GradleProject) {
                getPath() >> ":"
            }
        }
    }

    private BuildNode newBuildNode(EclipseProject eclipseProject, String projectLocation) {
        BuildNode buildNode = Stub(BuildNode)
        buildNode.isIncludedBuild() >> false
        buildNode.getRootEclipseProject() >> eclipseProject
        buildNode.getBuildTreeNode() >> newBuildTreeNode(projectLocation)
        buildNode
    }

    private BuildTreeNode newBuildTreeNode(String projectLocation) {
        BuildTreeNode buildTreeNode = Stub(BuildTreeNode)
        buildTreeNode.rootProjectDir >>  dir(projectLocation)
        buildTreeNode.supportsTaskExecutionInIncludedBuild() >> true
        buildTreeNode
    }
}
