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
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
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
import org.eclipse.core.runtime.QualifiedName;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.util.object.MoreObjects;

/**
 * Provides resource filtering on {@link IProject} instances.
 */
final class ResourceFilter {

    // to create filters we reuse the constants and classes from the org.eclipse.core.resources
    // plug-in. The IDs are not exposed as a class, consequently we redefine them here.
    // Documentation:
    // http://help.eclipse.org/luna/topic/org.eclipse.platform.doc.isv/guide/resInt_filters.htm

    // resource filter id
    private static final String FILTER_ID = "org.eclipse.ui.ide.multiFilter"; //$NON-NLS-1$

    private static final QualifiedName RESOURCE_PROPERTY_GRADLE_FILTER = new QualifiedName(CorePlugin.PLUGIN_ID, "GRADLE_FILTER");

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
        List<FileInfoMatcherDescription> matchers = createMatchers(project, childLocations);
        setExclusionFilters(project, matchers, monitor);
    }

    /**
     * Removes all resource filters created with the {@link #attachFilters(IProject, List, IProgressMonitor)} method.
     *
     * @param project the target project to remove the filters from
     * @param monitor the monitor to report progress on
     */
    public static void detachAllFilters(IProject project, IProgressMonitor monitor) {
        monitor = MoreObjects.firstNonNull(monitor, new NullProgressMonitor());
        monitor.beginTask("Detach all Gradle filters", IProgressMonitor.UNKNOWN);
        try {
            StringSetPersistentProperty knownMatcherNames = StringSetPersistentProperty.from(RESOURCE_PROPERTY_GRADLE_FILTER, project);
            Set<String> matcherNames = knownMatcherNames.get();
            for (IResourceFilterDescription filter : project.getFilters()) {
                FileInfoMatcherDescription matcher = filter.getFileInfoMatcherDescription();
                if (matcher != null && matcher.getArguments() != null && matcherNames.contains(matcher.getArguments())) {
                    knownMatcherNames.remove((String) matcher.getArguments());
                    filter.delete(IResource.BACKGROUND_REFRESH, monitor);
                }
            }
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        } finally {
            monitor.done();
        }
    }

    private static List<FileInfoMatcherDescription> createMatchers(IProject project, List<File> children) {
        ImmutableList.Builder<FileInfoMatcherDescription> matchers = ImmutableList.builder();
        IPath projectLocation = project.getLocation();
        for (File child : children) {
            IPath childLocation = new Path(child.getAbsolutePath());
            if (projectLocation.isPrefixOf(childLocation)) {
                matchers.add(new FileInfoMatcherDescription(FILTER_ID, createMultiFilterArgument(childLocation.makeRelativeTo(projectLocation).toPortableString())));
            }
        }
        return matchers.build();
    }

    private static String createMultiFilterArgument(String relativeLocation) {
        return "1.0-projectRelativePath-matches-false-false-" + relativeLocation; //$NON-NLS-1$
    }

    private static void setExclusionFilters(IProject project, List<FileInfoMatcherDescription> matchers, IProgressMonitor monitor) {
        monitor.beginTask(String.format("Set resource filters for project %s", project), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
        try {
            // retrieve already defined filters
            final IResourceFilterDescription[] existingFilters;
            try {
                existingFilters = project.getFilters();
            } catch (CoreException e) {
                String message = String.format("Cannot retrieve current resource filters for project %s.", project.getName()); //$NON-NLS-1$
                throw new GradlePluginsRuntimeException(message, e);
            }

            // filter the matchers by removing all matchers for which there is already a filter defined on the project
            ImmutableList<FileInfoMatcherDescription> newMatchers = FluentIterable.from(matchers).filter(new Predicate<FileInfoMatcherDescription>() {

                @Override
                public boolean apply(FileInfoMatcherDescription matcher) {
                    for (IResourceFilterDescription existingFilter : existingFilters) {
                        if (existingFilter.getFileInfoMatcherDescription().equals(matcher)) {
                            return false;
                        }
                    }
                    return true;
                }
            }).toList();

            // create new filters for the new matchers and assign them to the project
            if (!matchers.isEmpty()) {
                try {
                    int type = IResourceFilterDescription.EXCLUDE_ALL | IResourceFilterDescription.FOLDERS | IResourceFilterDescription.INHERITABLE;
                    for (FileInfoMatcherDescription matcher : newMatchers) {
                        project.createFilter(type, matcher, IResource.BACKGROUND_REFRESH, new NullProgressMonitor());
                        StringSetPersistentProperty.from(RESOURCE_PROPERTY_GRADLE_FILTER, project).add((String)matcher.getArguments());
                    }
                } catch (CoreException e) {
                    String message = String.format("Cannot create new resource filters for project %s.", project); //$NON-NLS-1$
                    throw new GradlePluginsRuntimeException(message, e);
                }
            }
        } finally {
            monitor.done();
        }
    }

}
