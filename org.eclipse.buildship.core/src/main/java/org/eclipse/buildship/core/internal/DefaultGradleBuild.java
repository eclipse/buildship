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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.SynchronizationResult;
import org.eclipse.buildship.core.internal.workspace.NewProjectHandler;

public final class DefaultGradleBuild implements GradleBuild {

    private final IProject project;
    private final CancellationTokenSource tokenSource;

    public DefaultGradleBuild(IProject project) {
        this.project = Preconditions.checkNotNull(project);
        this.tokenSource = GradleConnector.newCancellationTokenSource();
    }

    @Override
    public SynchronizationResult synchronize(IProgressMonitor monitor) {
        Optional<org.eclipse.buildship.core.internal.workspace.GradleBuild> buildOrNull = CorePlugin.gradleWorkspaceManager().getGradleBuild(this.project);
        if (buildOrNull.isPresent()) {
            org.eclipse.buildship.core.internal.workspace.GradleBuild gradleBuild = buildOrNull.get();
            try {
                gradleBuild.synchronize(NewProjectHandler.IMPORT_AND_MERGE, this.tokenSource, monitor);
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
