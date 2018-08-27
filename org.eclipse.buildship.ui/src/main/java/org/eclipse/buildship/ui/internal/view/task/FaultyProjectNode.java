/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

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
