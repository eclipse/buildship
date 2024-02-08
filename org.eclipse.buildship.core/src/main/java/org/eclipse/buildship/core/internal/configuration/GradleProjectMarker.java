/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.buildship.core.internal.CorePlugin;

/**
 * Project configuration error marker.
 * <p/>
 * Defined as an extension point of <code>org.eclipse.core.resources.markers</code> in the <i>plugin.xml</i>.
 */
public enum GradleProjectMarker {

    INSTANCE;

    // the marker ID has to be in the following format: ${PLUGIN_ID}.${MARKER_ID}
    public static final String ID = CorePlugin.PLUGIN_ID + ".gradleprojectconfigurationmarker";

    public void addMarkerToResource(IResource target, String message) throws CoreException {
        IMarker marker = target.createMarker(ID);
        marker.setAttribute(IMarker.MESSAGE, message);
        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
    }

    public void removeMarkerFromResourceRecursively(IResource resource) throws CoreException {
        resource.deleteMarkers(ID, false, IResource.DEPTH_INFINITE);
    }

}
