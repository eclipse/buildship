/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.configuration;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.internal.event.Event;

/**
 * Event raised when the Gradle nature is removed from a project.
 *
 * @author Donat Csikos
 */
public final class GradleProjectNatureDeconfiguredEvent implements Event {

    private final IProject project;

    public GradleProjectNatureDeconfiguredEvent(IProject project) {
        this.project = project;
    }

    public IProject getProject() {
        return this.project;
    }
}
