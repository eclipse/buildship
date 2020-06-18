/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;

public final class SynchronizationProblem {

    private final String pluginId;
    private final IResource resource;
    private final String message;
    private final Exception exception;
    private final int severity;
    private final int line;

    private SynchronizationProblem(String pluginId, IResource resource, String message, Exception exception, int severity, int line) {
        this.pluginId = pluginId;
        this.resource = resource;
        this.message = message;
        this.exception = exception;
        this.severity = severity;
        this.line = line;
    }

    public String getPluginId() {
        return this.pluginId;
    }

    public IResource getResource() {
        return this.resource;
    }

    public String getMessage() {
        return this.message;
    }

    public Exception getException() {
        return this.exception;
    }

    public int getSeverity() {
        return this.severity;
    }

    public int getLine() {
        return this.line;
    }

    public static final SynchronizationProblem newError(String pluginId, IResource resource, String message, Exception exception) {
        return new SynchronizationProblem(pluginId, resource, message, exception, IStatus.ERROR, 0);
    }

    public static final SynchronizationProblem newError(String pluginId, IResource resource, String message, Exception exception, int line) {
        return new SynchronizationProblem(pluginId, resource, message, exception, IStatus.ERROR, line);
    }


    public static final SynchronizationProblem newWarning(String pluginId, IResource resource, String message, Exception exception) {
        return new SynchronizationProblem(pluginId, resource, message, exception, IStatus.WARNING, 0);
    }

    public static final SynchronizationProblem newWarning(String pluginId, IResource resource, String message, Exception exception, int line) {
        return new SynchronizationProblem(pluginId, resource, message, exception, IStatus.WARNING, line);
    }
}
