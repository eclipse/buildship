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
 * Custom unchecked exception for indicating that the configuration provided by Gradle
 * can't be properly synchronized with Eclipse.
 */
public final class UnsupportedConfigurationException extends GradlePluginsRuntimeException {

    private static final long serialVersionUID = 1L;

    public UnsupportedConfigurationException(String message) {
        super(message);
    }

    public UnsupportedConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
