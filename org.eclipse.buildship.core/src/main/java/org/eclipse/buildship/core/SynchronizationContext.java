/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core;

/**
 * Common methods for {@link InitializationContext} and {@link ProjectContext}.
 *
 * @author Donat Csikos
 * @since 3.0
 */
public interface SynchronizationContext {

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
