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

/**
 * Project deleted event.
 *
 * @author Donat Csikos
 */
public final class ProjectMovedEvent extends BaseProjectChangedEvent {

    private final String previousName;

    public ProjectMovedEvent(IProject project, String previousName) {
        super(project);
        this.previousName = Preconditions.checkNotNull(previousName);
    }

    public String getPreviousName() {
        return this.previousName;
    }
}
