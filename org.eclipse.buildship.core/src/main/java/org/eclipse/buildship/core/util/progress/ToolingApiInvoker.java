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

package org.eclipse.buildship.core.util.progress;

import java.util.List;

import org.gradle.api.JavaVersion;
import org.gradle.tooling.BuildCancelledException;
import org.gradle.tooling.BuildException;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.TestExecutionException;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

import org.eclipse.buildship.core.AggregateException;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.util.string.StringUtils;

/**
 * Invokes the Tooling API and handles any thrown exceptions as specifically as possible.
 */
public final class ToolingApiInvoker {

    private final String workName;
    private final boolean notifyUserAboutBuildFailures;

    public ToolingApiInvoker(String workName, boolean notifyUserAboutBuildFailures) {
        this.workName = Preconditions.checkNotNull(workName);
        this.notifyUserAboutBuildFailures = notifyUserAboutBuildFailures;
    }

    /**
     * Invokes the given command, handles any thrown exceptions as specifically
     * as possible, and finally marks the monitor as done.
     *
     * @param command         the command to invoke
     * @param progressMonitor the progress monitor to mark as done once the operation has finished
     * @return the success status of invoking the command
     */
    public IStatus invoke(ToolingApiCommand command, IProgressMonitor progressMonitor) {
        try {
            command.run();
            return handleSuccess();
        } catch (Throwable t) {
            return handleFailure(t);
        } finally {
            progressMonitor.done();
        }
    }

    private IStatus handleFailure(Throwable failure) {
        if (failure instanceof OperationCanceledException) {
            return handleOperationCanceled((OperationCanceledException) failure);
        } else if (failure instanceof BuildCancelledException) {
            return handleBuildCanceled((BuildCancelledException) failure);
        } else if (failure instanceof BuildException) {
            return handleBuildFailed((BuildException) failure);
        } else if (failure instanceof GradleConnectionException) {
            return handleGradleConnectionFailed((GradleConnectionException) failure);
        } else if (failure instanceof GradlePluginsRuntimeException) {
            return handlePluginFailed((GradlePluginsRuntimeException) failure);
        } else if (failure instanceof AggregateException) {
            return handleMultiException((AggregateException) failure);
        } else {
            return handleUnknownFailed(failure);
        }
    }

    private IStatus handleSuccess() {
        String message = String.format("%s succeeded.", this.workName);
        CorePlugin.logger().debug(message);
        return Status.OK_STATUS;
    }

    private IStatus handleOperationCanceled(OperationCanceledException e) {
        // if an Eclipse job was cancelled by the user, just log the event
        String message = String.format("%s cancelled.", this.workName);
        CorePlugin.logger().debug(message, e);
        return createCancelStatus(message, e);
    }

    private IStatus handleBuildCanceled(BuildCancelledException e) {
        // if a Gradle job was cancelled by the user, just log the event
        String message = String.format("%s cancelled.", this.workName);
        CorePlugin.logger().debug(message, e);
        return createCancelStatus(message, e);
    }

    private IStatus handleBuildFailed(BuildException e) {
        // if there is an error in the project's build script, notify the user, but don't
        // put it in the error log (log as a warning instead)
        String message = String.format("%s failed due to an error in the referenced Gradle build.", this.workName);
        CorePlugin.logger().warn(message, e);
        if (shouldSendUserNotification(e)) {
            CorePlugin.userNotification().errorOccurred(String.format("%s failed", this.workName), message, collectErrorMessages(e), IStatus.WARNING, e);
        }
        return createInfoStatus(message, e);
    }

    private IStatus handleGradleConnectionFailed(GradleConnectionException e) {
        Throwable cause = e.getCause();
        String message;
        if (cause instanceof UnsupportedClassVersionError && !JavaVersion.current().isJava7Compatible()) {
            message = String.format("Project uses Gradle 3.x which requires Java 7 or later to work. Please specify a more recent JVM to run Eclipse.");
        } else {
            message = String.format("%s failed due to an error connecting to the Gradle build.", this.workName);
        }
        // if there is an error connecting to Gradle, notify the user, but don't
        // put it in the error log (log as a warning instead)
        CorePlugin.logger().warn(message, e);
        if (shouldSendUserNotification(e)) {
            CorePlugin.userNotification().errorOccurred(String.format("%s failed", this.workName), message, collectErrorMessages(e), IStatus.WARNING, e);
        }
        return createInfoStatus(message, e);
    }

    private IStatus handlePluginFailed(GradlePluginsRuntimeException e) {
        // if the exception was thrown by Buildship it should be shown and logged
        String message = String.format("%s failed due to an error configuring Eclipse.", this.workName);
        CorePlugin.logger().error(message, e);
        if (shouldSendUserNotification(e)) {
            CorePlugin.userNotification().errorOccurred(String.format("%s failed", this.workName), message, collectErrorMessages(e), IStatus.ERROR, e);
        }
        return createInfoStatus(message, e);
    }

    private IStatus handleUnknownFailed(Throwable t) {
        // if an unexpected exception was thrown it should be shown and logged
        String message = String.format("%s failed due to an unexpected error.", this.workName);
        CorePlugin.logger().error(message, t);
        if (shouldSendUserNotification(t)) {
            CorePlugin.userNotification().errorOccurred(String.format("%s failed", this.workName), message, collectErrorMessages(t), IStatus.ERROR, t);
        }
        return createInfoStatus(message, t);
    }

    private IStatus handleMultiException(AggregateException e) {
        MultiStatus status = new MultiStatus(CorePlugin.PLUGIN_ID, Status.OK, "Multiple problems", null);
        for (Throwable cause : e.getCauses()) {
            IStatus child = handleFailure(cause);
            status.add(child);
        }
        return status;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean shouldSendUserNotification(Throwable t) {
        if (t instanceof BuildCancelledException) {
            return false;
        } else if (t instanceof BuildException || t instanceof TestExecutionException) {
            return this.notifyUserAboutBuildFailures;
        } else {
            return true;
        }
    }

    private String collectErrorMessages(Throwable t) {
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

    private void collectCausesRecursively(Throwable t, List<String> messages) {
        List<String> singleLineMessages = Splitter.on('\n').omitEmptyStrings().splitToList(Strings.nullToEmpty(t.getMessage()));
        messages.addAll(singleLineMessages);
        Throwable cause = t.getCause();
        if (cause != null) {
            collectCausesRecursively(cause, messages);
        }
    }

    private static Status createInfoStatus(String message, Throwable t) {
        return new Status(IStatus.INFO, CorePlugin.PLUGIN_ID, message, t);
    }

    private static Status createCancelStatus(String message, Exception e) {
        return new Status(IStatus.CANCEL, CorePlugin.PLUGIN_ID, message, e);
    }

}
