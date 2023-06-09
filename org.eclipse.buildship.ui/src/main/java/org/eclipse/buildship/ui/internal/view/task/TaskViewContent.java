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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;

import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;

/**
 * Encapsulates the content backing the {@link TaskView}.
 */
public final class TaskViewContent {

    private final List<BuildNode> allBuilds;
    private final List<FaultyBuildTreeNode> fauiltyBuilds;

    private TaskViewContent(List<BuildNode> allBuilds, List<FaultyBuildTreeNode> faultyBuilds) {
        this.allBuilds = allBuilds;
        this.fauiltyBuilds = faultyBuilds;
    }

    public List<FaultyBuildTreeNode> getFaultyBuilds() {
        return this.fauiltyBuilds;
    }

    public boolean isEmpty() {
        return this.allBuilds.isEmpty() && this.fauiltyBuilds.isEmpty();
    }

    public Collection<BuildNode> getBuilds() {
        return this.allBuilds;
    }

    public static TaskViewContent from(Map<File, Map<String, EclipseProject>> allModels, Map<File, BuildEnvironment> environments,
            List<InternalGradleBuild> faultyBuilds) {
        List<BuildNode> builds = new ArrayList<>();
        for (Entry<File, Map<String, EclipseProject>> model : allModels.entrySet()) {
            File rootProjectDir = model.getKey();
            BuildEnvironment buildEnvironment = environments.get(rootProjectDir);
            BuildTreeNode buildTreeNode = new BuildTreeNode(rootProjectDir, buildEnvironment);
            for (Entry<String, EclipseProject> entry : model.getValue().entrySet()) {
                String includedBuildName = entry.getKey().equals(":") ? null : entry.getKey();
                EclipseProject rootEclipseProject = entry.getValue();
                builds.add(new BuildNode(buildTreeNode, rootEclipseProject, includedBuildName));
            }
        }

        List<FaultyBuildTreeNode> faultyBuildNodes = faultyBuilds.stream().map(b -> new FaultyBuildTreeNode(b.getBuildConfig())).collect(Collectors.toList());
        return new TaskViewContent(builds, faultyBuildNodes);
    }
}
