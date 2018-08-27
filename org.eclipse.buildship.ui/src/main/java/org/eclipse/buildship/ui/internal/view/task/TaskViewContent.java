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

package org.eclipse.buildship.ui.internal.view.task;

import java.util.List;

import org.gradle.tooling.model.eclipse.EclipseProject;

import org.eclipse.core.resources.IProject;

/**
 * Encapsulates the content backing the {@link TaskView}.
 */
public final class TaskViewContent {

    private final List<EclipseProject> projects;
    private final List<IProject> faultyProjects;

    public TaskViewContent(List<EclipseProject> projects, List<IProject> faultyProjects) {
        this.projects = projects;
        this.faultyProjects = faultyProjects;
    }

    public List<EclipseProject> getProjects() {
        return this.projects;
    }

    public List<IProject> getFaultyProjects() {
        return this.faultyProjects;
    }
}
