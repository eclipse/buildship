/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.navigator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.preferences.PersistentModel;

/**
 * Allows users to show or hide the build folder in the Navigator, Project and Package Explorer.
 *
 * @author Stefan Oehme
 */
public final class BuildFolderViewerFilter extends ViewerFilter {

    @SuppressWarnings({"cast", "RedundantCast"})
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        IResource resource = (IResource) Platform.getAdapterManager().getAdapter(element, IResource.class);
        return resource == null || !isBuildFolder(resource);
    }

    private boolean isBuildFolder(IResource resource) {
        if (resource instanceof IFolder) {
            return isBuildFolderInPerstentModel((IFolder) resource);
        } else {
            return false;
        }
    }

    public static boolean isBuildFolderInPerstentModel(IFolder folder) {
        try {
            IProject project = folder.getProject();
            PersistentModel model = CorePlugin.modelPersistence().loadModel(project);
            if (!model.isPresent()) {
                return false;
            } else {
                IPath modelBuildDir = model.getBuildDir();
                return modelBuildDir == null ? false : folder.getProjectRelativePath().equals(modelBuildDir);
            }
        } catch (Exception e) {
            CorePlugin.logger().debug(String.format("Could not check whether folder %s is a build folder.", folder.getFullPath()), e);
            return false;
        }
    }
}
