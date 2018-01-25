/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.view.task;

import java.util.List;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.omnimodel.OmniEclipseProject;

/**
 * Encapsulates the content backing the {@link TaskView}.
 */
public final class TaskViewContent {

    private final List<OmniEclipseProject> projects;
    private final List<IProject> faultyProjects;

    public TaskViewContent(List<OmniEclipseProject> projects, List<IProject> faultyProjects) {
        this.projects = projects;
        this.faultyProjects = faultyProjects;
    }

    public List<OmniEclipseProject> getProjects() {
        return this.projects;
    }

    public List<IProject> getFaultyProjects() {
        return this.faultyProjects;
    }
}
