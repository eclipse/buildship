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

package org.eclipse.buildship.core.workspace.internal;

import java.io.File;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;

/**
 * Provides resource filtering on {@link IProject} instances.
 */
final class ResourceFilter {

    // to create filters we reuse the constants and classes from the org.eclipse.core.resources
    // plug-in. The IDs are not exposed as a class, consequently we redefine them in this class.
    // documentation:
    // http://help.eclipse.org/luna/topic/org.eclipse.platform.doc.isv/guide/resInt_filters.htm

    // resource filter matcher id
    private static final String MATCHER_ID = "org.eclipse.core.resources.regexFilterMatcher";

    // id to merge the resource filters as a single OR statement
    private static final String OR_ID = "org.eclipse.ui.ide.orFilterMatcher";

    private ResourceFilter() {
    }

    /**
     * Attaches resource filters on the specified project to hide any of the given child locations.
     *
     * @param project the project for which to create resource filters
     * @param childLocations the child locations
     * @param monitor the monitor to report progress on
     */
    public static void attachFilters(IProject project, List<File> childLocations, IProgressMonitor monitor) {
        monitor = MoreObjects.firstNonNull(monitor, new NullProgressMonitor());
        List<FileInfoMatcherDescription> filters = createFilters(project, childLocations);
        setFilters(project, filters, monitor);
    }

    private static List<FileInfoMatcherDescription> createFilters(IProject project, List<File> children) {
        ImmutableList.Builder<FileInfoMatcherDescription> filters = ImmutableList.builder();
        IPath projectLocation = project.getLocation();
        for (File child : children) {
            IPath childLocation = new Path(child.getAbsolutePath());
            if (projectLocation.isPrefixOf(childLocation)) {
                filters.add(new FileInfoMatcherDescription(MATCHER_ID, childLocation.makeRelativeTo(projectLocation).toPortableString()));
            }
        }
        return filters.build();
    }

    private static void setFilters(IProject project, List<FileInfoMatcherDescription> filters, IProgressMonitor monitor) {
        monitor.beginTask(String.format("Set resource filters for project %s", project), 2);
        try {
            // get all current filters
            IResourceFilterDescription[] currentFilters;
            try {
                currentFilters = project.getFilters();
            } catch (CoreException e) {
                String message = String.format("Cannot retrieve current resource filters for project %s.", project.getName());
                throw new GradlePluginsRuntimeException(message, e);
            }

            // delete all current filters
            for (IResourceFilterDescription filter : currentFilters) {
                try {
                    filter.delete(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1));
                } catch (CoreException e) {
                    String message = String.format("Cannot delete current resource filter %s.", filter);
                    throw new GradlePluginsRuntimeException(message, e);
                }
            }

            // create the specified filters
            if (!filters.isEmpty()) {
                try {
                    project.createFilter(IResourceFilterDescription.EXCLUDE_ALL | IResourceFilterDescription.FOLDERS | IResourceFilterDescription.INHERITABLE, createCompositeFilter(filters), IResource.BACKGROUND_REFRESH, new SubProgressMonitor(
                            monitor, 1));
                } catch (CoreException e) {
                    String message = String.format("Cannot create new resource filters for project %s.", project);
                    throw new GradlePluginsRuntimeException(message, e);
                }
            }
        } finally {
            monitor.done();
        }
    }

    private static FileInfoMatcherDescription createCompositeFilter(List<FileInfoMatcherDescription> filters) {
        return new FileInfoMatcherDescription(OR_ID, filters.toArray(new FileInfoMatcherDescription[filters.size()]));
    }

}
