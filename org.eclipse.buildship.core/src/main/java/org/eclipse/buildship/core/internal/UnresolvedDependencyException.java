/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
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
public final class UnresolvedDependencyException extends GradlePluginsRuntimeException {

    private static final long serialVersionUID = 1L;
    private final String coordinates;

    public UnresolvedDependencyException(String coordinates) {
        super("Unresolved dependency: " + coordinates);
        this.coordinates = coordinates;
    }

    public String getCoordinates() {
        return this.coordinates;
    }
}
