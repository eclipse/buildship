/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

/**
 * Enumerates different strategies of how to fetch a given value from a caching data provider.
 *
 * @author Etienne Studer
 */
public enum FetchStrategy {

    /**
     * Looks up the requested value in the cache only. If the value is not present, the value is not loaded from the underlying system.
     */
    FROM_CACHE_ONLY,

    /**
     * Looks up the requested value in the cache and, iff the value is not present in the cache, loads the value from the underlying system.
     */
    LOAD_IF_NOT_CACHED,

    /**
     * Loads the value from the underlying system, regardless of whether the value is currently in the cache or not.
     */
    FORCE_RELOAD

}
