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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.buildship.core.Logger;

/**
 * Logs to the Eclipse logging infrastructure. Only logs debug information if tracing is enabled.
 *
 * Tracing can be enabled in Eclipse's 'Tracing' tab in the 'Run Configurations...' dialog.
 */
public final class EclipseLogger implements Logger {

    private final ILog log;
    private final String pluginId;
    private final DebugOptionsManager debugOptionsManager;

    public EclipseLogger(ILog log, String pluginId) {
        this.log = log;
        this.pluginId = pluginId;
        this.debugOptionsManager = new DebugOptionsManager(pluginId);
    }

    @Override
    public boolean isTraceCategoryEnabled(String category) {
        return this.debugOptionsManager.isDebugOptionEnabled(category);
    }

    @Override
    public void trace(String category, String message) {
        if (isTraceCategoryEnabled(category)) {
            this.log.log(new Status(IStatus.INFO, this.pluginId, message));
        }
    }

    @Override
    public void debug(String message) {
        if (this.debugOptionsManager.isDebugging()) {
            this.log.log(new Status(IStatus.INFO, this.pluginId, message));
        }
    }

    @Override
    public void debug(String message, Throwable t) {
        if (this.debugOptionsManager.isDebugging()) {
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


    private static class DebugOptionsManager {
        private final LoadingCache<String, Boolean> debugOptions;

        public DebugOptionsManager(final String pluginId) {
            this.debugOptions = CacheBuilder.newBuilder().build(new CacheLoader<String, Boolean>() {

                @Override
                public Boolean load(String key) throws Exception {
                    return "true".equals(Platform.getDebugOption(pluginId + "/" + key));
                }
            });
        }

        public boolean isDebugging() {
            return this.debugOptions.getUnchecked("debug");
        }

        public boolean isDebugOptionEnabled(String option) {
            return this.debugOptions.getUnchecked("debug/" + option);
        }
    }
}
