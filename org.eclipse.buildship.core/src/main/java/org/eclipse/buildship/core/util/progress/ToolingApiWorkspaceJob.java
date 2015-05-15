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

import org.gradle.tooling.BuildCancelledException;
import org.gradle.tooling.BuildException;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.util.string.StringUtils;

/**
 * Base class for cancellable workspace jobs that invoke the Gradle Tooling API.
 */
public abstract class ToolingApiWorkspaceJob extends WorkspaceJob {

    // if the job returns a normal error status then eclipse shows the default error dialog which is
    // too basic. on the other hand the OK_STATUS is not feasible since clients of the job might
    // need to determine if the job was finished successfully. to solve both problems we use this
    // custom, non-ok status
    private static final IStatus SILENT_ERROR_STATUS = new Status(IStatus.CANCEL, CorePlugin.PLUGIN_ID, "");

    private final CancellationTokenSource tokenSource;

    /**
     * Creates a new job with the specified name. The job name is a human-readable value that is
     * displayed to users. The name does not need to be unique, but it must not be {@code null}. A
     * token for Gradle build cancellation is created.
     *
     * @param name the name of the job
     */
    protected ToolingApiWorkspaceJob(String name) {
        super(name);
        this.tokenSource = GradleConnector.newCancellationTokenSource();
    }

    protected CancellationToken getToken() {
        return this.tokenSource.token();
    }

    /**
     * Method where the Tooling API-related work should be done.
     * <p/>
     * If an exception is thrown in this method it will be automatically reported via the
     * {@link org.eclipse.buildship.core.notification.UserNotification} service. The notification
     * will be selective based on the type of the thrown exception.
     * <p/>
     * If no exception was thrown in the method the job's return status will be always
     * {@link Status#OK_STATUS}. If an exception occurs an arbitrary non-ok and non-error status
     * will be returned. This disables the platform UI to show built-in (and rather basic) exception
     * dialog.
     *
     * @param monitor the monitor to report the progress
     * @throws Exception when any exception happens in the logic
     */
    public abstract void runToolingApiJobInWorkspace(IProgressMonitor monitor) throws Exception;

    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
        try {
            runToolingApiJobInWorkspace(monitor);
            return Status.OK_STATUS;
        } catch (BuildCancelledException e) {
            return handleBuildCancelled(e);
        } catch (BuildException e) {
            return handleBuildFailed(e);
        } catch (GradleConnectionException e) {
            return handleGradleConnectionFailed(e);
        } catch (GradlePluginsRuntimeException e) {
            return handlePluginFailed(e);
        } catch (Throwable t) {
            return handleUnknownFailed(t);
        } finally {
            monitor.done();
        }
    }

    private IStatus handleBuildCancelled(BuildCancelledException e) {
        // if the job was cancelled by the user, just log the event
        CorePlugin.logger().info("Gradle project import cancelled", e);
        return Status.CANCEL_STATUS;
    }

    private IStatus handleBuildFailed(BuildException e) {
        // if there is an error in the project's build script, notify the user, but don't
        // put it in the error log (log as a warning instead)
        String message = "Gradle project import failed due to an error in the referenced Gradle build.";
        CorePlugin.logger().warn(message, e);
        CorePlugin.userNotification().errorOccurred("Project import failed", message, collectErrorMessages(e), IStatus.WARNING, e);
        // the problem is already logged, the job doesn't have to record it again
        return SILENT_ERROR_STATUS;
    }

    private IStatus handleGradleConnectionFailed(GradleConnectionException e) {
        // if there is an error connecting to Gradle, notify the user, but don't
        // put it in the error log (log as a warning instead)
        String message = "Gradle project import failed due to an error connecting to the Gradle build.";
        CorePlugin.logger().warn(message, e);
        CorePlugin.userNotification().errorOccurred("Project import failed", message, collectErrorMessages(e), IStatus.WARNING, e);
        // the problem is already logged, the job doesn't have to record it again
        return SILENT_ERROR_STATUS;
    }

    private IStatus handlePluginFailed(GradlePluginsRuntimeException e) {
        // if the exception was thrown by Buildship it should be shown and logged
        String message = "Gradle project import failed due to an error setting up the Eclipse projects.";
        CorePlugin.logger().error(message, e);
        CorePlugin.userNotification().errorOccurred("Project import failed", message, collectErrorMessages(e), IStatus.ERROR, e);
        // the problem is already logged, the job doesn't have to record it again
        return SILENT_ERROR_STATUS;
    }

    private IStatus handleUnknownFailed(Throwable t) {
        // if an unexpected exception was thrown it should be shown and logged
        String message = "Gradle project import failed due to an unexpected error.";
        CorePlugin.logger().error(message, t);
        CorePlugin.userNotification().errorOccurred("Project import failed", message, collectErrorMessages(t), IStatus.ERROR, t);
        // the problem is already logged, the job doesn't have to record it again
        return SILENT_ERROR_STATUS;
    }

    private String collectErrorMessages(Throwable t) {
        // recursively collect the error messages going up the stacktrace
        // avoid the same message showing twice in a row
        List<String> messages = Lists.newArrayList();
        collectCausesRecursively(t.getCause(), messages);
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

    @Override
    protected void canceling() {
        this.tokenSource.cancel();
    }

}
