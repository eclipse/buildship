/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.internal.workspace;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.util.concurrent.UncheckedExecutionException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;
import org.eclipse.buildship.core.internal.util.gradle.ModelUtils;

/**
 * Default implementation of {@link ModelProvider}.
 *
 * @author Stefan Oehme
 */
public final class DefaultModelProvider implements ModelProvider {

    private static URLClassLoader ideFriendlyCustomActionClassLoader;

    private final GradleBuild gradleBuild;
    private final Cache<Object, Object> cache = CacheBuilder.newBuilder().build();

    public DefaultModelProvider(GradleBuild gradleBuild) {
        this.gradleBuild = gradleBuild;
    }

    @Override
    public <T> T fetchModel(Class<T> model, FetchStrategy strategy, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        return injectCompatibilityModel(executeModelQuery(model, monitor, strategy, model));
    }

    @Override
    public <T> Collection<T> fetchModels(Class<T> model, FetchStrategy strategy, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        if (supportsCompositeBuilds(tokenSource, monitor)) {
            return injectCompatibilityModel(model, executeCompositeModelQuery(model, monitor, strategy, model));
        } else {
            return ImmutableList.of(fetchModel(model, strategy, tokenSource, monitor));
        }
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
                BuildAction<Collection<T>> query = compositeModelQuery(model);
                return DefaultModelProvider.this.gradleBuild.withConnection(connection -> connection.action(query).run(), monitor);
            }
        }, fetchStrategy, cacheKey);
    }

    private static <T> BuildAction<Collection<T>> compositeModelQuery(Class<T> model) {
        if (Platform.inDevelopmentMode()) {
            return ideFriendlyCompositeModelQuery(model);
        } else {
            return new CompositeModelQuery<>(model);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> BuildAction<Collection<T>> ideFriendlyCompositeModelQuery(Class<T> model) {
        // When Buildship is launched from the IDE - as an Eclipse application or as a plug-in
        // test - the URLs returned by the Equinox class loader is incorrect. This means, the
        // Tooling API is unable to find the referenced build actions and fails with a CNF
        // exception. To work around that, we look up the build action class locations and load the
        // classes via an isolated URClassLoader.
        try {
            ClassLoader coreClassloader = DefaultModelProvider.class.getClassLoader();
            ClassLoader tapiClassloader = ProjectConnection.class.getClassLoader();
            URL actionRootUrl = FileLocator.resolve(coreClassloader.getResource(""));
            if (ideFriendlyCustomActionClassLoader == null) {
                ideFriendlyCustomActionClassLoader = new URLClassLoader(new URL[] { actionRootUrl }, tapiClassloader);
            }
            Class<?> actionClass = ideFriendlyCustomActionClassLoader.loadClass(CompositeModelQuery.class.getName());
            return (BuildAction<Collection<T>>) actionClass.getConstructor(Class.class).newInstance(model);
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(e);
        }
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