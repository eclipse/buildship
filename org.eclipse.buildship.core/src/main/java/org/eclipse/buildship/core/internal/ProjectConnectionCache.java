/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import java.util.Optional;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class ProjectConnectionCache {

    private final Cache<Object, Object> cache = CacheBuilder.newBuilder().build();

    public void clear() {
        this.cache.invalidateAll();
    }

    public Optional<Object> get(Object key) {
        return Optional.ofNullable(this.cache.getIfPresent(key));
    }

    public void put(Object key, Object value) {
        this.cache.put(key, value);
    }
}
