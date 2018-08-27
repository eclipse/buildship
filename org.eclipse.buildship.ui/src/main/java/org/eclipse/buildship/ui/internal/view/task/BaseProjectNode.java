/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

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
