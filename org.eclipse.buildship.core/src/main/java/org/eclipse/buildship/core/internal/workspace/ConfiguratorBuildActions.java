/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.gradle.tooling.ProjectConnection;

import com.google.common.base.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.extension.BuildActionContribution;

public class ConfiguratorBuildActions {

    private final Map<String, Object> queryResults;

    public ConfiguratorBuildActions(Map<String, Object> queryResults) {
        this.queryResults = queryResults;
    }

    public Object resultFor(String queryId) {
        // TODO (donat) maybe return optional
        return this.queryResults.get(queryId);
    }

    public static ConfiguratorBuildActions from(GradleBuild gradleBuild, List<BuildActionContribution> buildActionContributions, List<SynchronizationProblem> failures, IProgressMonitor monitor) throws Exception {
        SubMonitor progress = SubMonitor.convert(monitor, buildActionContributions.size());
        Map<String, Object> buildActionResults = new HashMap<>(buildActionContributions.size());
        for (BuildActionContribution buildActionContribution : buildActionContributions) {
            try {
                Function<ProjectConnection, ?> buildAction = buildActionContribution.createBuildAction();
                Object result = gradleBuild.withConnection(buildAction, progress.newChild(1));
                buildActionResults.put(buildActionContribution.getId(), result);
            } catch (Exception e) {
                if (EclipseProjectQuery.BUILD_ACTION_ID.equals(buildActionContribution.getId())) {
                    // the synchronization can't continue if we can't get the EclipseProject model
                    throw e;
                } else {
                    failures.add(SynchronizationProblem.newError(buildActionContribution.getId(), markerLocation(gradleBuild), "Failed to load build action " + buildActionContribution.getId(), e));
                    progress.worked(1);
                }
            }
        }
        return new ConfiguratorBuildActions(buildActionResults);
    }

    private static IResource markerLocation(GradleBuild gradleBuild) {
        Optional<IProject> projectOrNull = CorePlugin.workspaceOperations().findProjectByLocation(((InternalGradleBuild) gradleBuild).getBuildConfig().getRootProjectDirectory());
        return projectOrNull.isPresent() ? projectOrNull.get() : ResourcesPlugin.getWorkspace().getRoot();
    }
}

// TODO (donat) there's a duplication in the console output when loading the models