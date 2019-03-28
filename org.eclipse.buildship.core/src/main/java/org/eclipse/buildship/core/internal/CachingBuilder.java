/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildActionExecuter;
import org.gradle.tooling.BuildActionExecuter.Builder;
import org.gradle.tooling.IntermediateResultHandler;
import org.gradle.tooling.ProjectConnection;

import com.google.common.cache.Cache;

public class CachingBuilder implements BuildActionExecuter.Builder {

    private final Cache<Object, Object> cache;
    private final Builder delegate;

    public CachingBuilder(ProjectConnection connection, Cache<Object, Object> cache) {
        this.delegate = connection.action();
        this.cache = cache;
    }

    @Override
    public <T> Builder projectsLoaded(BuildAction<T> buildAction, IntermediateResultHandler<? super T> handler) throws IllegalArgumentException {
        this.delegate.projectsLoaded(buildAction, new CachingIntermediateResultHandler<>(buildAction, handler, this.cache));
        return this;
    }

    @Override
    public <T> Builder buildFinished(BuildAction<T> buildAction, IntermediateResultHandler<? super T> handler) throws IllegalArgumentException {
        this.delegate.buildFinished(buildAction, new CachingIntermediateResultHandler<>(buildAction, handler, this.cache));
        return this;
    }

    @Override
    public BuildActionExecuter<Void> build() {
        return this.delegate.build();
    }

}
