/*******************************************************************************
 * Copyright (c) 2020 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.ui.internal.view.task;

import java.util.Map;

import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Objects;

import org.eclipse.buildship.core.internal.util.gradle.Path;

/**
 * Can be a root build or an included build.
 */
public class BuildNode {

    private final BuildTreeNode buildTreeNode;
    private final EclipseProject rootEclipseProject;
    private final String includedBuildName;
    private final Map<Path, BuildInvocations> allBuildInvocations;

    public BuildNode(BuildTreeNode buildTreeNode, EclipseProject rootEclipseProject, String includedBuildName) {
        this.buildTreeNode = buildTreeNode;
        this.rootEclipseProject = rootEclipseProject;
        this.includedBuildName = includedBuildName;
        this.allBuildInvocations = BuildInvocations.collectAll(rootEclipseProject.getGradleProject());
    }

    public BuildTreeNode getBuildTreeNode() {
        return this.buildTreeNode;
    }

    public boolean isIncludedBuild() {
        return this.includedBuildName != null;
    }

    public String getIncludedBuildName() {
        return this.includedBuildName;
    }


    public EclipseProject getRootEclipseProject() {
        return this.rootEclipseProject;
    }

    public BuildInvocations buildInvocationsFor(Path path) {
        return this.allBuildInvocations.get(path);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        BuildNode that = (BuildNode) other;
        return Objects.equal(this.buildTreeNode, that.buildTreeNode) &&
                Objects.equal(this.rootEclipseProject, that.rootEclipseProject) &&
                Objects.equal(this.includedBuildName, that.includedBuildName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.buildTreeNode, this.rootEclipseProject, this.includedBuildName);
    }
}
