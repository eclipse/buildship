/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import java.util.Map;
import java.util.concurrent.Callable;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.UncheckedExecutionException;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;
import org.eclipse.buildship.core.internal.util.gradle.IdeFriendlyClassLoading;

/**
 * Default implementation of {@link ModelProvider}.
 *
 * @author Stefan Oehme
 */
public final class DefaultModelProvider implements ModelProvider {

    private final InternalGradleBuild gradleBuild;
    private final Cache<Object, Object> cache = CacheBuilder.newBuilder().build();

    public DefaultModelProvider(InternalGradleBuild gradleBuild) {
        this.gradleBuild = gradleBuild;
    }

    @Override
    public <T> T fetchModel(final Class<T> model, FetchStrategy strategy, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        return executeOperation(() ->
            DefaultModelProvider.this.gradleBuild.withConnection(connection -> {
                return queryModel(model, connection);
            }, tokenSource, monitor),
        strategy, model);
    }

    @Override
    public <T>  Map<String, T> fetchModels(Class<T> model, FetchStrategy strategy, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        return executeOperation(() ->
            DefaultModelProvider.this.gradleBuild.withConnection(connection -> {
                BuildEnvironment buildEnvironment = connection.getModel(BuildEnvironment.class);
                GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
                if (gradleVersion.supportsCompositeBuilds()) {
                    return queryCompositeModel(model, connection);
                } else {
                    return ImmutableMap.of(":", queryModel(model, connection));
                }
            }, tokenSource, monitor),
        strategy, model);
    }

    @Override
    public  Map<String, EclipseProject> fetchEclipseProjectAndRunSyncTasks(final CancellationTokenSource tokenSource, final IProgressMonitor monitor) {
        return executeOperation(() ->
            // TODO (donat) Right now, project configurators can only get cached model query results if they invoke the same exact actions
            // used below. We should fix this by letting configurators declare their required models.
            DefaultModelProvider.this.gradleBuild.withConnection(connection -> ExtendedEclipseModelUtils.collectEclipseModels(ExtendedEclipseModelUtils.runTasksAndQueryModels(connection)), tokenSource, monitor),
        FetchStrategy.FORCE_RELOAD, EclipseProject.class);
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

    private static <T> Map<String, T> queryCompositeModel(Class<T> model, ProjectConnection connection) {
        BuildAction<Map<String, T>> query = IdeFriendlyClassLoading.loadCompositeModelQuery(model);
        return connection.action(query).run();
    }

    private static <T> T queryModel(Class<T> model, ProjectConnection connection) {
        return connection.getModel(model);
    }

}
