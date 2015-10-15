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

package org.eclipse.buildship.core.util.predicate;

import java.util.List;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.workspace.internal.ResourceFilter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 * Supplies some useful {@link Predicate} instances.
 */
public final class Predicates {

    private Predicates() {
    }

    private static final Predicate<IResource> isChildResourcesVisible = new Predicate<IResource>() {

        @Override
        public boolean apply(IResource resource) {
            return isResourceFiltered(resource, ResourceFilter.CHILD_LOCATIONS_FILTER_RESOURCES_QUALIFIEDNAME);
        }
    };

    private static final Predicate<IResource> isBuildFolderVisible = new Predicate<IResource>() {

        @Override
        public boolean apply(IResource resource) {
            return isResourceFiltered(resource, ResourceFilter.FILTER_BUILD_FOLDER_QUALIFIEDNAME);
        }
    };

    private static final Predicate<IResource> isGradleFolderVisible = new Predicate<IResource>() {

        @Override
        public boolean apply(IResource resource) {
            return isResourceFiltered(resource, ResourceFilter.FILTER_GRADLE_FOLDER_QUALIFIEDNAME);
        }
    };

    private static final Predicate<IResource> isDotGradleFolderVisible = new Predicate<IResource>() {

        @Override
        public boolean apply(IResource resource) {
            return isResourceFiltered(resource, ResourceFilter.FILTER_DOT_GRADLE_FOLDER_QUALIFIEDNAME);
        }
    };

    public static Predicate<IResource> isChildResourcesVisible() {
        return isChildResourcesVisible;
    }

    public static Predicate<IResource> isBuildFolderVisible() {
        return isBuildFolderVisible;
    }

    public static Predicate<IResource> isGradleFolderVisible() {
        return isGradleFolderVisible;
    }

    public static Predicate<IResource> isDotGradleFolderVisible() {
        return isDotGradleFolderVisible;
    }

    private static boolean isResourceFiltered(IResource resource, QualifiedName persistentProperty) {
        List<String> filteredLocations = getLocationsToBeFiltered(resource, persistentProperty);
        return !filteredLocations.contains(resource.getLocation().toOSString());
    }

    private static List<String> getLocationsToBeFiltered(IResource resource, QualifiedName persistentProperty) {
        try {
            String resourceLocationsProperty = resource.getProject().getPersistentProperty(persistentProperty);
            if (resourceLocationsProperty != null) {
                return Splitter.on(ResourceFilter.FILTER_ELEMENT_SEPARATOR).trimResults().omitEmptyStrings()
                        .splitToList(resourceLocationsProperty);
            }
        } catch (CoreException e) {
            // just log and return the empty list
            CorePlugin.logger().info(e.getMessage(), e);
        }

        return ImmutableList.of();
    }

    public static Predicate<IProject> accessibleGradleProject() {
        return new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                try {
                    return project.isAccessible() && project.hasNature(GradleProjectNature.ID);
                } catch (CoreException e) {
                    throw new GradlePluginsRuntimeException(e);
                }
            }
        };
    }

}
