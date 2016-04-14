/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
