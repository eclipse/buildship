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

import java.util.Objects;

import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;

/**
 * Represents a Gradle build that Buildship could not sync or haven't synced yet.
 */
public class FaultyBuildTreeNode {

    private final BuildConfiguration configuration;

    public FaultyBuildTreeNode(BuildConfiguration buildConfiguration) {
        this.configuration = buildConfiguration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.configuration);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FaultyBuildTreeNode other = (FaultyBuildTreeNode) obj;
        return Objects.equals(this.configuration, other.configuration);
    }

    public String getBuildName() {
        return this.configuration.getRootProjectDirectory().getName();
    }

    public String getProjectPath() {
        return this.configuration.getRootProjectDirectory().getAbsolutePath();
    }

    public BuildConfiguration getBuildConfiguration() {
        return this.configuration;
    }
}
