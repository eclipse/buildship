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

package org.eclipse.buildship.ui.navigator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.navigator.CommonViewer;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.ui.UiPluginConstants;

/**
 * Common Navigator Framework filter hiding all which contain nested projects.
 * <p/>
 * If the nested project is located the parent project's folder, we show only one (project) node in
 * the tree.
 */
public final class HideFolderWhenProjectIsShownAsNested extends ViewerFilter {

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        // only CommonViewer is supposed to be passed to this method
        // if not the case then log the event and let the elements pass through the filter
        if (!(viewer instanceof CommonViewer)) {
            CorePlugin.logger().warn(this.getClass().getName() + " should take only CommonViewer instances but " + viewer + " was received");
            return true;
        }

        // if enabled (in the Project Explorer's context menu) filter all folders containing a
        // workspace project with Gradle nature
        CommonViewer commonViewer = (CommonViewer) viewer;
        if (commonViewer.getNavigatorContentService().getActivationService().isNavigatorExtensionActive(UiPluginConstants.NAVIGATOR_NESTED_PROJECTS_CONTENT_PROVIDER_ID)) {
            if (element instanceof IFolder) {
                return workspaceGradleProjectExistsInFolder((IFolder) element);
            }
        }

        // by default let the elements pass the filter
        return true;
    }

    private static boolean workspaceGradleProjectExistsInFolder(IFolder folder) {
        IPath folderLocation = null;
        try {
            folderLocation = folder.getLocation();
            for (IProject project : CorePlugin.workspaceOperations().getAllProjects()) {
                if (project.isOpen() && GradleProjectNature.INSTANCE.isPresentOn(project) && project.getLocation().equals(folderLocation)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            CorePlugin.logger().error("Failed to determine whether project there is a Gradle project in " + folderLocation, e);
            return false;
        }
    }
}
