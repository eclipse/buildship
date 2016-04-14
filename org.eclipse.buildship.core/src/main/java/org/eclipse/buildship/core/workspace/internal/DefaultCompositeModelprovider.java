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

import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace;
import com.gradleware.tooling.toolingmodel.repository.CompositeBuildModelRepository;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.workspace.CompositeModelProvider;

/**
 * Default implementation of {@link CompositeModelProvider}.
 *
 * @author Stefan Oehme
 */
final class DefaultCompositeModelprovider extends AbstractModelProvider implements CompositeModelProvider {

    private final CompositeBuildModelRepository modelRepository;

    public DefaultCompositeModelprovider(CompositeBuildModelRepository modelRepository) {
        this.modelRepository = Preconditions.checkNotNull(modelRepository);
    }

    @Override
    public OmniEclipseWorkspace fetchEclipseWorkspace(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        return this.modelRepository.fetchEclipseWorkspace(getTransientRequestAttributes(token, monitor), fetchStrategy);
    }

}
