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
import org.gradle.tooling.connection.ModelResults;

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
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
    public ModelResults<OmniEclipseProject> fetchEclipseProjects(FetchStrategy fetchStrategy, CancellationToken token, IProgressMonitor monitor) {
        return this.modelRepository.fetchEclipseProjects(getTransientRequestAttributes(token, monitor), fetchStrategy);
    }

}
