/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
