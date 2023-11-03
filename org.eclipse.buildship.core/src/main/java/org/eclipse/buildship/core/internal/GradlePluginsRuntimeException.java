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

/**
 * Base class for all custom unchecked exception types thrown by the Gradle integration plugins.
 */
public class GradlePluginsRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.
     *
     * @param message the detail message
     */
    public GradlePluginsRuntimeException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public GradlePluginsRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     *
     * @param cause the cause
     */
    public GradlePluginsRuntimeException(Throwable cause) {
        super(cause);
    }

}
