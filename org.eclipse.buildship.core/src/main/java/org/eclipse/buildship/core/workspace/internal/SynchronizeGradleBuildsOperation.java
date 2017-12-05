/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.util.Set;

import org.gradle.tooling.CancellationToken;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.workspace.GradleBuild;
import org.eclipse.buildship.core.workspace.GradleBuilds;
import org.eclipse.buildship.core.workspace.ModelProvider;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Synchronizes each of the given Gradle builds with the workspace.
 */
public final class SynchronizeGradleBuildsOperation {

    private final ImmutableSet<GradleBuild> builds;
    private final NewProjectHandler newProjectHandler;
    private final AsyncHandler initializer;

    private SynchronizeGradleBuildsOperation(Set<GradleBuild> builds, NewProjectHandler newProjectHandler, AsyncHandler initializer) {
        this.builds = ImmutableSet.copyOf(builds);
        this.newProjectHandler = Preconditions.checkNotNull(newProjectHandler);
        this.initializer = Preconditions.checkNotNull(initializer);
    }

    Set<GradleBuild> getBuilds() {
        return this.builds;
    }

    protected void run(CancellationToken token, IProgressMonitor monitor) throws Exception {
        final SubMonitor progress = SubMonitor.convert(monitor, this.builds.size() + 1);

        // TODO (donat) This is the time to get rid of the initializer

        this.initializer.run(progress.newChild(1), token);

        for (GradleBuild build : this.builds) {
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
            synchronizeBuild(build, token, progress.newChild(1));
        }
    }

    private void synchronizeBuild(GradleBuild build, CancellationToken token, SubMonitor progress) throws CoreException {
        BuildConfiguration buildConfig = build.getBuildConfig();
        progress.setTaskName((String.format("Synchronizing Gradle build at %s with workspace", buildConfig.getRootProjectDirectory())));
        progress.setWorkRemaining(4);
        Set<OmniEclipseProject> allProjects = fetchEclipseProjects(build, token, progress.newChild(1));
        new ValidateProjectLocationOperation(allProjects).run(progress.newChild(1));
        new SynchronizeBuildConfigurationOperation(buildConfig).run(progress.newChild(1), token);
        new RunOnImportTasksOperation(allProjects, buildConfig).run(progress.newChild(1), token);
        new SynchronizeGradleBuildOperation(allProjects, buildConfig, SynchronizeGradleBuildsOperation.this.newProjectHandler).run(progress.newChild(1));
    }

    private Set<OmniEclipseProject> fetchEclipseProjects(GradleBuild build, CancellationToken token, SubMonitor progress) {
        progress.setTaskName("Loading Gradle project models");
        ModelProvider modelProvider = build.getModelProvider();
        return modelProvider.fetchEclipseGradleProjects(FetchStrategy.FORCE_RELOAD, token, progress);
    }

    public static SynchronizeGradleBuildsOperation forSingleGradleBuild(GradleBuild build, NewProjectHandler newProjectHandler, AsyncHandler initializer) {
        return new SynchronizeGradleBuildsOperation(ImmutableSet.of(build), newProjectHandler, initializer);
    }

    public static SynchronizeGradleBuildsOperation forMultipleGradleBuilds(GradleBuilds builds, NewProjectHandler newProjectHandler, AsyncHandler initializer) {
        return new SynchronizeGradleBuildsOperation(builds.getGradleBuilds(), newProjectHandler, initializer);
    }

}
