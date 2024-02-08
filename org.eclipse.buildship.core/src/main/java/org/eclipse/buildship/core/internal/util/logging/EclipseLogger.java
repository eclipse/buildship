/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.logging;

import java.util.Map;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.buildship.core.internal.Logger;
import org.eclipse.buildship.core.internal.TraceScope;

/**
 * Logs to the Eclipse logging infrastructure. Only logs debug information if tracing is enabled.
 *
 * Tracing can be enabled in Eclipse's 'Tracing' tab in the 'Run Configurations...' dialog.
 */
public final class EclipseLogger implements Logger {

    private final ILog log;
    private final String pluginId;
    private final Map<TraceScope, Boolean> tracingEnablement;

    public EclipseLogger(ILog log, String pluginId, Map<TraceScope, Boolean> tracingEnablement) {
        this.log = log;
        this.pluginId = pluginId;
        this.tracingEnablement = tracingEnablement;
    }

    @Override
    public boolean isScopeEnabled(TraceScope scope) {
        return this.tracingEnablement.get(scope);
    }

    @Override
    public void trace(TraceScope scope, String message) {
        if (this.tracingEnablement.get(scope)) {
            this.log.log(new Status(IStatus.INFO, this.pluginId, "[" + scope.getScopeKey() + "] " + message));
        }
    }

    @Override
    public void trace(TraceScope scope, String message, Throwable t) {
        if (this.tracingEnablement.get(scope)) {
            this.log.log(new Status(IStatus.INFO, this.pluginId, message, t));
        }
    }

    @Override
    public void info(String message) {
        this.log.log(new Status(IStatus.INFO, this.pluginId, message));
    }

    @Override
    public void info(String message, Throwable t) {
        this.log.log(new Status(IStatus.INFO, this.pluginId, message, t));
    }

    @Override
    public void warn(String message) {
        this.log.log(new Status(IStatus.WARNING, this.pluginId, message));
    }

    @Override
    public void warn(String message, Throwable t) {
        this.log.log(new Status(IStatus.WARNING, this.pluginId, message, t));
    }

    @Override
    public void error(String message) {
        this.log.log(new Status(IStatus.ERROR, this.pluginId, message));
    }

    @Override
    public void error(String message, Throwable t) {
        this.log.log(new Status(IStatus.ERROR, this.pluginId, message, t));
    }

}
