/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import java.util.function.Function;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import com.google.common.base.Preconditions;

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
import org.eclipse.buildship.core.internal.configuration.GradleArguments;
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
        SynchronizeOperation operation = new SynchronizeOperation(newProjectHandler);
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

    @Override
    public <T> T withConnection(Function<ProjectConnection, ? extends T> action, IProgressMonitor monitor) throws Exception {
        Preconditions.checkNotNull(action);
        monitor = monitor != null ? monitor : new NullProgressMonitor();

        GradleConnectionOperation<T> operation = new GradleConnectionOperation<>(action);
        try {
            CorePlugin.operationManager().run(operation, GradleConnector.newCancellationTokenSource(), monitor);
            return operation.result;
        } catch (CoreException e) {
            if (e.getStatus().getException() instanceof Exception) {
                throw (Exception) e.getStatus().getException();
            } else {
                throw e;
            }
        }
    }

    /**
     * Executes the synchronization on the target Gradle build.
     */
    private class SynchronizeOperation extends BaseToolingApiOperation {

        private final NewProjectHandler newProjectHandler;

        public SynchronizeOperation(NewProjectHandler newProjectHandler) {
            super("Synchronize project " + DefaultGradleBuild.this.gradleBuild.getBuildConfig().getRootProjectDirectory().getName());
            this.newProjectHandler = newProjectHandler;
        }

        @Override
        public void runInToolingApi(CancellationTokenSource tokenSource, IProgressMonitor monitor) throws Exception {
            DefaultGradleBuild.this.gradleBuild.synchronize(this.newProjectHandler, tokenSource, monitor);
        }

        @Override
        public ISchedulingRule getRule() {
            return ResourcesPlugin.getWorkspace().getRoot();
        }
    }

    private class GradleConnectionOperation<T> extends BaseToolingApiOperation {

        private final Function<ProjectConnection, ? extends T> action;
        private T result;

        public GradleConnectionOperation(Function<ProjectConnection, ? extends T> action) {
            super("Connecting to Gradle");
            this.action = action;
        }

        @Override
        public void runInToolingApi(CancellationTokenSource tokenSource, IProgressMonitor monitor) throws Exception {
            // TODO (donat) use AutoCloseable once we update to Tooling API 5.0
            ProjectConnection connection = IdeAttachedProjectConnection.newInstance(tokenSource, getGradleArguments(), monitor);
            try {
                this.result = this.action.apply(connection);
            } finally {
                connection.close();
            }
        }

        private GradleArguments getGradleArguments() {
            org.eclipse.buildship.core.internal.configuration.BuildConfiguration config = DefaultGradleBuild.this.gradleBuild.getBuildConfig();
            return config.toGradleArguments();
        }

        @Override
        public ISchedulingRule getRule() {
            return ResourcesPlugin.getWorkspace().getRoot();
        }
    }
}
