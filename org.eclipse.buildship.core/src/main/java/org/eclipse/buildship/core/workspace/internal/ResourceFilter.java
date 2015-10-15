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

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.util.object.MoreObjects;
import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

/**
 * Provides resource filtering on {@link IProject} instances.
 */
public final class ResourceFilter {

    public static final char FILTER_ELEMENT_SEPARATOR = ',';

    public static final QualifiedName CHILD_LOCATIONS_FILTER_RESOURCES_QUALIFIEDNAME = new QualifiedName(
            CorePlugin.PLUGIN_ID, "filteredChildResources"); //$NON-NLS-1$

    public static final QualifiedName FILTER_BUILD_FOLDER_QUALIFIEDNAME = new QualifiedName(CorePlugin.PLUGIN_ID,
            "filteredBuildfolder"); //$NON-NLS-1$

    public static final QualifiedName FILTER_GRADLE_FOLDER_QUALIFIEDNAME = new QualifiedName(CorePlugin.PLUGIN_ID,
            "filteredGradleFolder"); //$NON-NLS-1$

    public static final QualifiedName FILTER_DOT_GRADLE_FOLDER_QUALIFIEDNAME = new QualifiedName(CorePlugin.PLUGIN_ID,
            "filteredDotGradleFolder"); //$NON-NLS-1$

    // properties key to store/retrieve the filters created by Buildship
    private static final String PROJECT_PROPERTY_KEY_GRADLE_FILTERS = "GRADLE_FILTERS";

    private ResourceFilter() {
    }

    /**
     * Attaches resource filters on the specified project to hide any of the
     * given child locations.
     *
     * @param project
     *            the project for which to create resource filters
     * @param childLocations
     *            the child locations
     * @param monitor
     *            the monitor to report progress on
     * @throws CoreException
     */
    public static void attachFilters(IProject project, List<File> childLocations, IProgressMonitor monitor)
            throws CoreException {
        monitor = MoreObjects.firstNonNull(monitor, new NullProgressMonitor());
        addFilterValuesToProject(project, childLocations, monitor);
    }

    /**
     * Removes all resource filters created with the
     * {@link #attachFilters(IProject, List, IProgressMonitor)} method.
     *
     * @param project
     *            the target project to remove the filters from
     * @param monitor
     *            the monitor to report progress on
     */
    public static void detachAllFilters(IProject project, IProgressMonitor monitor) {
        monitor = MoreObjects.firstNonNull(monitor, new NullProgressMonitor());
        monitor.beginTask(String.format("Remove Gradle resource filters from project %s", project),
                IProgressMonitor.UNKNOWN);
        try {
            StringSetProjectProperty knownMatcherNames = StringSetProjectProperty.from(project,
                    PROJECT_PROPERTY_KEY_GRADLE_FILTERS);
            Set<String> matcherNames = knownMatcherNames.get();
            for (IResourceFilterDescription filter : project.getFilters()) {
                FileInfoMatcherDescription matcher = filter.getFileInfoMatcherDescription();
                if (matcher != null && matcher.getArguments() instanceof String
                        && matcherNames.contains(matcher.getArguments())) {
                    knownMatcherNames.remove((String) matcher.getArguments());
                    filter.delete(IResource.NONE, monitor);
                }
            }
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        } finally {
            monitor.done();
        }
    }

    private static void addFilterValuesToProject(IProject project, List<File> childLocations, IProgressMonitor monitor)
            throws CoreException {
        ImmutableList<String> childLocationStrings = FluentIterable.from(childLocations)
                .transform(new Function<File, String>() {
                    @Override
                    public String apply(File input) {
                        return input.getAbsolutePath();
                    }
                }).toList();

        // store filtered child locations as persistentProperty in the project
        // resource, which can be read by an AbstractGradleViewerFilter for
        // filtering
        String joinedChildLocations = Joiner.on(FILTER_ELEMENT_SEPARATOR).skipNulls().join(childLocationStrings);
        project.setPersistentProperty(CHILD_LOCATIONS_FILTER_RESOURCES_QUALIFIEDNAME, joinedChildLocations);

        // TODO the build and .gradle values are currently hard codes. This
        // should be replaced by values coming from the TAPI.
        project.setPersistentProperty(FILTER_BUILD_FOLDER_QUALIFIEDNAME,
                project.getLocation().append("build").toOSString());
        project.setPersistentProperty(FILTER_GRADLE_FOLDER_QUALIFIEDNAME,
                project.getLocation().append("gradle").toOSString());
        project.setPersistentProperty(FILTER_DOT_GRADLE_FOLDER_QUALIFIEDNAME,
                project.getLocation().append(".gradle").toOSString());
    }

}
