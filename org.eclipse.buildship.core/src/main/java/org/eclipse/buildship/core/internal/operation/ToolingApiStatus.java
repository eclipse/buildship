/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.operation;

import org.gradle.tooling.BuildCancelledException;
import org.gradle.tooling.BuildException;
import org.gradle.tooling.GradleConnectionException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.UnsupportedConfigurationException;
import org.eclipse.buildship.core.internal.workspace.impl.ImportRootProjectOperation.ImportRootProjectException;

/**
 * Custom {@link IStatus} implementation to represent Gradle-related statuses.
 *
 * @author Donat Csikos
 */
public final class ToolingApiStatus extends Status implements IStatus {

    /**
     * The possible problem types that a {@link ToolingApiStatus} can represent.
     */
    public static enum ToolingApiStatusType {

        BUILD_CANCELLED(IStatus.CANCEL, "%s"),
        IMPORT_ROOT_DIR_FAILED(IStatus.WARNING, "%s failed due to an error while importing the root project."),
        BUILD_FAILED(IStatus.WARNING, "%s failed due to an error in the referenced Gradle build."),
        CONNECTION_FAILED(IStatus.WARNING, "%s failed due to an error connecting to the Gradle build."),
        UNSUPPORTED_CONFIGURATION(IStatus.WARNING, "%s failed due to an unsupported configuration in the referenced Gradle build."),
        PLUGIN_FAILED(IStatus.ERROR, "%s failed due to an error configuring Eclipse."),
        UNKNOWN(IStatus.ERROR, "%s failed due to an unexpected error.");

        private final int severity;
        private final String messageTemplate;

        private ToolingApiStatusType(int severity, String messageTemplate) {
            this.severity = severity;
            this.messageTemplate = messageTemplate;
        }

        public int getSeverity() {
            return this.severity;
        }

        public int getCode() {
            return this.ordinal();
        }

        public boolean matches(ToolingApiStatus status) {
            return getCode() == status.getCode();
        }

        String messageTemplate() {
            return this.messageTemplate;
        }
    }

    private ToolingApiStatus(ToolingApiStatusType type, String workName, Throwable exception) {
        super(type.getSeverity(), CorePlugin.PLUGIN_ID, type.getCode(), String.format(type.messageTemplate(), workName), exception);
    }

    public static ToolingApiStatus from(String workName, Throwable failure) {
        if (failure instanceof OperationCanceledException) {
            return new ToolingApiStatus(ToolingApiStatusType.BUILD_CANCELLED, workName, null);
        } else if (failure instanceof BuildCancelledException) {
            return new ToolingApiStatus(ToolingApiStatusType.BUILD_CANCELLED, workName, null);
        } else if (failure instanceof BuildException) {
            return new ToolingApiStatus(ToolingApiStatusType.BUILD_FAILED, workName, (BuildException) failure);
        } else if (failure instanceof GradleConnectionException) {
            return new ToolingApiStatus(ToolingApiStatusType.CONNECTION_FAILED, workName, (GradleConnectionException) failure);
        } else if (failure instanceof ImportRootProjectException) {
            return new ToolingApiStatus(ToolingApiStatusType.IMPORT_ROOT_DIR_FAILED, workName, (ImportRootProjectException) failure);
        } else if (failure instanceof UnsupportedConfigurationException) {
            return new ToolingApiStatus(ToolingApiStatusType.UNSUPPORTED_CONFIGURATION, workName, (UnsupportedConfigurationException) failure);
        } else if (failure instanceof GradlePluginsRuntimeException) {
            return new ToolingApiStatus(ToolingApiStatusType.PLUGIN_FAILED, workName, (GradlePluginsRuntimeException) failure);
        } else {
            return new ToolingApiStatus(ToolingApiStatusType.UNKNOWN, workName, failure);
        }
    }

    public void log() {
        CorePlugin.getInstance().getLog().log(this);
    }

    public boolean severityMatches(int severity) {
        return (getSeverity() & severity) != 0;
    }
}
