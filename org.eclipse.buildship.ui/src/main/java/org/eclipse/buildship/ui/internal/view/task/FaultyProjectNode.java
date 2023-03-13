/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

import org.eclipse.core.resources.IProject;

/**
 * Tree node in the {@link TaskView} representing a faulty project.
 */
public final class FaultyProjectNode extends BaseProjectNode {

    public FaultyProjectNode(IProject project) {
        super(Optional.of(project));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return Objects.equal(getWorkspaceProject(), getWorkspaceProject());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getWorkspaceProject());
    }
}
