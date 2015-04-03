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

package org.eclipse.buildship.ui.domain;

import org.eclipse.core.resources.IProject;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;

/**
 * Domain object representing a Gradle project.
 * <p/>
 * It connects the the {@link OmniEclipseProject} and {@link OmniGradleProject} domain elements
 * from the tooling model and the {@link IProject} from the Eclipse environment.
 * <p/>
 * This object is presented in various places in the UI.
 */
public final class ProjectNode {

    private final ProjectNode parentProjectNode;
    private final OmniEclipseProject eclipseProject;
    private final OmniGradleProject gradleProject;
    private final Optional<IProject> workspaceProject;

    public ProjectNode(ProjectNode parentProjectNode, OmniEclipseProject eclipseProject, OmniGradleProject gradleProject, Optional<IProject> workspaceProject) {
        this.parentProjectNode = parentProjectNode; // is null for root project
        this.eclipseProject = Preconditions.checkNotNull(eclipseProject);
        this.gradleProject = Preconditions.checkNotNull(gradleProject);
        this.workspaceProject = workspaceProject;
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

    public OmniEclipseProject getEclipseProject() {
        return this.eclipseProject;
    }

    public OmniGradleProject getGradleProject() {
        return this.gradleProject;
    }

    public Optional<IProject> getWorkspaceProject() {
        return this.workspaceProject;
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
        return Objects.equal(this.parentProjectNode, that.parentProjectNode) && Objects.equal(this.eclipseProject, that.eclipseProject)
                && Objects.equal(this.gradleProject, that.gradleProject) && Objects.equal(this.workspaceProject, that.workspaceProject);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.parentProjectNode, this.eclipseProject, this.gradleProject, this.workspaceProject);
    }

}
