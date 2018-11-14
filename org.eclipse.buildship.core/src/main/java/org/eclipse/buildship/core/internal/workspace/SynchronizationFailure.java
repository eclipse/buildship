/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import org.eclipse.core.resources.IResource;

public final class SynchronizationFailure {

    private final String pluginId;
    private final IResource resource;
    private final String message;
    private final Exception exception;

    private SynchronizationFailure(String pluginId, IResource resource, String message, Exception exception) {
        this.pluginId = pluginId;
        this.resource = resource;
        this.message = message;
        this.exception = exception;
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

    public static final SynchronizationFailure from(String pluginId, IResource resource, String message, Exception exception) {
        return new SynchronizationFailure(pluginId, resource, message, exception);
    }
}
