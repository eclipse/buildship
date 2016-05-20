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

import org.gradle.tooling.GradleConnectionException;

import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;

/**
 * Encapsulates the content backing the {@link TaskView}.
 */
public final class TaskViewContent {

    private final GradleConnectionException failure;
    private final List<OmniEclipseProject> projects;

    public TaskViewContent(List<OmniEclipseProject> projects, GradleConnectionException failure) {
        this.projects = ImmutableList.copyOf(projects);
        this.failure = failure;
    }

    public List<OmniEclipseProject> getProjects() {
        if (this.failure != null) {
            throw this.failure;
        }
        return this.projects;
    }

    public Exception getFailure() {
        return this.failure;
    }
}
