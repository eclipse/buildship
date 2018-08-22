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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.SynchronizationResult;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.workspace.NewProjectHandler;

public final class DefaultGradleBuild implements GradleBuild {

    private final org.eclipse.buildship.core.internal.workspace.GradleBuild gradleBuild;
    private final CancellationTokenSource tokenSource = GradleConnector.newCancellationTokenSource();

    public DefaultGradleBuild(IProject project) {
        this.gradleBuild = CorePlugin.gradleWorkspaceManager().getGradleBuild(project).orNull();
    }

    public DefaultGradleBuild(BuildConfiguration configuration) {
        org.eclipse.buildship.core.internal.configuration.BuildConfiguration buildConfiguration = CorePlugin.configurationManager().createBuildConfiguration(
            configuration.getRootProjectDirectory(),
            configuration.isOverrideWorkspaceConfiguration(),
            configuration.getGradleDistribution(),
            configuration.getGradleUserHome(),
            configuration.isBuildScansEnabled(),
            configuration.isOfflineMode(),
            configuration.isAutoSync());
        this.gradleBuild = CorePlugin.gradleWorkspaceManager().getGradleBuild(buildConfiguration);
    }

    @Override
    public SynchronizationResult synchronize(IProgressMonitor monitor) {
        if (this.gradleBuild != null) {
            try {
                this.gradleBuild.synchronize(NewProjectHandler.IMPORT_AND_MERGE, this.tokenSource, monitor);
            } catch (final CoreException e) {
                return newSynchronizationResult(e.getStatus());
            }
        }
        return newSynchronizationResult(Status.OK_STATUS);
    }

    private static SynchronizationResult newSynchronizationResult(final IStatus result) {
        return new SynchronizationResult() {

            @Override
            public IStatus getStatus() {
                return result;
            }
        };
    }
}
