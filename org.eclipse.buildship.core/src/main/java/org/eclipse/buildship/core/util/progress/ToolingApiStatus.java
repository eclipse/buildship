/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.util.progress;

import java.util.List;

import org.gradle.tooling.BuildCancelledException;
import org.gradle.tooling.BuildException;
import org.gradle.tooling.GradleConnectionException;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.UnsupportedConfigurationException;
import org.eclipse.buildship.core.util.string.StringUtils;

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

        BUILD_CANCELLED(IStatus.CANCEL),
        BUILD_FAILED(IStatus.WARNING),
        CONNECTION_FAILED(IStatus.WARNING),
        UNSUPPORTED_CONFIGURATION(IStatus.WARNING),
        PLUGIN_FAILED(IStatus.ERROR),
        UNKNOWN(IStatus.ERROR);

        private final int severity;

        private ToolingApiStatusType(int severity) {
            this.severity = severity;
        }

        public int getSeverity() {
            return this.severity;
        }

        public int getCode() {
            return this.ordinal();
        }
    }

    private ToolingApiStatus(ToolingApiStatusType type, String message, Throwable exception) {
        super(type.getSeverity(), CorePlugin.PLUGIN_ID, type.getCode(), message, exception);
    }

    public static ToolingApiStatus from(String workName, Throwable failure) {
        if (failure instanceof OperationCanceledException) {
            return new ToolingApiStatus(ToolingApiStatusType.BUILD_CANCELLED, null, null);
        } else if (failure instanceof BuildCancelledException) {
            return new ToolingApiStatus(ToolingApiStatusType.BUILD_CANCELLED, null, null);
        } else if (failure instanceof BuildException) {
            String message = String.format("%s failed due to an error in the referenced Gradle build.", workName);
            return new ToolingApiStatus(ToolingApiStatusType.BUILD_FAILED, message, (BuildException) failure);
        } else if (failure instanceof GradleConnectionException) {
            String message = String.format("%s failed due to an error connecting to the Gradle build.", workName);
            return new ToolingApiStatus(ToolingApiStatusType.CONNECTION_FAILED, message, (GradleConnectionException) failure);
        } else if (failure instanceof UnsupportedConfigurationException) {
            String message = String.format("%s failed due to an unsupported configuration in the referenced Gradle build.", workName);
            return new ToolingApiStatus(ToolingApiStatusType.UNSUPPORTED_CONFIGURATION, message, (UnsupportedConfigurationException) failure);
        } else if (failure instanceof GradlePluginsRuntimeException) {
            String message = String.format("%s failed due to an error configuring Eclipse.", workName);
            return new ToolingApiStatus(ToolingApiStatusType.PLUGIN_FAILED, message, (GradlePluginsRuntimeException) failure);
        } else {
            String message = String.format("%s failed due to an unexpected error.", workName);
            return new ToolingApiStatus(ToolingApiStatusType.UNKNOWN, message, failure);
        }
    }

    /**
     * Default way of presenting {@link ToolingApiStatus} instances.
     * <p>
     * TODO this method should disappear once we successfully convert all error dialogs
     * displays to markers and log messages.
     *
     * @param workName The name of the task to display in the error dialog
     * @param status the status to present in the dialog
     */
    public static void handleDefault(String workName, IStatus status) {
        CorePlugin.getInstance().getLog().log(status);

        if (status instanceof ToolingApiStatus) {
            if ((status.getSeverity() & (IStatus.WARNING | IStatus.ERROR)) != 0) {
                CorePlugin.userNotification().errorOccurred(
                        String.format("%s failed", workName),
                        status.getMessage(),
                        collectErrorMessages(status.getException()),
                        status.getSeverity(),
                        status.getException());
            }
        }
    }

    private static String collectErrorMessages(Throwable t) {
        if (t == null) {
            return "";
        }

        // recursively collect the error messages going up the stacktrace
        // avoid the same message showing twice in a row
        List<String> messages = Lists.newArrayList();
        Throwable cause = t.getCause();
        if (cause != null) {
            collectCausesRecursively(cause, messages);
        }
        String messageStack = Joiner.on('\n').join(StringUtils.removeAdjacentDuplicates(messages));
        return t.getMessage() + (messageStack.isEmpty() ? "" : "\n\n" + messageStack);
    }

    private static void collectCausesRecursively(Throwable t, List<String> messages) {
        List<String> singleLineMessages = Splitter.on('\n').omitEmptyStrings().splitToList(Strings.nullToEmpty(t.getMessage()));
        messages.addAll(singleLineMessages);
        Throwable cause = t.getCause();
        if (cause != null) {
            collectCausesRecursively(cause, messages);
        }
    }
}
