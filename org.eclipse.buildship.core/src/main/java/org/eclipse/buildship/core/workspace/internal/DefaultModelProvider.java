/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace.internal;

import java.util.List;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.workspace.ModelProvider;

/**
 * Default implementation of {@link ModelProvider}.
 *
 * @author Stefan Oehme
 */
final class DefaultModelProvider implements ModelProvider {

    private final ModelRepository modelRepository;

    public DefaultModelProvider(ModelRepository singleModelRepository) {
        this.modelRepository = Preconditions.checkNotNull(singleModelRepository);
    }

    @Override
    public OmniGradleBuildStructure fetchGradleBuildStructure(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        return this.modelRepository.fetchGradleBuildStructure(getTransientRequestAttributes(token, monitor), fetchStrategy);
    }

    @Override
    public OmniGradleBuild fetchGradleBuild(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        return this.modelRepository.fetchGradleBuild(getTransientRequestAttributes(token, monitor), fetchStrategy);
    }

    @Override
    public OmniEclipseGradleBuild fetchEclipseGradleBuild(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        return this.modelRepository.fetchEclipseGradleBuild(getTransientRequestAttributes(token, monitor), fetchStrategy);
    }

    @Override
    public OmniBuildEnvironment fetchBuildEnvironment(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        return this.modelRepository.fetchBuildEnvironment(getTransientRequestAttributes(token, monitor), fetchStrategy);
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
