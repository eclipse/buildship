/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
