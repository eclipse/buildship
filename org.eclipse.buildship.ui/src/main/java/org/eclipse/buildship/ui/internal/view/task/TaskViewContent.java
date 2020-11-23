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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.collect.Lists;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.internal.util.gradle.HierarchicalElementUtils;

/**
 * Encapsulates the content backing the {@link TaskView}.
 */
public final class TaskViewContent {

    private final Map<File, Map<String, EclipseProject>> models;
    private final List<EclipseProject> allEclipseProjects;
    private final List<IProject> faultyWorkspaceProjects;

    public TaskViewContent(Map<File, Map<String, EclipseProject>> models, Map<String, IProject> allGradleWorkspaceProjects) {
        this.models = models;
        this.allEclipseProjects = collectAllEclipseProjects(models);
        this.faultyWorkspaceProjects = collectFaultyWorkspaceProjects(allGradleWorkspaceProjects, this.allEclipseProjects);
    }

    private static List<EclipseProject> collectAllEclipseProjects(Map<File, Map<String, EclipseProject>> models) {
        List<EclipseProject> result = Lists.newArrayList();
        for(Map<String, EclipseProject> ep1 : models.values()) {
            for (EclipseProject ep2 : ep1.values()) {
                result.addAll(HierarchicalElementUtils.getAll(ep2));
            }
        }
        return result;
    }

    private static List<IProject> collectFaultyWorkspaceProjects(Map<String, IProject> workspaceProjects, List<EclipseProject> eclipseProjects) {
        Map<String, IProject> result = new LinkedHashMap<>(workspaceProjects);
        for (EclipseProject p : eclipseProjects) {
            result.remove(p.getName());
        }
        return new ArrayList<>(result.values());
    }

    public List<IProject> getFaultyWorkspaceProjects() {
        return this.faultyWorkspaceProjects;
    }

    public List<EclipseProject> getAllEclipseProjects() {
        return this.allEclipseProjects;
    }

    public Map<File, Map<String, EclipseProject>> getModels() {
        return this.models;
    }
}
