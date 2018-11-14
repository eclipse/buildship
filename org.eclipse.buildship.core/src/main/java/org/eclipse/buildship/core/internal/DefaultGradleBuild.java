/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.TestLauncher;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Preconditions;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import org.eclipse.buildship.core.BuildConfiguration;
import org.eclipse.buildship.core.SynchronizationResult;
import org.eclipse.buildship.core.internal.configuration.GradleArguments;
import org.eclipse.buildship.core.internal.configuration.RunConfiguration;
import org.eclipse.buildship.core.internal.gradle.GradleProgressAttributes;
import org.eclipse.buildship.core.internal.marker.GradleMarkerManager;
import org.eclipse.buildship.core.internal.operation.BaseToolingApiOperation;
import org.eclipse.buildship.core.internal.operation.ToolingApiStatus;
import org.eclipse.buildship.core.internal.workspace.ConnectionAwareLauncherProxy;
import org.eclipse.buildship.core.internal.workspace.DefaultModelProvider;
import org.eclipse.buildship.core.internal.workspace.FetchStrategy;
import org.eclipse.buildship.core.internal.workspace.ImportRootProjectOperation;
import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;
import org.eclipse.buildship.core.internal.workspace.ModelProvider;
import org.eclipse.buildship.core.internal.workspace.ModelProviderUtil;
import org.eclipse.buildship.core.internal.workspace.NewProjectHandler;
import org.eclipse.buildship.core.internal.workspace.ProjectConfigurators;
import org.eclipse.buildship.core.internal.workspace.RunOnImportTasksOperation;
import org.eclipse.buildship.core.internal.workspace.SynchronizeGradleBuildOperation;
import org.eclipse.buildship.core.internal.workspace.ValidateProjectLocationOperation;

public final class DefaultGradleBuild implements InternalGradleBuild {

    private static Map<DefaultGradleBuild, SynchronizeOperation> syncOperations = new ConcurrentHashMap<>();

    private final org.eclipse.buildship.core.internal.configuration.BuildConfiguration buildConfig;
    private final ModelProvider modelProvider;

    public DefaultGradleBuild(BuildConfiguration configuration) {
        this(CorePlugin.configurationManager().createBuildConfiguration(
            configuration.getRootProjectDirectory(),
            configuration.isOverrideWorkspaceConfiguration(),
            configuration.getGradleDistribution(),
            configuration.getGradleUserHome().orElse(null),
            configuration.isBuildScansEnabled(),
            configuration.isOfflineMode(),
            configuration.isAutoSync()));
    }

    public DefaultGradleBuild(org.eclipse.buildship.core.internal.configuration.BuildConfiguration buildConfiguration) {
        this.buildConfig = buildConfiguration;
        this.modelProvider = new DefaultModelProvider(this.buildConfig);
    }

    @Override
    public SynchronizationResult synchronize(IProgressMonitor monitor) {
        return synchronize(NewProjectHandler.IMPORT_AND_MERGE, GradleConnector.newCancellationTokenSource(), monitor);
    }

    public SynchronizationResult synchronize(NewProjectHandler newProjectHandler, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        monitor = monitor != null ? monitor : new NullProgressMonitor();

        SynchronizeOperation operation = new SynchronizeOperation(this, newProjectHandler);
        SynchronizeOperation runningOperation = syncOperations.putIfAbsent(this, operation);

        if (runningOperation != null && (newProjectHandler == NewProjectHandler.NO_OP || Objects.equals(newProjectHandler, runningOperation.newProjectHandler))) {
            return newSynchronizationResult(Status.OK_STATUS);
        }

        try {
            GradleMarkerManager.clear(this);
            CorePlugin.operationManager().run(operation, tokenSource, monitor);
            return newSynchronizationResult(Status.OK_STATUS);
        } catch (CoreException e) {
            ToolingApiStatus status = ToolingApiStatus.from("Project synchronization" , e);
            if (status.severityMatches(IStatus.WARNING | IStatus.ERROR)) {
              GradleMarkerManager.addError(this, status);
          }
            return newSynchronizationResult(e.getStatus());
        } finally {
            syncOperations.remove(this);
        }
    }

    public boolean isSynchronizing() {
        return syncOperations.containsKey(this);
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

    @Override
    public int hashCode() {
        return Objects.hash(this.buildConfig);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultGradleBuild other = (DefaultGradleBuild) obj;
        return Objects.equals(this.buildConfig, other.buildConfig);
    }

    /**
     * Executes the synchronization on the target Gradle build.
     */
    private static class SynchronizeOperation extends BaseToolingApiOperation {

        private final InternalGradleBuild gradleBuild;
        private final NewProjectHandler newProjectHandler;

        public SynchronizeOperation(InternalGradleBuild gradleBuild, NewProjectHandler newProjectHandler) {
            super("Synchronize project " + gradleBuild.getBuildConfig().getRootProjectDirectory().getName());
            this.gradleBuild = gradleBuild;
            this.newProjectHandler = newProjectHandler;
        }

        @Override
        public void runInToolingApi(CancellationTokenSource tokenSource, IProgressMonitor monitor) throws Exception {
            SubMonitor progress = SubMonitor.convert(monitor, 5);
            progress.setTaskName((String.format("Synchronizing Gradle build at %s with workspace", this.gradleBuild.getBuildConfig().getRootProjectDirectory())));
            new ImportRootProjectOperation(this.gradleBuild.getBuildConfig(), this.newProjectHandler).run(progress.newChild(1));
            Set<EclipseProject> allProjects = ModelProviderUtil.fetchAllEclipseProjects(this.gradleBuild, tokenSource, FetchStrategy.FORCE_RELOAD, progress.newChild(1));
            new ValidateProjectLocationOperation(allProjects).run(progress.newChild(1));
            new RunOnImportTasksOperation(allProjects, this.gradleBuild.getBuildConfig()).run(progress.newChild(1), tokenSource);
            new SynchronizeGradleBuildOperation(allProjects, this.gradleBuild, this.newProjectHandler, ProjectConfigurators.create(this.gradleBuild, CorePlugin.extensionManager().loadConfigurators())).run(progress.newChild(1));
        }

        @Override
        public ISchedulingRule getRule() {
            return ResourcesPlugin.getWorkspace().getRoot();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.gradleBuild, this.newProjectHandler);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            SynchronizeOperation other = (SynchronizeOperation) obj;
            return Objects.equals(this.gradleBuild, other.gradleBuild) && Objects.equals(this.newProjectHandler, other.newProjectHandler);
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
            return DefaultGradleBuild.this.buildConfig.toGradleArguments();
        }

        @Override
        public ISchedulingRule getRule() {
            return ResourcesPlugin.getWorkspace().getRoot();
        }
    }

    @Override
    public ModelProvider getModelProvider() {
        return this.modelProvider;
    }

    @Override
    public BuildLauncher newBuildLauncher(RunConfiguration runConfiguration, GradleProgressAttributes progressAttributes) {
        return ConnectionAwareLauncherProxy.newBuildLauncher(runConfiguration.toGradleArguments(), progressAttributes);
    }

    @Override
    public TestLauncher newTestLauncher(RunConfiguration runConfiguration, GradleProgressAttributes progressAttributes) {
        return ConnectionAwareLauncherProxy.newTestLauncher(runConfiguration.toGradleArguments(), progressAttributes);
    }

    @Override
    public org.eclipse.buildship.core.internal.configuration.BuildConfiguration getBuildConfig() {
        return this.buildConfig;
    }
}
