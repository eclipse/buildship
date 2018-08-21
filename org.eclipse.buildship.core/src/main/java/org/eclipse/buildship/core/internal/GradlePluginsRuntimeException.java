/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

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
