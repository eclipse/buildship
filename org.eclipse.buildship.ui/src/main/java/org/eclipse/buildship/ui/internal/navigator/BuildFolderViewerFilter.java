/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.navigator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.preferences.PersistentModel;
import org.eclipse.buildship.ui.internal.UiPlugin;
import org.eclipse.buildship.ui.internal.UiTraceScopes;

/**
 * Allows users to show or hide the build folder in the Navigator, Project and Package Explorer.
 *
 * @author Stefan Oehme
 */
public final class BuildFolderViewerFilter extends ViewerFilter {

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof IFolder) {
            return !isBuildFolderInPersistentModel((IFolder) element);
        }
        return true;
    }

    public static boolean isBuildFolderInPersistentModel(IFolder folder) {
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
            UiPlugin.logger().trace(UiTraceScopes.NAVIGATOR, String.format("Could not check whether folder %s is a build folder.", folder.getFullPath()), e);
            return false;
        }
    }
}
