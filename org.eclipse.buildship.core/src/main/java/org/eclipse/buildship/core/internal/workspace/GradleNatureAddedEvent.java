/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.internal.event.Event;

/**
 * Event announcing that the Gradle nature is added to a set of projects.
 *
 * @author Donat Csikos
 */
public final class GradleNatureAddedEvent implements Event {

    private final IProject project;

    public GradleNatureAddedEvent(IProject project) {
        this.project = project;
    }

    public IProject getProject() {
        return this.project;
    }
}
