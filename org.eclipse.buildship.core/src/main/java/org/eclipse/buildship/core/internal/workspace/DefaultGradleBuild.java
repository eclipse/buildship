/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.internal.workspace;

import java.util.Set;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.TestLauncher;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.configuration.RunConfiguration;
import org.eclipse.buildship.core.internal.gradle.GradleProgressAttributes;
import org.eclipse.buildship.core.internal.marker.GradleMarkerManager;
import org.eclipse.buildship.core.internal.operation.ToolingApiStatus;

/**
 * Default implementation of {@link GradleBuild}.
 *
 * @author Stefan Oehme
 */
public class DefaultGradleBuild implements GradleBuild {

    private final BuildConfiguration buildConfig;
    private final DefaultModelProvider modelProvider;

    public DefaultGradleBuild(BuildConfiguration buildConfig) {
        this.buildConfig = Preconditions.checkNotNull(buildConfig);
        this.modelProvider = new DefaultModelProvider(this.buildConfig);
    }

    @Override
    public void synchronize(NewProjectHandler newProjectHandler, CancellationTokenSource tokenSource, IProgressMonitor monitor) throws CoreException {
        try {
            GradleMarkerManager.clear(this);
            doSynchronize(newProjectHandler, tokenSource, monitor);
        } catch (Exception e) {
            ToolingApiStatus status = ToolingApiStatus.from("Project synchronization" , e);
            if (status.severityMatches(IStatus.WARNING | IStatus.ERROR)) {
                GradleMarkerManager.addError(this, status);
            }
            throw e;
        }
    }

    private void doSynchronize(NewProjectHandler newProjectHandler, CancellationTokenSource tokenSource, IProgressMonitor monitor) throws CoreException {
        SubMonitor progress = SubMonitor.convert(monitor, 5);
        progress.setTaskName((String.format("Synchronizing Gradle build at %s with workspace", this.buildConfig.getRootProjectDirectory())));
        new ImportRootProjectOperation(this.buildConfig, newProjectHandler).run(progress.newChild(1));
        Set<EclipseProject> allProjects = ModelProviderUtil.fetchAllEclipseProjects(this, tokenSource, FetchStrategy.FORCE_RELOAD, progress.newChild(1));
        new ValidateProjectLocationOperation(allProjects).run(progress.newChild(1));
        new RunOnImportTasksOperation(allProjects, this.buildConfig).run(progress.newChild(1), tokenSource);
        new SynchronizeGradleBuildOperation(allProjects, this, newProjectHandler, ProjectConfigurators.create(this, CorePlugin.extensionManager().loadConfigurators())).run(progress.newChild(1));
    }

    @Override
    public ModelProvider getModelProvider() {
        return this.modelProvider;
    }

    @Override
    public BuildConfiguration getBuildConfig() {
        return this.buildConfig;
    }

    @Override
    public BuildLauncher newBuildLauncher(RunConfiguration runConfiguration, GradleProgressAttributes progressAttributes) {
        // TODO (donat) once GradleWorkspaceManager#getGradleBuild(FixedRequestAttributes) is removed then we should only allow run config that contain the same build config
        return ConnectionAwareLauncherProxy.newBuildLauncher(runConfiguration.toGradleArguments(), progressAttributes);
    }

    @Override
    public TestLauncher newTestLauncher(RunConfiguration runConfiguration, GradleProgressAttributes progressAttributes) {
        // TODO (donat) once GradleWorkspaceManager#getGradleBuild(FixedRequestAttributes) is removed then we should only allow run config that contain the same build config
        return ConnectionAwareLauncherProxy.newTestLauncher(runConfiguration.toGradleArguments(), progressAttributes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultGradleBuild) {
            DefaultGradleBuild other = (DefaultGradleBuild) obj;
            return Objects.equal(this.buildConfig, other.buildConfig);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.buildConfig);
    }

    public org.eclipse.buildship.core.GradleBuild toApiGradleBuild() {
        return new org.eclipse.buildship.core.internal.DefaultGradleBuild(this);
    }
}
