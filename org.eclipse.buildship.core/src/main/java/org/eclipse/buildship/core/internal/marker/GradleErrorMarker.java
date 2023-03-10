/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.marker;

import com.google.common.base.Throwables;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;

/**
 * Describes Gradle error marker.
 *
 * @author Donat Csikos
 */
public class GradleErrorMarker {

    public static String ID = CorePlugin.PLUGIN_ID + ".errormarker";
    public static String ATTRIBUTE_STACKTRACE = "stacktrace";
    public static String ATTRIBUTE_ROOT_DIR = "rootdir";

    private GradleErrorMarker() {
    }

    public static boolean belongsToBuild(IMarker marker, InternalGradleBuild build) {
        String rootDir = marker.getAttribute(ATTRIBUTE_ROOT_DIR, null);
        return build.getBuildConfig().getRootProjectDirectory().getAbsolutePath().equals(rootDir);
    }

    public static void createError(IResource resource, InternalGradleBuild gradleBuild, String message, Throwable exception, int lineNumber) {
        createMarker(IMarker.SEVERITY_ERROR, resource, gradleBuild, message, exception,lineNumber);
    }

    public static void createWarning(IResource resource, InternalGradleBuild gradleBuild, String message, Throwable exception, int lineNumber) {
        createMarker(IMarker.SEVERITY_WARNING, resource, gradleBuild, message, exception,lineNumber);
    }

    private static void createMarker(int severity, IResource resource, InternalGradleBuild gradleBuild, String message, Throwable exception, int lineNumber) {
        try {
            IMarker marker = resource.createMarker(GradleErrorMarker.ID);

            if (lineNumber >= 0) {
                marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
            }

            marker.setAttribute(IMarker.MESSAGE, message);
            marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
            marker.setAttribute(IMarker.SEVERITY, severity);
            marker.setAttribute(ATTRIBUTE_ROOT_DIR, gradleBuild.getBuildConfig().getRootProjectDirectory().getAbsolutePath());
            if (exception != null) {
                String stackTrace = Throwables.getStackTraceAsString(exception);
                marker.setAttribute(GradleErrorMarker.ATTRIBUTE_STACKTRACE, stackTrace);
            }
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot create Gradle error marker", e);
        }
    }
}
