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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.navigator.CommonViewer;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.ui.UiPluginConstants;

/**
 * Common Navigator Framework filter hiding all non-root Gradle projects.
 * <p/>
 * Applying this filter removes the duplicate project representation from the Package Explorer when
 * where a project can be displayed both as workspace project (sibling to the root Gradle project)
 * and under it's parent project node.
 *
 */
public final class HideTopLevelProjectIfNested extends ViewerFilter {

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        // only CommonViewer is supposed to be passed to this method
        // if not the case then log the event and let the elements pass through the filter
        if (!(viewer instanceof CommonViewer)) {
            CorePlugin.logger().warn(this.getClass().getName() + " should take only CommonViewer instances but " + viewer + "was received");
            return true;
        }

        // if enabled remove all projects from the workspace root which have a Gradle nature and is
        // not a root project
        CommonViewer commonViewer = (CommonViewer) viewer;
        if (commonViewer.getNavigatorContentService().getActivationService().isNavigatorExtensionActive(UiPluginConstants.NAVIGATOR_NESTED_PROJECTS_CONTENT_PROVIDER_ID)) {
            if (element instanceof IProject) {
                IProject project = (IProject) element;
                if (project.isOpen() && GradleProjectNature.INSTANCE.isPresentOn(project)) {
                    checkParentElement(parentElement);
                    if (checkParentElement(parentElement)){
                        return isGradleRootProject(project);
                    }
                }
            }
        }

        // by default let the elements pass the filter
        return true;
    }

    private boolean checkParentElement(Object parentElement) {
        Object result = null;
        if (parentElement instanceof TreeNode) {
            result = ((TreeNode) parentElement).getValue();
        } else if (parentElement instanceof TreePath) {
            result = ((TreePath) parentElement).getLastSegment();
        } else {
            result = parentElement;
        }

        if (result instanceof IAdaptable) {
            IAdaptable parentAdaptable = (IAdaptable) result;
            return parentAdaptable.getAdapter(IWorkspaceRoot.class) != null || parentAdaptable.getAdapter(IWorkingSet.class) != null;
        } else {
            return false;
        }
    }

    private static boolean isGradleRootProject(IProject project) {
        try {
            ProjectConfiguration configuration = CorePlugin.projectConfigurationManager().readProjectConfiguration(project);
            return configuration.getProjectPath().getPath().equals(":");
        } catch (Exception e) {
            CorePlugin.logger().error("Failed to determine whether project " + project.getName() + " is a root project", e);
            return false;
        }
    }
}
