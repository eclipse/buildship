/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.IntermediateResultHandler;

import com.google.common.cache.Cache;

class CachingIntermediateResultHandler<T> implements IntermediateResultHandler<T> {

    private BuildAction<T> buildAction;
    private IntermediateResultHandler<? super T> delegate;
    private Cache<Object, Object> cache;

    public CachingIntermediateResultHandler(BuildAction<T> buildAction, IntermediateResultHandler<? super T> handler, Cache<Object, Object> cache) {
        this.buildAction = buildAction;
        this.delegate = handler;
        this.cache = cache;
    }

    @Override
    public void onComplete(T result) {
        if (result != null) {
            this.cache.put(CacheKey.builder().setBuildAction(this.buildAction).build(), result);
        }
        this.delegate.onComplete(result);
    }
}
