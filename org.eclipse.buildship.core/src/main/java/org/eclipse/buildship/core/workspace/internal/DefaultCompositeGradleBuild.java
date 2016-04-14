/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace.internal;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.repository.CompositeBuildModelRepository;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.workspace.CompositeGradleBuild;
import org.eclipse.buildship.core.workspace.CompositeModelProvider;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Default implementation of {@link CompositeGradleBuild}.
 *
 * @author Stefan Oehme
 */
public class DefaultCompositeGradleBuild implements CompositeGradleBuild {

    private final ImmutableSet<FixedRequestAttributes> attributes;

    public DefaultCompositeGradleBuild(Set<FixedRequestAttributes> attributes) {
        this.attributes = ImmutableSet.copyOf(attributes);
    }

    @Override
    public void synchronize(NewProjectHandler newProjectHandler) {
        new SynchronizeGradleBuildsJob(this.attributes, newProjectHandler, AsyncHandler.NO_OP).schedule();
    }

    @Override
    public CompositeModelProvider getModelProvider() {
        CompositeBuildModelRepository modelRepository = CorePlugin.modelRepositoryProvider().getCompositeModelRepository(this.attributes);
        return new DefaultCompositeModelprovider(modelRepository);
    }
}
