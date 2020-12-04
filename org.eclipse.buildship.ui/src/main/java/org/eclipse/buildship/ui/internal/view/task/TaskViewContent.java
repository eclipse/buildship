/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.gradle.tooling.model.eclipse.EclipseProject;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.internal.util.gradle.HierarchicalElementUtils;

/**
 * Encapsulates the content backing the {@link TaskView}.
 */
public final class TaskViewContent {

    private final Collection<GradleBuildViewModel> models;
    private final List<EclipseProject> allEclipseProjects;
    private final List<IProject> faultyWorkspaceProjects;

    private TaskViewContent(Collection<GradleBuildViewModel> models, List<EclipseProject> allEclipseProjects, List<IProject> faultyWorkspaceProjects) {
       this.models = models;
       this.allEclipseProjects = allEclipseProjects;
       this.faultyWorkspaceProjects = faultyWorkspaceProjects;
    }

    public List<IProject> getFaultyWorkspaceProjects() {
        return this.faultyWorkspaceProjects;
    }

    public boolean isEmpty() {
        return this.allEclipseProjects.isEmpty() && this.faultyWorkspaceProjects.isEmpty();
    }

    public Collection<GradleBuildViewModel> getModels() {
        return this.models;
    }

    public static TaskViewContent from(Collection<GradleBuildViewModel> models, Map<String, IProject> allGradleWorkspaceProjects) {
        List<EclipseProject> allEclipseProjects = collectAllEclipseProjects(models);
        List<IProject> faultyWorkspaceProjects = collectFaultyWorkspaceProjects(allGradleWorkspaceProjects, allEclipseProjects);
        return new TaskViewContent(models, allEclipseProjects, faultyWorkspaceProjects);
    }

    private static List<EclipseProject> collectAllEclipseProjects(Collection<GradleBuildViewModel> models) {
        return models.stream().map(GradleBuildViewModel::getRootEclipseProject).flatMap(p -> HierarchicalElementUtils.getAll(p).stream()).collect(Collectors.toUnmodifiableList());
    }

    private static List<IProject> collectFaultyWorkspaceProjects(Map<String, IProject> workspaceProjects, List<EclipseProject> eclipseProjects) {
        Map<String, IProject> result = new LinkedHashMap<>(workspaceProjects);
        for (EclipseProject p : eclipseProjects) {
            result.remove(p.getName());
        }
        return new ArrayList<>(result.values());
    }
}
