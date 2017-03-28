/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace.internal;

import java.beans.Transient;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.gradle.tooling.BuildActionExecuter;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.gradle.GradleBuild;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.internal.DefaultOmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.repository.internal.DefaultOmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.internal.DefaultOmniGradleBuild;
import com.gradleware.tooling.toolingmodel.repository.internal.DefaultOmniGradleProject;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.workspace.ModelProvider;

/**
 * Default implementation of {@link ModelProvider}.
 *
 * @author Stefan Oehme
 */
final class DefaultModelProvider implements ModelProvider {

    private final FixedRequestAttributes fixedAttributes;

    public DefaultModelProvider(FixedRequestAttributes fixedAttributes) {
        this.fixedAttributes = Preconditions.checkNotNull(fixedAttributes);
    }

    @Override
    public OmniBuildEnvironment fetchBuildEnvironment(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        ModelBuilder<BuildEnvironment> builder = newModelBuilder(BuildEnvironment.class, token, monitor);
        return DefaultOmniBuildEnvironment.from(builder.get());
    }

    @Override
    public OmniGradleBuild fetchGradleBuild(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        ModelBuilder<GradleBuild> builder = newModelBuilder(GradleBuild.class, token, monitor);
        return DefaultOmniGradleBuild.from(builder.get());
    }

    @Override
    public Set<OmniGradleProject> fetchGradleProjects(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        BuildActionExecuter<Collection<GradleProject>> action = newCompositeModelCollectorAction(GradleProject.class, token, monitor);
        ImmutableSet.Builder<OmniGradleProject> projects = ImmutableSet.builder();
        for (GradleProject gradleProject : action.run()) {
            projects.addAll(DefaultOmniGradleProject.from(gradleProject).getAll());
        }
        return projects.build();
    }

    @Override
    public Set<OmniEclipseProject> fetchEclipseGradleProjects(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        BuildActionExecuter<Collection<EclipseProject>> action = newCompositeModelCollectorAction(EclipseProject.class, token, monitor);
        ImmutableSet.Builder<OmniEclipseProject> projects = ImmutableSet.builder();
        for (EclipseProject eclipseProject : action.run()) {
            projects.addAll(DefaultOmniEclipseProject.from(eclipseProject).getAll());
        }
        return projects.build();
    }

    private <T> ModelBuilder<T> newModelBuilder(Class<T> model, CancellationToken token, IProgressMonitor monitor) {
        ModelBuilder<T> modelBuilder = ConnectionAwareLauncherProxy.newModelBuilder(openConnection(this.fixedAttributes), model);
        applyRequestAttributes(modelBuilder, token, monitor);
        return modelBuilder;
    }

    private <T> BuildActionExecuter<Collection<T>> newCompositeModelCollectorAction(Class<T> model, CancellationToken token, IProgressMonitor monitor) {
        BuildActionExecuter<Collection<T>> action = ConnectionAwareLauncherProxy.newCompositeModelQueryExecuter(openConnection(this.fixedAttributes), model);
        applyRequestAttributes(action, token, monitor);
        return action;

    }

    private void applyRequestAttributes(LongRunningOperation operation, CancellationToken token, IProgressMonitor monitor) {
        // fixed attributes
        operation.setJavaHome(this.fixedAttributes.getJavaHome());
        operation.withArguments(this.fixedAttributes.getArguments());
        operation.setJvmArguments(this.fixedAttributes.getJvmArguments());

        // transient attributes
        TransientRequestAttributes transientAttributes = getTransientRequestAttributes(token, monitor);
        operation.setStandardOutput(transientAttributes.getStandardOutput());
        operation.setStandardError(transientAttributes.getStandardError());
        operation.setStandardInput(transientAttributes.getStandardInput());
        for (ProgressListener listener : transientAttributes.getProgressListeners()) {
            operation.addProgressListener(listener);
        }
        operation. withCancellationToken(transientAttributes.getCancellationToken());
    }


    private static ProjectConnection openConnection(FixedRequestAttributes fixedAttributes) {
        GradleConnector connector = GradleConnector.newConnector().forProjectDirectory(fixedAttributes.getProjectDir());
        GradleDistributionWrapper.from(fixedAttributes.getGradleDistribution()).apply(connector);
        connector.useGradleUserHomeDir(fixedAttributes.getGradleUserHome());
        return connector.connect();
    }

    private final TransientRequestAttributes getTransientRequestAttributes(CancellationToken token, IProgressMonitor monitor) {
        ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        List<ProgressListener> progressListeners = ImmutableList.<ProgressListener> of(DelegatingProgressListener.withoutDuplicateLifecycleEvents(monitor));
        ImmutableList<org.gradle.tooling.events.ProgressListener> noEventListeners = ImmutableList.<org.gradle.tooling.events.ProgressListener> of();
        if (token == null) {
            token = GradleConnector.newCancellationTokenSource().token();
        }
        return new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), streams.getInput(), progressListeners, noEventListeners, token);
    }

}
