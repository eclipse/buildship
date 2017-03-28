/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace.internal;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.TestLauncher;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.workspace.GradleBuild;
import org.eclipse.buildship.core.workspace.ModelProvider;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Default implementation of {@link GradleBuild}.
 *
 * @author Stefan Oehme
 */
public class DefaultGradleBuild implements GradleBuild {

    private final FixedRequestAttributes attributes;

    public DefaultGradleBuild(FixedRequestAttributes builds) {
        this.attributes = Preconditions.checkNotNull(builds);
    }

    @Override
    public void synchronize() {
        synchronize(NewProjectHandler.NO_OP);
    }
    @Override
    public void synchronize(NewProjectHandler newProjectHandler) {
        synchronize(newProjectHandler, AsyncHandler.NO_OP);
    }
    @Override
    public void synchronize(NewProjectHandler newProjectHandler, AsyncHandler initializer) {
        SynchronizeGradleBuildsJob.forSingleGradleBuild(this, newProjectHandler, initializer).schedule();
    }

    @Override
    public boolean isSyncRunning() {
        Job[] syncJobs = Job.getJobManager().find(CorePlugin.GRADLE_JOB_FAMILY);
        for (Job job : syncJobs) {
            if (job instanceof SynchronizeGradleBuildsJob) {
                for (GradleBuild gradleBuild : ((SynchronizeGradleBuildsJob) job).getBuilds()) {
                    if (gradleBuild.getRequestAttributes().getProjectDir().equals(this.attributes.getProjectDir())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public ModelProvider getModelProvider() {
        return new DefaultModelProvider(this.attributes);
    }

    @Override
    public FixedRequestAttributes getRequestAttributes() {
        return this.attributes;
    }

    @Override
    public BuildLauncher newBuildLauncher(TransientRequestAttributes transientAttributes) {
        BuildLauncher launcher = ConnectionAwareLauncherProxy.newBuildLauncher(openConnection(this.attributes));
        applyRequestAttributes(launcher, this.attributes, transientAttributes);
        return launcher;
    }

    @Override
    public TestLauncher newTestLauncher(TransientRequestAttributes transientAttributes) {
        TestLauncher launcher = ConnectionAwareLauncherProxy.newTestLauncher(openConnection(this.attributes));
        applyRequestAttributes(launcher, this.attributes, transientAttributes);
        return launcher;
    }

    private static ProjectConnection openConnection(FixedRequestAttributes fixedAttributes) {
        GradleConnector connector = GradleConnector.newConnector().forProjectDirectory(fixedAttributes.getProjectDir());
        GradleDistributionWrapper.from(fixedAttributes.getGradleDistribution()).apply(connector);
        connector.useGradleUserHomeDir(fixedAttributes.getGradleUserHome());
        return connector.connect();
    }

    private void applyRequestAttributes(LongRunningOperation operation, FixedRequestAttributes fixedAttributes, TransientRequestAttributes transientAttributes) {
        // fixed attributes
        operation.setJavaHome(fixedAttributes.getJavaHome());
        operation.withArguments(fixedAttributes.getArguments());
        operation.setJvmArguments(fixedAttributes.getJvmArguments());

        // transient attributes
        operation.setStandardOutput(transientAttributes.getStandardOutput());
        operation.setStandardError(transientAttributes.getStandardError());
        operation.setStandardInput(transientAttributes.getStandardInput());
        for (ProgressListener listener : transientAttributes.getProgressListeners()) {
            operation.addProgressListener(listener);
        }
        operation. withCancellationToken(transientAttributes.getCancellationToken());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultGradleBuild) {
            DefaultGradleBuild other = (DefaultGradleBuild) obj;
            return Objects.equal(this.attributes, other.attributes);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.attributes);
    }
}
