/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
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
