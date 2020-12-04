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

import com.google.common.base.Objects;

/**
 * Represents a composite build that has a root build and and zero or more included builds.
 */
public class BuildTreeNode {

    private File rootProjectDir;

    public BuildTreeNode(File rootProjectDir) {
        this.rootProjectDir = rootProjectDir;
    }

    public File getRootProjectDir() {
        return this.rootProjectDir;
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
