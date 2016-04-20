/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace.internal;

import org.gradle.tooling.CancellationToken;

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.SingleBuildModelRepository;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.workspace.ModelProvider;

/**
 * {@link ModelProvider} implementation backed by a {@link SingleBuildModelRepository}.
 *
 * @author Stefan Oehme
 */
final class DefaultModelprovider extends AbstractModelProvider implements ModelProvider {

    private final SingleBuildModelRepository repository;

    DefaultModelprovider(SingleBuildModelRepository repository) {
        this.repository = Preconditions.checkNotNull(repository);
    }

    @Override
    public OmniGradleBuild fetchGradleBuild(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        return this.repository.fetchGradleBuild(getTransientRequestAttributes(token, monitor), fetchStrategy);
    }

    @Override
    public OmniEclipseGradleBuild fetchEclipseGradleBuild(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        return this.repository.fetchEclipseGradleBuild(getTransientRequestAttributes(token, monitor), fetchStrategy);
    }

    @Override
    public OmniBuildEnvironment fetchBuildEnvironment(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        return this.repository.fetchBuildEnvironment(getTransientRequestAttributes(token, monitor), fetchStrategy);
    }

}
