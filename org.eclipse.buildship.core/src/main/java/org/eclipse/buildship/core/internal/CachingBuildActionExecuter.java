/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildActionExecuter;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ResultHandler;
import org.gradle.tooling.events.OperationType;

import com.google.common.cache.Cache;
import com.google.common.collect.Lists;

public class CachingBuildActionExecuter<T> implements BuildActionExecuter<T> {

    private final BuildActionExecuter<T> delegate;
    private final Cache<Object, Object> cache;
    private final CacheKey cacheKey;

    CachingBuildActionExecuter(BuildActionExecuter<T> delegate, BuildAction<T> buildAction, Cache<Object, Object> cache) {
        this.delegate = delegate;
        this.cache = cache;
        this.cacheKey = new CacheKey();
        this.cacheKey.setBuildAction(buildAction);
    }

    @Override
    public CachingBuildActionExecuter<T> withArguments(String... arguments) {
        this.cacheKey.setArguments(Arrays.asList(arguments));
        this.delegate.withArguments(arguments);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> withArguments(Iterable<String> arguments) {
        this.cacheKey.setArguments(arguments == null ? null : Lists.newArrayList(arguments));
        this.delegate.withArguments(arguments);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> addArguments(String... arguments) {
        this.cacheKey.addArguments(Arrays.asList(arguments));
        this.delegate.addArguments(arguments);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> addArguments(Iterable<String> arguments) {
        this.cacheKey.addArguments(Lists.newArrayList(arguments));
        this.delegate.addArguments(arguments);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> setStandardOutput(OutputStream outputStream) {
        this.cacheKey.markInvalid();
        this.delegate.setStandardOutput(outputStream);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> setStandardError(OutputStream outputStream) {
        this.cacheKey.markInvalid();
        this.delegate.setStandardError(outputStream);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> setColorOutput(boolean colorOutput) {
        this.cacheKey.markInvalid();
        this.delegate.setColorOutput(colorOutput);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> setStandardInput(InputStream inputStream) {
        this.cacheKey.markInvalid();
        this.delegate.setStandardInput(inputStream);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> setJavaHome(File javaHome) {
        this.cacheKey.setJavaHome(javaHome);
        this.delegate.setJavaHome(javaHome);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> setJvmArguments(String... jvmArguments) {
        this.cacheKey.setJvmArguments(Arrays.asList(jvmArguments));
        this.delegate.setJvmArguments(jvmArguments);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> setJvmArguments(Iterable<String> jvmArguments) {
        this.cacheKey.setJvmArguments(jvmArguments == null ? null : Lists.newArrayList(jvmArguments));
        this.delegate.setJvmArguments(jvmArguments);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> addJvmArguments(String... jvmArguments) {
        this.cacheKey.addJvmArguments(Arrays.asList(jvmArguments));
        this.delegate.addJvmArguments(jvmArguments);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> addJvmArguments(Iterable<String> jvmArguments) {
        this.cacheKey.addJvmArguments(Lists.newArrayList(jvmArguments));
        this.delegate.addJvmArguments(jvmArguments);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> setEnvironmentVariables(Map<String, String> envVariables) {
        this.cacheKey.setEnvironmentVariables(envVariables);
        this.delegate.setEnvironmentVariables(envVariables);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> addProgressListener(ProgressListener listener) {
        this.cacheKey.markInvalid();
        this.delegate.addProgressListener(listener);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> addProgressListener(org.gradle.tooling.events.ProgressListener listener) {
        this.cacheKey.markInvalid();
        this.delegate.addProgressListener(listener);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> addProgressListener(org.gradle.tooling.events.ProgressListener listener, Set<OperationType> eventTypes) {
        this.cacheKey.markInvalid();
        this.delegate.addProgressListener(listener, eventTypes);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> addProgressListener(org.gradle.tooling.events.ProgressListener listener, OperationType... operationTypes) {
        this.cacheKey.markInvalid();
        this.delegate.addProgressListener(listener, operationTypes);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> withCancellationToken(CancellationToken cancellationToken) {
        this.delegate.withCancellationToken(cancellationToken);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> forTasks(String... tasks) {
        this.cacheKey.setTasks(Arrays.asList(tasks));
        this.delegate.forTasks(tasks);
        return this;
    }

    @Override
    public CachingBuildActionExecuter<T> forTasks(Iterable<String> tasks) {
        this.cacheKey.setTasks(tasks == null ? null : Lists.newArrayList(tasks));
        this.delegate.forTasks(tasks);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T run() throws GradleConnectionException, IllegalStateException {
        Object cachedValue = this.cacheKey.isInvalid() ? null : this.cache.getIfPresent(this.cacheKey);
        if (cachedValue != null) {
            return (T) cachedValue;
        } else {
            T result = this.delegate.run();
            this.cache.put(this.cacheKey, result);
            return result;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(ResultHandler<? super T> handler) throws IllegalStateException {
        Object cachedValue = this.cacheKey.isInvalid() ? null : this.cache.getIfPresent(this.cacheKey);
        if (cachedValue != null) {
            handler.onComplete((T) cachedValue);
        } else {
            InspectableResultHandler<T> inspectableResultHandler = new InspectableResultHandler<>();
            this.delegate.run(inspectableResultHandler);
            inspectableResultHandler.getResult().ifPresent(r -> this.cache.put(this.cacheKey, r));
            inspectableResultHandler.forwardResults(handler);
        }
    }
}
