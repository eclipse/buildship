/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.navigator;

import java.util.Collection;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.preferences.PersistentModel;
import org.eclipse.buildship.ui.internal.UiPlugin;
import org.eclipse.buildship.ui.internal.UiTraceScopes;

/**
 * Allows users to show or hide the sub projects in the Navigator, Project and Package Explorer.
 *
 * @author Stefan Oehme
 */
public final class SubProjectViewerFilter extends ViewerFilter {

    @SuppressWarnings({"cast", "RedundantCast"})
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        IResource resource = (IResource) Platform.getAdapterManager().getAdapter(element, IResource.class);
        return resource == null || !isSubProject(resource);
    }

    private boolean isSubProject(IResource resource) {
        if (resource instanceof IFolder) {
            IFolder folder = (IFolder) resource;
            return isSubProjectFolder(folder);
        } else {
            return false;
        }
    }

    private boolean isSubProjectFolder(IFolder folder) {
        try {
            PersistentModel model = CorePlugin.modelPersistence().loadModel(folder.getProject());
            if (model.isPresent()) {
                Collection<IPath> paths = model.getSubprojectPaths();
                return paths != null && paths.contains(folder.getProjectRelativePath());
            } else {
                return false;
            }
        } catch (Exception e) {
            UiPlugin.logger().trace(UiTraceScopes.NAVIGATOR, String.format("Could not check whether folder %s is a sub project.", folder.getFullPath()), e);
            return false;
        }
    }
}
