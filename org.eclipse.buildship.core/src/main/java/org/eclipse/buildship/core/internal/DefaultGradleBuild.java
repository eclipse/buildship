/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import org.eclipse.buildship.core.BuildConfiguration;
import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.SynchronizationResult;
import org.eclipse.buildship.core.internal.operation.BaseToolingApiOperation;
import org.eclipse.buildship.core.internal.workspace.NewProjectHandler;

public final class DefaultGradleBuild implements GradleBuild {

    private final org.eclipse.buildship.core.internal.workspace.GradleBuild gradleBuild;

    public DefaultGradleBuild(IProject project) {
        this.gradleBuild = CorePlugin.gradleWorkspaceManager().getGradleBuild(project).orNull();
    }

    public DefaultGradleBuild(BuildConfiguration configuration) {
        org.eclipse.buildship.core.internal.configuration.BuildConfiguration buildConfiguration = CorePlugin.configurationManager().createBuildConfiguration(
            configuration.getRootProjectDirectory(),
            configuration.isOverrideWorkspaceConfiguration(),
            configuration.getGradleDistribution(),
            configuration.getGradleUserHome().orElse(null),
            configuration.isBuildScansEnabled(),
            configuration.isOfflineMode(),
            configuration.isAutoSync());
        this.gradleBuild = CorePlugin.gradleWorkspaceManager().getGradleBuild(buildConfiguration);
    }

    @Override
    public SynchronizationResult synchronize(IProgressMonitor monitor) {
        return synchronize(NewProjectHandler.IMPORT_AND_MERGE, GradleConnector.newCancellationTokenSource(), monitor);
    }

    public SynchronizationResult synchronize(NewProjectHandler newProjectHandler, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        monitor = monitor != null ? monitor : new NullProgressMonitor();
        SynchronizeOperation operation = new SynchronizeOperation(this.gradleBuild, newProjectHandler);
        try {
            CorePlugin.operationManager().run(operation, tokenSource, monitor);
            return newSynchronizationResult(Status.OK_STATUS);
        } catch (CoreException e) {
            return newSynchronizationResult(e.getStatus());
        }
    }

    private static SynchronizationResult newSynchronizationResult(final IStatus result) {
        return new SynchronizationResult() {

            @Override
            public IStatus getStatus() {
                return result;
            }
        };
    }

    /**
     * Executes the synchronization on the target Gradle build.
     */
    private static class SynchronizeOperation extends BaseToolingApiOperation {

        private final org.eclipse.buildship.core.internal.workspace.GradleBuild gradleBuild;
        private final NewProjectHandler newProjectHandler;

        public SynchronizeOperation(org.eclipse.buildship.core.internal.workspace.GradleBuild gradleBuild, NewProjectHandler newProjectHandler) {
            super("Synchronize project " + gradleBuild.getBuildConfig().getRootProjectDirectory().getName());
            this.gradleBuild = gradleBuild;
            this.newProjectHandler = newProjectHandler;
        }

        @Override
        public void runInToolingApi(CancellationTokenSource tokenSource, IProgressMonitor monitor) throws Exception {
            this.gradleBuild.synchronize(this.newProjectHandler, tokenSource, monitor);
        }

        @Override
        public ISchedulingRule getRule() {
            return ResourcesPlugin.getWorkspace().getRoot();
        }
    }
}
