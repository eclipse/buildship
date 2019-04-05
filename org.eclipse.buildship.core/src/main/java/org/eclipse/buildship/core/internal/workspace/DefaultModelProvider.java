/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.internal.workspace;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.model.build.BuildEnvironment;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.UncheckedExecutionException;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.util.gradle.BuildActionUtil;
import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;

/**
 * Default implementation of {@link ModelProvider}.
 *
 * @author Stefan Oehme
 */
public final class DefaultModelProvider implements ModelProvider {

    private final GradleBuild gradleBuild;
    private final Cache<Object, Object> cache = CacheBuilder.newBuilder().build();

    public DefaultModelProvider(GradleBuild gradleBuild) {
        this.gradleBuild = gradleBuild;
    }

    @Override
    public <T> T fetchModel(Class<T> model, FetchStrategy strategy, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        return executeModelQuery(model, monitor, strategy, model);
    }

    @Override
    public <T> Collection<T> fetchModels(Class<T> model, FetchStrategy strategy, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        if (supportsCompositeBuilds(tokenSource, monitor)) {
            return executeCompositeModelQuery(model, monitor, strategy, model);
        } else {
            return ImmutableList.of(fetchModel(model, strategy, tokenSource, monitor));
        }
    }

    private <T> T executeModelQuery(final Class<T> model, final IProgressMonitor monitor, FetchStrategy fetchStrategy, Class<?> cacheKey) {
        return executeOperation(new Callable<T>() {

            @Override
            public T call() throws Exception {
                return DefaultModelProvider.this.gradleBuild.withConnection(connection -> connection.getModel(model), monitor);
            }
        }, fetchStrategy, cacheKey);
    }

    private <T> Collection<T> executeCompositeModelQuery(Class<T> model, final IProgressMonitor monitor, FetchStrategy fetchStrategy, Class<?> cacheKey) {
        return executeOperation(new Callable<Collection<T>>() {

            @Override
            public Collection<T> call() throws Exception {
                BuildAction<Collection<T>> query = BuildActionUtil.compositeModelQuery(model);
                return DefaultModelProvider.this.gradleBuild.withConnection(connection -> connection.action(query).run(), monitor);
            }
        }, fetchStrategy, cacheKey);
    }

    private <T> T executeOperation(final Callable<T> operation, FetchStrategy fetchStrategy, Class<?> cacheKey) {
        if (FetchStrategy.FROM_CACHE_ONLY == fetchStrategy) {
            @SuppressWarnings("unchecked")
            T result = (T) this.cache.getIfPresent(cacheKey);
            return result;
        }

        if (FetchStrategy.FORCE_RELOAD == fetchStrategy) {
            this.cache.invalidate(cacheKey);
        }

        T value = getFromCache(cacheKey, operation);

        return value;
    }

    private <U> U getFromCache(Class<?> cacheKey, Callable<U> cacheValueLoader) {
        try {
            @SuppressWarnings("unchecked")
            U result = (U) this.cache.get(cacheKey, cacheValueLoader);
            return result;
        } catch (Exception e) {
            if (e instanceof UncheckedExecutionException && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException)e.getCause();
            } else {
                throw new GradlePluginsRuntimeException(e);
            }
        }
    }

    private boolean supportsCompositeBuilds(CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        BuildEnvironment buildEnvironment = fetchModel(BuildEnvironment.class, FetchStrategy.FORCE_RELOAD, tokenSource, monitor);
        GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
        return gradleVersion.getBaseVersion().compareTo(GradleVersion.version("3.3")) >= 0;
    }
}