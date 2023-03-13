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

import java.io.File;

import org.gradle.tooling.model.build.BuildEnvironment;

import com.google.common.base.Objects;

import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;

/**
 * Represents a composite build that has a root build and and zero or more included builds.
 */
public class BuildTreeNode {

    private final File rootProjectDir;
    private final GradleVersion gradleVersion;

    public BuildTreeNode(File rootProjectDir, BuildEnvironment buildEnvironment) {
        this.rootProjectDir = rootProjectDir;
        this.gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
    }

    public File getRootProjectDir() {
        return this.rootProjectDir;
    }

    public boolean supportsTaskExecutionInIncludedBuild() {
        return this.gradleVersion.supportsTaskExecutionInIncudedBuild();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        BuildTreeNode that = (BuildTreeNode) other;
        return Objects.equal(this.rootProjectDir, that.rootProjectDir);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.rootProjectDir);
    }
}
