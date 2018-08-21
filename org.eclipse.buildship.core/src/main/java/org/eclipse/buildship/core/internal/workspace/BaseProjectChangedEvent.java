/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import com.google.common.base.Preconditions;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.internal.event.Event;

/**
 * Common base class for project add/delete/move events.
 *
 * @author Donat Csikos
 */
public abstract class BaseProjectChangedEvent implements Event {

    private final IProject project;

    public BaseProjectChangedEvent(IProject project) {
        this.project = Preconditions.checkNotNull(project);
    }

    public IProject getProject() {
        return this.project;
    }
}
