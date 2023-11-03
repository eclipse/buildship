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

import com.google.common.base.Optional;

import org.eclipse.core.resources.IProject;

/**
 * Base class for task view nodes that represent a project.
 *
 * @author Donat Csikos
 */
public abstract class BaseProjectNode {

    private final Optional<IProject> workspaceProject;

    public BaseProjectNode(Optional<IProject> workspaceProject) {
        this.workspaceProject = workspaceProject;
    }

    public Optional<IProject> getWorkspaceProject() {
        return this.workspaceProject;
    }
}
