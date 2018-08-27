/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.internal.view.task;

import java.util.Map;

import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.internal.util.gradle.Path;


/**
 * Tree node in the {@link TaskView} representing a Gradle project.
 */
public final class ProjectNode extends BaseProjectNode {

    private final ProjectNode parentProjectNode;
    private final EclipseProject eclipseProject;
    private final GradleProject gradleProject;
    private final boolean includedProject;
    private final Map<Path, BuildInvocations> allBuildInvocations;
    private final Path projectPath;

    public ProjectNode(ProjectNode parentProjectNode, EclipseProject eclipseProject, GradleProject gradleProject, Optional<IProject> workspaceProject, boolean includedProject,  Map<Path, BuildInvocations> allBuildInvocations, Path projectPath) {
        super(workspaceProject);
        this.parentProjectNode = parentProjectNode; // is null for root project
        this.eclipseProject = Preconditions.checkNotNull(eclipseProject);
        this.gradleProject = Preconditions.checkNotNull(gradleProject);
        this.includedProject = includedProject;
        this.allBuildInvocations = Preconditions.checkNotNull(allBuildInvocations);
        this.projectPath = Preconditions.checkNotNull(projectPath);
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
        return this.gradleProject;
    }

    public boolean isIncludedProject() {
        return this.includedProject;
    }

    public Map<Path, BuildInvocations> getAllBuildInvocations() {
        return ImmutableMap.copyOf(this.allBuildInvocations);
    }

    public BuildInvocations getInvocations() {
        return this.allBuildInvocations.get(this.projectPath);
    }

    @Override
    public String toString() {
        return this.gradleProject.getName();
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
        return Objects.equal(this.parentProjectNode, that.parentProjectNode)
                && Objects.equal(this.eclipseProject, that.eclipseProject)
                && Objects.equal(this.gradleProject, that.gradleProject)
                && Objects.equal(this.includedProject, that.includedProject)
                && Objects.equal(this.allBuildInvocations, that.allBuildInvocations)
                && Objects.equal(this.projectPath, that.projectPath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getWorkspaceProject(), this.parentProjectNode, this.eclipseProject, this.gradleProject, this.includedProject, this.allBuildInvocations, this.projectPath);
    }
}
