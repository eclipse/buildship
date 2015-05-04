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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;

/**
 * Common Navigator Framework content provider retrieving Gradle projects with their logical
 * hierarchy.
 */
public final class HierarchicalProjectContentProvider implements ITreeContentProvider {

    private static final Object[] NO_CHILDREN = new Object[0];

    @Override
    public Object[] getChildren(Object parent) {
        Optional<IProject> parentProject = parent instanceof IProject ? Optional.of((IProject) parent) : Optional.<IProject> absent();

        if (parentProject.isPresent() && parentProject.get().isOpen() && GradleProjectNature.INSTANCE.isPresentOn(parentProject.get())) {
            final String parentPath = CorePlugin.projectConfigurationManager().readProjectConfiguration(parentProject.get()).getProjectPath().getPath();
            return FluentIterable.from(CorePlugin.workspaceOperations().getAllProjects()).filter(childrenWorkspaceGradleProjects(parentPath)).toArray(IProject.class);
        } else {
            return NO_CHILDREN;
        }
    }

    @Override
    public Object getParent(Object element) {
        Optional<IProject> project = element instanceof IProject ? Optional.of((IProject) element) : Optional.<IProject> absent();

        if (project.isPresent() && GradleProjectNature.INSTANCE.isPresentOn(project.get())) {
            final String path = CorePlugin.projectConfigurationManager().readProjectConfiguration(project.get()).getProjectPath().getPath();
            return FluentIterable.from(CorePlugin.workspaceOperations().getAllProjects()).filter(parentWorkspaceGradleProject(path)).first().orNull();
        } else {
            return null;
        }
    }

    private static Predicate<IProject> childrenWorkspaceGradleProjects(final String parentPath) {
        return new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                try {
                    if (!GradleProjectNature.INSTANCE.isPresentOn(project)) {
                        return false;
                    } else {
                        String path = CorePlugin.projectConfigurationManager().readProjectConfiguration(project).getProjectPath().getPath();
                        return isDirectChildPath(path);
                    }
                } catch (Exception e) {
                    CorePlugin.logger().error("Unexpected error", e);
                    return false;
                }
            }

            private boolean isDirectChildPath(String path) {
                // a direct child is starts (but not equal) with a parent path and the last segment
                // starting with ':' is at the same position as the the length of the parent path
                return !path.equals(parentPath) && path.startsWith(parentPath) && (path.lastIndexOf(':') + 1) == parentPath.length();
            }
        };
    }

    private static Predicate<IProject> parentWorkspaceGradleProject(final String path) {
        return new Predicate<IProject>() {

            @Override
            public boolean apply(IProject parentProject) {
                try {
                    if (!GradleProjectNature.INSTANCE.isPresentOn(parentProject)) {
                        return false;
                    } else {
                        String parentPath = CorePlugin.projectConfigurationManager().readProjectConfiguration(parentProject).getProjectPath().getPath();
                        return isDirectParentPath(parentPath);
                    }
                } catch (Exception e) {
                    CorePlugin.logger().error("Unexpected error", e);
                    return false;
                }
            }

            private boolean isDirectParentPath(String parentPath) {
                return !path.equals(parentPath) && path.startsWith(parentPath) && (path.lastIndexOf(':') + 1) == parentPath.length();
            }
        };
    }

    @Override
    public boolean hasChildren(Object element) {
        return true;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return null;
    }
}
