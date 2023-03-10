/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.internal.util.gradle.HierarchicalElementUtils;

/**
 * Encapsulates the content backing the {@link TaskView}.
 */
public final class TaskViewContent {

    private final List<BuildNode> allBuilds;
    private final List<IProject> faultyWorkspaceProjects;

    private TaskViewContent(List<BuildNode> allBuilds, List<IProject> faultyWorkspaceProjects) {
       this.allBuilds = allBuilds;
       this.faultyWorkspaceProjects = faultyWorkspaceProjects;
    }

    public List<IProject> getFaultyWorkspaceProjects() {
        return this.faultyWorkspaceProjects;
    }

    public boolean isEmpty() {
        return this.allBuilds.isEmpty() && this.faultyWorkspaceProjects.isEmpty();
    }

    public Collection<BuildNode> getBuilds() {
        return this.allBuilds;
    }

    public static TaskViewContent from(Map<File, Map<String, EclipseProject>> allModels, Map<File, BuildEnvironment> environments, Map<String, IProject> allGradleWorkspaceProjects) {
        List<BuildNode> builds = new ArrayList<>();

        for (Entry<File, Map<String, EclipseProject>> model : allModels.entrySet()) {
            File rootProjectDir = model.getKey();
            BuildEnvironment buildEnvironment = environments.get(rootProjectDir);
            BuildTreeNode buildTreeNode = new BuildTreeNode(rootProjectDir, buildEnvironment);
            for (Entry<String,EclipseProject> entry : model.getValue().entrySet()) {
                String includedBuildName = entry.getKey().equals(":") ? null : entry.getKey();
                EclipseProject rootEclipseProject = entry.getValue();
                builds.add(new BuildNode(buildTreeNode, rootEclipseProject, includedBuildName));
            }
        }
        List<IProject> faultyWorkspaceProjects = collectFaultyWorkspaceProjects(allGradleWorkspaceProjects, builds);
        return new TaskViewContent(builds, faultyWorkspaceProjects);
    }

    private static List<IProject> collectFaultyWorkspaceProjects(Map<String, IProject> workspaceProjects, List<BuildNode> builds) {
        Map<String, IProject> result = new LinkedHashMap<>(workspaceProjects);
        for (EclipseProject p : collectAllEclipseProjects(builds)) {
            result.remove(p.getName());
        }
        return new ArrayList<>(result.values());
    }

    private static List<EclipseProject> collectAllEclipseProjects(List<BuildNode> builds) {
        return builds.stream().map(BuildNode::getRootEclipseProject).flatMap(p -> HierarchicalElementUtils.getAll(p).stream()).collect(Collectors.toList());
    }
}
