/*******************************************************************************
 * Copyright (c) 2020 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.ui.internal.view.task;

import java.io.File;

import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Objects;

public class GradleBuildViewModel {

    private final File buildRootDir;
    private final EclipseProject eclipseProject;
    private final String includedBuildName;

    public GradleBuildViewModel(File buildRootDir, EclipseProject eclipseProject, String includedBuildName) {
        this.buildRootDir = buildRootDir;
        this.eclipseProject = eclipseProject;
        this.includedBuildName = includedBuildName;
    }

    public File getBuildRootDir() {
        return this.buildRootDir;
    }

    public EclipseProject getRootEclipseProject() {
        return this.eclipseProject;
    }

    public String getIncludedBuildName() {
        return this.includedBuildName;
    }

    public boolean isIncludedBuild() {
        return this.includedBuildName != null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        GradleBuildViewModel that = (GradleBuildViewModel) other;
        return Objects.equal(this.buildRootDir, that.buildRootDir)
                && Objects.equal(this.eclipseProject, that.eclipseProject)
                && Objects.equal(this.includedBuildName, that.includedBuildName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.buildRootDir, this.eclipseProject, this.includedBuildName);
    }
}
