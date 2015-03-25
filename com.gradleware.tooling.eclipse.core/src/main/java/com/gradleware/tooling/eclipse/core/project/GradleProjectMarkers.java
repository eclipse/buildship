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

package com.gradleware.tooling.eclipse.core.project;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.gradleware.tooling.eclipse.core.CorePlugin;

/**
 * Marker definitions defined for the Buildship plugin.
 * <p/>
 * All entries are defined as extensions of the <code>org.eclipse.core.resources.markers</code>
 * point in the plugin.xml.
 *
 */
public enum GradleProjectMarkers {

    MISSING_CONFIGURATION_MARKER("missingconfiguration", IMarker.SEVERITY_ERROR);

    private final String markerId;
    private final int severity;

    private GradleProjectMarkers(String markerId, int severity) {
        // the marker ID has to be in the following format: ${PLUGIN_ID}.${MARKER_ID}
        this.markerId = CorePlugin.PLUGIN_ID + "." + markerId;
        this.severity = severity;
    }

    public void addMarkerToResource(IResource target, String message) throws CoreException {
        IMarker marker = target.createMarker(this.markerId);
        marker.setAttribute(IMarker.MESSAGE, message);
        marker.setAttribute(IMarker.SEVERITY, this.severity);
    }

    public void removeMarkerFromResource(IResource resource) throws CoreException {
        resource.deleteMarkers(this.markerId, false, IResource.DEPTH_ZERO);
    }

    public void removeMarkerFromResourceRecursively(IResource resource) throws CoreException {
        resource.deleteMarkers(this.markerId, false, IResource.DEPTH_INFINITE);
    }

}
