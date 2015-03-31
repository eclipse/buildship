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

package org.eclipse.buildship.core.util.logging;

import org.eclipse.buildship.core.Logger;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Logs to the Eclipse logging infrastructure.
 */
public final class EclipseLogger implements Logger {

    private final ILog log;
    private final String pluginId;

    public EclipseLogger(ILog log, String pluginId) {
        this.log = log;
        this.pluginId = pluginId;
    }

    @Override
    public void info(String message) {
        this.log.log(new Status(IStatus.INFO, this.pluginId, message));
    }

    @Override
    public void warn(String message) {
        this.log.log(new Status(IStatus.WARNING, this.pluginId, message));
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
