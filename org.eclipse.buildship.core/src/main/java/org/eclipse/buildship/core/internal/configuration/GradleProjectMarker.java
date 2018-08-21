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
