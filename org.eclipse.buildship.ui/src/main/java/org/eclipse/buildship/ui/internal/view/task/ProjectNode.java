/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task;

import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.internal.util.gradle.Path;


/**
 * Tree node in the {@link TaskView} representing a Gradle project.
 */
public final class ProjectNode extends BaseProjectNode {

    private final ProjectNode parentProjectNode;
    private final EclipseProject eclipseProject;
    private final BuildInvocations buildInvocations;
    private final BuildNode buildNode;

    public ProjectNode(ProjectNode parentProjectNode, BuildNode buildNode, Optional<IProject> workspaceProject, EclipseProject eclipseProject) {
        super(workspaceProject);
        this.parentProjectNode = parentProjectNode; // null for root project
        this.eclipseProject = eclipseProject;
        this.buildInvocations = buildNode.buildInvocationsFor(Path.from(eclipseProject.getGradleProject().getPath()));
        this.buildNode = buildNode;
    }

    public String getDisplayName() {
        String name;
        Optional<IProject> workspaceProject = this.getWorkspaceProject();
        if (workspaceProject.isPresent()) {
            name = workspaceProject.get().getName();
        } else {
            name = this.getEclipseProject().getName();
        }
        return name;
    }

    public ProjectNode getRootProjectNode() {
        ProjectNode root = this;
        while (root.getParentProjectNode() != null) {
            root = root.getParentProjectNode();
        }
        return root;
    }

    public ProjectNode getParentProjectNode() {
        return this.parentProjectNode;
    }

    public EclipseProject getEclipseProject() {
        return this.eclipseProject;
    }

    public GradleProject getGradleProject() {
        return this.eclipseProject.getGradleProject();
    }

    public BuildInvocations getInvocations() {
        return this.buildInvocations;
    }

    public BuildNode getBuildNode() {
        return this.buildNode;
    }

    @Override
    public String toString() {
        return this.eclipseProject.getGradleProject().getName();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ProjectNode that = (ProjectNode) other;
        return Objects.equal(this.getWorkspaceProject(), that.getWorkspaceProject())
                && Objects.equal(this.parentProjectNode, that.parentProjectNode)
                && Objects.equal(this.eclipseProject, that.eclipseProject)
                && Objects.equal(this.buildInvocations, that.buildInvocations)
                && Objects.equal(this.buildNode, that.buildNode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getWorkspaceProject(), this.parentProjectNode, this.eclipseProject, this.buildInvocations, this.buildNode);
    }
}
