/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import java.util.Set;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;

/**
 * Synchronizes each of the given Gradle builds with the workspace.
 */
final class SynchronizeGradleBuildsOperation {

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
        Set<EclipseProject> allProjects = ModelProviderUtil.fetchAllEclipseProjects(this.build, tokenSource, FetchStrategy.FORCE_RELOAD, progress.newChild(1));
        new ValidateProjectLocationOperation(allProjects).run(progress.newChild(1));
        new RunOnImportTasksOperation(allProjects, buildConfig).run(progress.newChild(1), tokenSource);
        new SynchronizeGradleBuildOperation(allProjects, buildConfig, SynchronizeGradleBuildsOperation.this.newProjectHandler, ProjectConfigurators.create(this.build, CorePlugin.extensionManager().loadConfigurators())).run(progress.newChild(1));
    }

    public static SynchronizeGradleBuildsOperation forSingleGradleBuild(GradleBuild build, NewProjectHandler newProjectHandler) {
        return new SynchronizeGradleBuildsOperation(build, newProjectHandler);
    }
}
