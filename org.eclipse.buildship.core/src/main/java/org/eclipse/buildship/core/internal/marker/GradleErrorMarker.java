/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.marker;

import java.nio.charset.StandardCharsets;

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

            marker.setAttribute(IMarker.MESSAGE, trimMarkerProperty(message));
            marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
            marker.setAttribute(IMarker.SEVERITY, severity);
            marker.setAttribute(ATTRIBUTE_ROOT_DIR, gradleBuild.getBuildConfig().getRootProjectDirectory().getAbsolutePath());
            if (exception != null) {
                String stackTrace = Throwables.getStackTraceAsString(exception);
                marker.setAttribute(GradleErrorMarker.ATTRIBUTE_STACKTRACE, trimMarkerProperty(stackTrace));
            }
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot create Gradle error marker", e);
        }
    }

    /*
     * The Eclipse platform will throw an exception if a marker property is longer than 65535 bytes
     * https://github.com/eclipse-platform/eclipse.platform/blob/97d555a8b563dcb3a32bd43ad58ba452fa027a73/resources/bundles/org.eclipse.core.resources/src/org/eclipse/core/internal/resources/MarkerInfo.java#L56-L60
     */
    private static String trimMarkerProperty(String property) {
        // avoid calling String.toBytes() for shorter Strings
        // UTF-8 can theoretically use up to 4 bytes to represent a code point
        if (property.length() < 16384) {
            return property;
        }
        // check precise length
        byte[] bytes = property.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= 65535) {
            return property;
        }
        // trim to size if needed
        return new String(bytes, 0, 65535, StandardCharsets.UTF_8);
    }
}
