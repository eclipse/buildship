/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.marker;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.marker.impl.ErrorMarkerLocation;
import org.eclipse.buildship.core.internal.operation.ToolingApiStatus;
import org.eclipse.buildship.core.internal.workspace.GradleBuild;

/**
 * Main interface to update Gradle error markers on workspace projects.
 *
 * @author Donat Csikos
 */
public class GradleMarkerManager {

    private GradleMarkerManager() {
    }

    /**
     * Clears all Gradle error markers from all projects that belong to the target Gradle build.
     *
     * @param gradleBuild the target build
     */
    public static void clear(GradleBuild gradleBuild) {
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
    public static void addError(GradleBuild gradleBuild, ToolingApiStatus status) {
        try {
            ErrorMarkerLocation errorLocation = ErrorMarkerLocation.findErrorLocation(gradleBuild, status.getException());
            GradleErrorMarker.create(errorLocation.getResource(), gradleBuild, status.getMessage(), status.getException(), errorLocation.getLineNumber());
        } catch (CoreException e) {
            CorePlugin.getInstance().getLog().log(e.getStatus());
        }
    }
}
