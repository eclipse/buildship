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

import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.SimpleModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.workspace.ModelProvider;

/**
 * {@link ModelProvider} implementation backed by a {@link SimpleModelRepository}.
 *
 * @author Stefan Oehme
 */
class DefaultModelprovider implements ModelProvider {

    private final SimpleModelRepository repository;

    DefaultModelprovider(SimpleModelRepository repository) {
        this.repository = repository;
    }

    @Override
    public OmniEclipseGradleBuild fetchEclipseGradleBuild(FetchStrategy fetchStrategy, IProgressMonitor monitor, CancellationToken token) {
        return this.repository.fetchEclipseGradleBuild(getTransientRequestAttributes(monitor, token), fetchStrategy);
    }

    @Override
    public OmniBuildEnvironment fetchBuildEnvironment(FetchStrategy fetchStrategy, IProgressMonitor monitor, CancellationToken token) {
        return this.repository.fetchBuildEnvironment(getTransientRequestAttributes(monitor, token), fetchStrategy);
    }

    private TransientRequestAttributes getTransientRequestAttributes(IProgressMonitor monitor, CancellationToken token) {
        ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        List<ProgressListener> progressListeners = ImmutableList.<ProgressListener> of(DelegatingProgressListener.withoutDuplicateLifecycleEvents(monitor));
        ImmutableList<org.gradle.tooling.events.ProgressListener> noEventListeners = ImmutableList.<org.gradle.tooling.events.ProgressListener> of();
        if (token == null) {
            token = GradleConnector.newCancellationTokenSource().token();
        }
        return new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), streams.getInput(), progressListeners, noEventListeners, token);
    }

}
