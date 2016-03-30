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
import com.gradleware.tooling.toolingmodel.repository.SimpleModelRepository;

import org.eclipse.buildship.core.CorePlugin;
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

    public DefaultGradleBuild(FixedRequestAttributes attributes) {
        this.attributes = Preconditions.checkNotNull(attributes);
    }

    @Override
    public void create(NewProjectHandler newProjectHandler, AsyncHandler initializer) {
        Preconditions.checkArgument(initializer != AsyncHandler.NO_OP, "Can't create projects with a no-op initializer");
        Preconditions.checkArgument(newProjectHandler != NewProjectHandler.NO_OP, "Can't import projects with a no-op handler");
        new SynchronizeGradleBuildJob(this.attributes, newProjectHandler, initializer, true).schedule();
    }

    @Override
    public void synchronize(NewProjectHandler newProjectHandler) {
        new SynchronizeGradleBuildJob(this.attributes, newProjectHandler, AsyncHandler.NO_OP, true).schedule();
    }

    @Override
    public ModelProvider getModelProvider() {
        SimpleModelRepository modelRepository = CorePlugin.modelRepositoryProvider().getModelRepository(this.attributes);
        return new DefaultModelprovider(modelRepository);
    }

}
