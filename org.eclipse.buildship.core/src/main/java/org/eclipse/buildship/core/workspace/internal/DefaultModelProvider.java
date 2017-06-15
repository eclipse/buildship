/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace.internal;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gradle.tooling.BuildActionExecuter;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.gradle.GradleBuild;
import org.gradle.util.GradleVersion;

import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.internal.DefaultOmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.repository.internal.DefaultOmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.internal.DefaultOmniGradleBuild;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.workspace.ModelProvider;

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
    public <T> T fetchModel(Class<T> model, FetchStrategy strategy, CancellationToken token, IProgressMonitor monitor) {
        TransientRequestAttributes transientAttributes = getTransientRequestAttributes(token, monitor);
        ModelBuilder<T> builder = ConnectionAwareLauncherProxy.newModelBuilder(model, this.buildConfiguration.toGradleArguments(), transientAttributes);
        return executeModelBuilder(builder, strategy, model);
    }

    @Override
    public <T> Collection<T> fetchModels(Class<T> model, FetchStrategy strategy, CancellationToken token, IProgressMonitor monitor) {
        TransientRequestAttributes transientAttributes = getTransientRequestAttributes(token, monitor);
        if (supportsCompositeBuilds(token, monitor)) {
            final BuildActionExecuter<Collection<T>> executer = ConnectionAwareLauncherProxy
                    .newCompositeModelQueryExecuter(model, DefaultModelProvider.this.buildConfiguration.toGradleArguments(), transientAttributes);
            return executeBuildActionExecuter(executer, strategy, model);
        } else {
            ModelBuilder<T> builder = ConnectionAwareLauncherProxy.newModelBuilder(model, this.buildConfiguration.toGradleArguments(), transientAttributes);
            return ImmutableList.of(executeModelBuilder(builder, strategy, model));
        }
    }

    @Override
    public OmniBuildEnvironment fetchBuildEnvironment(FetchStrategy strategy, CancellationToken token, IProgressMonitor monitor) {
        BuildEnvironment model = fetchModel(BuildEnvironment.class, strategy, token, monitor);
        return DefaultOmniBuildEnvironment.from(model);
    }

    @Override
    public OmniGradleBuild fetchGradleBuild(FetchStrategy strategy, CancellationToken token, IProgressMonitor monitor) {
        GradleBuild model = fetchModel(GradleBuild.class, strategy, token, monitor);
        return DefaultOmniGradleBuild.from(model);
    }

    @Override
    public Set<OmniEclipseProject> fetchEclipseGradleProjects(FetchStrategy strategy, CancellationToken token, IProgressMonitor monitor) {
        Collection<EclipseProject> models = fetchModels(EclipseProject.class, strategy, token, monitor);
        ImmutableSet.Builder<OmniEclipseProject> result = ImmutableSet.builder();
        for (EclipseProject model : models) {
            result.addAll(DefaultOmniEclipseProject.from(model).getAll());
        }
        return result.build();
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

        final AtomicBoolean modelLoaded = new AtomicBoolean(false);
        T value = getFromCache(cacheKey, new Callable<T>() {

            @Override
            public T call() {
                T model = operation.get();
                modelLoaded.set(true);
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
            throw new GradlePluginsRuntimeException(e);
        }
    }

    private boolean supportsCompositeBuilds(CancellationToken token, IProgressMonitor monitor) {
        BuildEnvironment buildEnvironment = fetchModel(BuildEnvironment.class, FetchStrategy.FORCE_RELOAD, token, monitor);
        GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
        return gradleVersion.getBaseVersion().compareTo(GradleVersion.version("3.3")) >= 0;
    }

    private static TransientRequestAttributes getTransientRequestAttributes(CancellationToken token, IProgressMonitor monitor) {
        ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        List<ProgressListener> progressListeners = ImmutableList.<ProgressListener>of(DelegatingProgressListener.withoutDuplicateLifecycleEvents(monitor));
        ImmutableList<org.gradle.tooling.events.ProgressListener> noEventListeners = ImmutableList.<org.gradle.tooling.events.ProgressListener>of();
        if (token == null) {
            token = GradleConnector.newCancellationTokenSource().token();
        }
        return new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), streams.getInput(), progressListeners, noEventListeners, token);
    }
}