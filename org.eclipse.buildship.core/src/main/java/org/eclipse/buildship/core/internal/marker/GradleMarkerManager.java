/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.marker;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.operation.ToolingApiStatus;
import org.eclipse.buildship.core.internal.util.string.StringUtils;
import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;

/**
 * Main interface to update Gradle error markers on workspace projects.
 *
 * @author Donat Csikos
 */
public class GradleMarkerManager {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private GradleMarkerManager() {
    }

    /**
     * Clears all Gradle error markers from all projects that belong to the target Gradle build.
     *
     * @param gradleBuild the target build
     */
    public static void clear(InternalGradleBuild gradleBuild) {
        try {
            for (IMarker marker : ResourcesPlugin.getWorkspace().getRoot().findMarkers(GradleErrorMarker.ID, false, IResource.DEPTH_INFINITE)) {
                if (GradleErrorMarker.belongsToBuild(marker, gradleBuild)) {
                    marker.delete();
                }
            }
        } catch (CoreException e) {
            CorePlugin.getInstance().getLog().log(e.getStatus());
        }
    }

    /**
     * Creates a new Gradle error marker displaying the target Tooling API status.
     * <p/>
     * The method will try to place the marker to the most specific resource available:
     * <ul>
     * <li>To a specific line of a build script, in case the status describes a location-specific problem and the file is present in the workspace</li>
     * <li>To the root project build script, if present</li>
     * <li>To the root project, if present</li>
     * <li>To the workspace root if nothing else is present</li>
     * </ul>
     *
     * @param gradleBuild the target Gradle build
     * @param status the status to display in the marker
     */
    public static void addError(InternalGradleBuild gradleBuild, ToolingApiStatus status) {
        ErrorMarkerLocation errorLocation = ErrorMarkerLocation.findErrorLocation(gradleBuild, status.getException());
        GradleErrorMarker.create(errorLocation.getResource(), gradleBuild, collectErrorMessages(status.getException()), status.getException(), errorLocation.getLineNumber());
    }

    private static String collectErrorMessages(Throwable t) {
        // recursively collect the error messages going up the stacktrace
        // avoid the same message showing twice in a row
        List<String> messages = Lists.newArrayList();
        Throwable cause = t.getCause();
        if (cause != null) {
            collectCausesRecursively(cause, messages);
        }
        String messageStack = Joiner.on(LINE_SEPARATOR).join(StringUtils.removeAdjacentDuplicates(messages));
        return t.getMessage() + (messageStack.isEmpty() ? "" : LINE_SEPARATOR + messageStack);
    }

    private static void collectCausesRecursively(Throwable t, List<String> messages) {
        List<String> singleLineMessages = Splitter.on(LINE_SEPARATOR).omitEmptyStrings().splitToList(Strings.nullToEmpty(t.getMessage()));
        messages.addAll(singleLineMessages);
        Throwable cause = t.getCause();
        if (cause != null) {
            collectCausesRecursively(cause, messages);
        }
    }
}
