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
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IProject;

/**
 * Encapsulates the content backing the {@link TaskView}.
 */
public final class TaskViewContent {

    private final Map<FixedRequestAttributes, Set<OmniEclipseProject>> projects;
    private final List<IProject> faultyProjects;

    public TaskViewContent(Map<FixedRequestAttributes, Set<OmniEclipseProject>> resultProjects, List<IProject> faultyProjects) {
        this.projects = ImmutableMap.copyOf(resultProjects);
        this.faultyProjects = faultyProjects;
    }

    public List<OmniEclipseProject> getProjects() {
        return ImmutableList.copyOf(Iterables.concat(this.projects.values()));
    }

    public Set<OmniEclipseProject> findProjectsFor(FixedRequestAttributes attributes) {
        return this.projects.get(attributes);
    }

    public List<IProject> getFaultyProjects() {
        return this.faultyProjects;
    }
}
