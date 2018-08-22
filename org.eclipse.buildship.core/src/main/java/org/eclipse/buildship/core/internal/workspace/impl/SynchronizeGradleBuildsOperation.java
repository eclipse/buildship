/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace.impl;

import java.util.Collection;
import java.util.Set;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.util.gradle.HierarchicalElementUtils;
import org.eclipse.buildship.core.internal.workspace.FetchStrategy;
import org.eclipse.buildship.core.internal.workspace.GradleBuild;
import org.eclipse.buildship.core.internal.workspace.ModelProvider;
import org.eclipse.buildship.core.internal.workspace.NewProjectHandler;

/**
 * Synchronizes each of the given Gradle builds with the workspace.
 */
public final class SynchronizeGradleBuildsOperation {

    private final GradleBuild build;
    private final NewProjectHandler newProjectHandler;

    private SynchronizeGradleBuildsOperation(GradleBuild build, NewProjectHandler newProjectHandler) {
        this.build = Preconditions.checkNotNull(build);
        this.newProjectHandler = Preconditions.checkNotNull(newProjectHandler);
    }

    protected void run(CancellationTokenSource tokenSource, IProgressMonitor monitor) throws CoreException {
        SubMonitor progress = SubMonitor.convert(monitor, 5);
        BuildConfiguration buildConfig = this.build.getBuildConfig();
        progress.setTaskName((String.format("Synchronizing Gradle build at %s with workspace", buildConfig.getRootProjectDirectory())));
        new ImportRootProjectOperation(buildConfig, this.newProjectHandler).run(progress.newChild(1));
        Set<EclipseProject> allProjects = fetchEclipseProjects(this.build, tokenSource, progress.newChild(1));
        new ValidateProjectLocationOperation(allProjects).run(progress.newChild(1));
        new RunOnImportTasksOperation(allProjects, buildConfig).run(progress.newChild(1), tokenSource);
        new SynchronizeGradleBuildOperation(allProjects, buildConfig, SynchronizeGradleBuildsOperation.this.newProjectHandler).run(progress.newChild(1));
    }

    private Set<EclipseProject> fetchEclipseProjects(GradleBuild build, CancellationTokenSource tokenSource, SubMonitor progress) {
        progress.setTaskName("Loading Gradle project models");
        ModelProvider modelProvider = build.getModelProvider();

        Collection<EclipseProject> models = modelProvider.fetchModels(EclipseProject.class, FetchStrategy.FORCE_RELOAD, tokenSource, progress);
        ImmutableSet.Builder<EclipseProject> result = ImmutableSet.builder();
        for (EclipseProject model : models) {
            result.addAll(HierarchicalElementUtils.getAll(model));
        }
        return result.build();
    }

    public static SynchronizeGradleBuildsOperation forSingleGradleBuild(GradleBuild build, NewProjectHandler newProjectHandler) {
        return new SynchronizeGradleBuildsOperation(build, newProjectHandler);
    }
}
