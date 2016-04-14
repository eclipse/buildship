/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace.internal;

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.SingleBuildModelRepository;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.workspace.GradleBuild;
import org.eclipse.buildship.core.workspace.ModelProvider;

/**
 * Default implementation of {@link GradleBuild}.
 *
 * @author Stefan Oehme
 */
public class DefaultGradleBuild implements GradleBuild {

    private final FixedRequestAttributes attributes;

    public DefaultGradleBuild(FixedRequestAttributes attributes) {
        this.attributes = Preconditions.checkNotNull(attributes);
    }

    @Override
    public ModelProvider getModelProvider() {
        SingleBuildModelRepository modelRepository = CorePlugin.modelRepositoryProvider().getModelRepository(this.attributes);
        return new DefaultModelprovider(modelRepository);
    }

}
