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
 *
 * Note that if the job returns a normal error status then Eclipse shows the default error dialog,
 * which is too basic for our needs. On the other hand, the OK_STATUS is not feasible since invokers
 * of the job might need to determine if the job has finished successfully or not. To solve this
 * dilemma, we return an INFO/CANCEL status containing the thrown exception.
 */
public abstract class ToolingApiWorkspaceJob extends WorkspaceJob {

    private final CancellationTokenSource tokenSource;
    private final String workName;

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
        this.workName = name;
    }

    protected CancellationToken getToken() {
        return this.tokenSource.token();
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
        try {
            runToolingApiJobInWorkspace(monitor);
            return handleSuccess();
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

    /**
     * Template method that executes the actual Tooling API-related work.
     * <p/>
     * If an exception is thrown in this method, the exception is reported via the
     * {@link org.eclipse.buildship.core.notification.UserNotification} service. The
     * notification content and its severity depend on the type of the thrown exception.
     * <p/>
     * If no exception is thrown in the template method, the job's return status is
     * {@link Status#OK_STATUS}. If an exception occurs, a non-error, non-ok status
     * is returned. This disables the platform UI to show the built-in (and rather basic)
     * exception dialog.
     *
     * @param monitor the monitor to report the progress
     * @throws Exception thrown when an error happens during the execution
     */
    protected abstract void runToolingApiJobInWorkspace(IProgressMonitor monitor) throws Exception;

    private IStatus handleSuccess() {
        String message = String.format("%s succeeded.", this.workName);
        CorePlugin.logger().info(message);
        return Status.OK_STATUS;
    }

    private IStatus handleBuildCancelled(BuildCancelledException e) {
        // if the job was cancelled by the user, just log the event
        String message = String.format("%s cancelled.", this.workName);
        CorePlugin.logger().info(message, e);
        return createCancelStatus(e);
    }

    private IStatus handleBuildFailed(BuildException e) {
        // if there is an error in the project's build script, notify the user, but don't
        // put it in the error log (log as a warning instead)
        String message = String.format("%s failed due to an error in the referenced Gradle build.", this.workName);
        CorePlugin.logger().warn(message, e);
        CorePlugin.userNotification().errorOccurred(String.format("%s failed", this.workName), message, collectErrorMessages(e), IStatus.WARNING, e);
        return createInfoStatus(e);
    }

    private IStatus handleGradleConnectionFailed(GradleConnectionException e) {
        // if there is an error connecting to Gradle, notify the user, but don't
        // put it in the error log (log as a warning instead)
        String message = String.format("%s failed due to an error connecting to the Gradle build.", this.workName);
        CorePlugin.logger().warn(message, e);
        CorePlugin.userNotification().errorOccurred(String.format("%s failed", this.workName), message, collectErrorMessages(e), IStatus.WARNING, e);
        return createInfoStatus(e);
    }

    private IStatus handlePluginFailed(GradlePluginsRuntimeException e) {
        // if the exception was thrown by Buildship it should be shown and logged
        String message = String.format("%s failed due to an error configuring Eclipse.", this.workName);
        CorePlugin.logger().error(message, e);
        CorePlugin.userNotification().errorOccurred(String.format("%s failed", this.workName), message, collectErrorMessages(e), IStatus.ERROR, e);
        return createInfoStatus(e);
    }

    private IStatus handleUnknownFailed(Throwable t) {
        // if an unexpected exception was thrown it should be shown and logged
        String message = String.format("%s failed due to an unexpected error.", this.workName);
        CorePlugin.logger().error(message, t);
        CorePlugin.userNotification().errorOccurred(String.format("%s failed", this.workName), message, collectErrorMessages(t), IStatus.ERROR, t);
        return createInfoStatus(t);
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
        if (t != null) {
            List<String> singleLineMessages = Splitter.on('\n').omitEmptyStrings().splitToList(Strings.nullToEmpty(t.getMessage()));
            messages.addAll(singleLineMessages);
            Throwable cause = t.getCause();
            if (cause != null) {
                collectCausesRecursively(cause, messages);
            }
        }
    }

    private static Status createInfoStatus(Throwable t) {
        return new Status(IStatus.INFO, CorePlugin.PLUGIN_ID, "", t);
    }

    private static Status createCancelStatus(BuildCancelledException e) {
        return new Status(IStatus.CANCEL, CorePlugin.PLUGIN_ID, "", e);
    }

    @Override
    protected void canceling() {
        this.tokenSource.cancel();
    }

}
