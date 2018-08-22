/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.internal.workspace.impl;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.gradle.tooling.BuildActionExecuter;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.util.concurrent.UncheckedExecutionException;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.gradle.GradleProgressAttributes;
import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;
import org.eclipse.buildship.core.internal.util.gradle.ModelUtils;
import org.eclipse.buildship.core.internal.workspace.FetchStrategy;
import org.eclipse.buildship.core.internal.workspace.ModelProvider;

/**
 * Default implementation of {@link ModelProvider}.
 *
 * @author Stefan Oehme
 */
final class DefaultModelProvider implements ModelProvider {

    private final BuildConfiguration buildConfiguration;
    private final Cache<Object, Object> cache = CacheBuilder.newBuilder().build();

    public DefaultModelProvider(BuildConfiguration buildConfiguration) {
        this.buildConfiguration = buildConfiguration;
    }

    @Override
    public <T> T fetchModel(Class<T> model, FetchStrategy strategy, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        GradleProgressAttributes progressAttributes = createProgressAttributes(tokenSource, monitor);
        ModelBuilder<T> builder = ConnectionAwareLauncherProxy.newModelBuilder(model, this.buildConfiguration.toGradleArguments(), progressAttributes);
        return injectCompatibilityModel(executeModelBuilder(builder, strategy, model));
    }

    @Override
    public <T> Collection<T> fetchModels(Class<T> model, FetchStrategy strategy, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        GradleProgressAttributes attributes = createProgressAttributes(tokenSource, monitor);
        if (supportsCompositeBuilds(tokenSource, monitor)) {
            final BuildActionExecuter<Collection<T>> executer = ConnectionAwareLauncherProxy
                    .newCompositeModelQueryExecuter(model, DefaultModelProvider.this.buildConfiguration.toGradleArguments(), attributes);
            Collection<T> result = executeBuildActionExecuter(executer, strategy, model);
            return injectCompatibilityModel(model, result);
        } else {
            ModelBuilder<T> builder = ConnectionAwareLauncherProxy.newModelBuilder(model, this.buildConfiguration.toGradleArguments(), attributes);
            Collection<T> result = ImmutableList.of(executeModelBuilder(builder, strategy, model));
            return injectCompatibilityModel(model, result);
        }
    }

    private GradleProgressAttributes createProgressAttributes(CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        return GradleProgressAttributes.builder(tokenSource, monitor)
                .forBackgroundProcess()
                .withFilteredProgress()
                .build();
    }

    @SuppressWarnings("unchecked")
    private static <T> T injectCompatibilityModel(T model) {
        if (model instanceof EclipseProject) {
            return (T) ModelUtils.createCompatibilityModel((EclipseProject) model);
        }

        return model;
    }

    @SuppressWarnings("unchecked")
    private static <T> Collection<T> injectCompatibilityModel(Class<T> modelClass, Collection<T> models) {
        if (modelClass == EclipseProject.class) {
            Builder<EclipseProject> result = ImmutableList.builder();
            for (T model : models) {
                result.add(ModelUtils.createCompatibilityModel((EclipseProject) model));
            }
            return (Collection<T>) result.build();
        }

        return models;
    }

    private <T> T executeBuildActionExecuter(final BuildActionExecuter<T> executer, FetchStrategy fetchStrategy, Class<?> cacheKey) {
        return executeOperation(new Supplier<T>() {

            @Override
            public T get() {
                return executer.run();
            }
        }, fetchStrategy, cacheKey);
    }

    private <T> T executeModelBuilder(final ModelBuilder<T> builder, FetchStrategy fetchStrategy, Class<?> cacheKey) {
        return executeOperation(new Supplier<T>() {

            @Override
            public T get() {
                return builder.get();
            }
        }, fetchStrategy, cacheKey);
    }

    private <T> T executeOperation(final Supplier<T> operation, FetchStrategy fetchStrategy, Class<?> cacheKey) {
        if (FetchStrategy.FROM_CACHE_ONLY == fetchStrategy) {
            @SuppressWarnings("unchecked")
            T result = (T) this.cache.getIfPresent(cacheKey);
            return result;
        }

        if (FetchStrategy.FORCE_RELOAD == fetchStrategy) {
            this.cache.invalidate(cacheKey);
        }

        T value = getFromCache(cacheKey, new Callable<T>() {

            @Override
            public T call() {
                T model = operation.get();
                return model;
            }
        });

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