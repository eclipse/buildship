/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

/**
 * Common methods for {@link InitializationContext} and {@link ProjectContext}.
 *
 * @author Donat Csikos
 * @since 3.0
 */
public interface SynchronizationContext {

    /**
     *
     * @param id
     * @return
     */
    Object getQueryResult(String id);

    /**
     * Registers a error during synchronization. Instead of implementing separate error handling,
     * project configurators should use this method to report issues.
     *
     * @param message the error message describing the problem
     * @param exception The exception to report; can be {@code null}
     */
    void error(String message, Exception exception);

    /**
     * Registers a warning during synchronization. Instead of implementing separate error handling,
     * project configurators should use this method to report issues.
     *
     * @param message the warning message describing the problem
     * @param exception The exception to report; can be {@code null}
     */
    void warning(String message, Exception exception);

}