/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildActionExecuter;
import org.gradle.tooling.BuildActionExecuter.Builder;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.gradle.tooling.TestLauncher;

import com.google.common.cache.Cache;

final class CachingProjectConnection implements ProjectConnection {

    private final ProjectConnection delegate;
    private final Cache<Object, Object> cache;

    public CachingProjectConnection(ProjectConnection delegate, Cache<Object, Object> cache) {
        this.delegate = delegate;
        this.cache = cache;
    }

    @Override
    public <T> T getModel(Class<T> modelType) throws GradleConnectionException, IllegalStateException {
        return model(modelType).get();
    }

    @Override
    public <T> void getModel(Class<T> modelType, ResultHandler<? super T> handler) throws IllegalStateException {
        model(modelType).get(handler);
    }

    @Override
    public BuildLauncher newBuild() {
        return this.delegate.newBuild();
    }

    @Override
    public TestLauncher newTestLauncher() {
        return this.delegate.newTestLauncher();
    }

    @Override
    public <T> ModelBuilder<T> model(Class<T> modelType) {
        return new CachingModelBuilder<>(this.delegate.model(modelType), this.cache, modelType);
    }

    @Override
    public <T> BuildActionExecuter<T> action(BuildAction<T> buildAction) {
        return new CachingBuildActionExecuter<>(this.delegate.action(buildAction), buildAction, this.cache);
    }

    @Override
    public Builder action() {
        return this.delegate.action();
    }

    @Override
    public void close() {
        this.delegate.close();
    }
}
