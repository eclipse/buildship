/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.view.task;

import org.eclipse.core.resources.IProject;

/**
 * Tree node in the {@link TaskView} representing a faulty project.
 */
public final class FaultyProjectNode {

    private final IProject project;

    public FaultyProjectNode(IProject project) {
        this.project = project;
    }

    public IProject getProject() {
        return this.project;
    }
}
