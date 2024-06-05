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
import java.util.function.Consumer;

import com.google.common.base.Throwables;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;

/**
 * Describes a Gradle problem marker.
 *
 * @author Donat Csikos
 */
public class GradleErrorMarker {

    public static final String ID = CorePlugin.PLUGIN_ID + ".errormarker";
    public static final String ATTRIBUTE_STACKTRACE = "stacktrace";
    public static final String ATTRIBUTE_ROOT_DIR = "rootdir";
    public static final String ATTRIBUTE_ID = "problem.id";
    public static final String ATTRIBUTE_LABEL = "problem.label";
    public static final String ATTRIBUTE_DETAILS = "problem.details";
    public static final String ATTRIBUTE_SOLUTIONS = "problem.solutions";
    public static final String ATTRIBUTE_DOCUMENTATION_LINK = "problem.documentationlink";

    private GradleErrorMarker() {
    }

    public static boolean belongsToBuild(IMarker marker, InternalGradleBuild build) {
        String rootDir = marker.getAttribute(ATTRIBUTE_ROOT_DIR, null);
        return build.getBuildConfig().getRootProjectDirectory().getAbsolutePath().equals(rootDir);
    }

    public static void createError(IResource resource, InternalGradleBuild gradleBuild, String message, Throwable exception) {
        createMarker(IMarker.SEVERITY_ERROR, resource, gradleBuild, message, exception, 0);
    }

    public static void createError(IResource resource, InternalGradleBuild gradleBuild, String message, Throwable exception, int lineNumber) {
        createMarker(IMarker.SEVERITY_ERROR, resource, gradleBuild, message, exception, lineNumber);
    }

    public static void createWarning(IResource resource, InternalGradleBuild gradleBuild, String message, Throwable exception) {
        createMarker(IMarker.SEVERITY_WARNING, resource, gradleBuild, message, exception, 0);
    }

    private static void createMarker(int severity, IResource resource, InternalGradleBuild gradleBuild, String message, Throwable exception, int lineNumber) {
        String stacktrace = exception == null ? null : trimMarkerProperty(Throwables.getStackTraceAsString(exception));
        createMarker(resource, severity, gradleBuild, message, stacktrace, marker -> {
            if (lineNumber >= 0) {
                try {
                    marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static void createMarker(IResource resource, int severity, InternalGradleBuild gradleBuild, String message, String exception, Consumer<IMarker> customMarkerConfiguration) {
        createMarker(resource, new Consumer<IMarker>() {

            @Override
            public void accept(IMarker marker) {
                try {
                    marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
                    marker.setAttribute(IMarker.SEVERITY, severity);
                    marker.setAttribute(ATTRIBUTE_ROOT_DIR, gradleBuild.getBuildConfig().getRootProjectDirectory().getAbsolutePath());
                    marker.setAttribute(IMarker.MESSAGE, trimMarkerProperty(message));
                    if (exception != null) {
                        marker.setAttribute(GradleErrorMarker.ATTRIBUTE_STACKTRACE, exception);
                    }
                } catch (CoreException e) {
                    CorePlugin.logger().warn("Cannot create Gradle error marker", e);
                    throw new RuntimeException(e);
                }
            }
        }.andThen(customMarkerConfiguration));
    }

    private static void createMarker(IResource resource, Consumer<IMarker> markerConfiguration) {
        try {
            IMarker marker = resource.createMarker(GradleErrorMarker.ID);
            markerConfiguration.accept(marker);
        } catch (Exception e) {
            CorePlugin.logger().warn("Cannot create Gradle error marker", e);
        }
    }

    public static void createProblemMarker(int severity, IResource resource, InternalGradleBuild gradleBuild, String message, String exception, Consumer<IMarker> problemPosition, Consumer<IMarker> problemDetails) {
        createMarker(resource, severity, gradleBuild, message, exception, problemPosition.andThen(problemDetails));
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
