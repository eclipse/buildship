/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.launch.internal;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Sets;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Contains utility methods to collect Gradle scopes.
 *
 * @author Donat Csikos
 */
public final class GradleScopeUtils {

    private GradleScopeUtils() {
    }

    /**
     * Collects the required dependency scopes for target launch configuration.
     * <p/>
     * If the scopes cannot be obtained (e.g. the target configuration is not supported) or if the
     * current Gradle version doesn't provide scope information then the method returns and empty
     * set. Upon exception the message is logged and an empty set is returned.
     *
     * @param configuration the target launch configuration
     * @return the source set names
     */
    public static Set<String> collectRequiredDependencyScopes(ILaunchConfiguration configuration) {
        Builder<String> result = ImmutableSet.builder();
        try {
            Set<IPackageFragmentRoot> soureFolders = SupportedLaunchConfigType.collectSourceFolders(configuration);
            for (IPackageFragmentRoot sourceFolder : soureFolders) {
                result.addAll(requireDependencyScopesFor(sourceFolder.getRawClasspathEntry()));
            }
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot collect dependency scope information for launch configuration " + configuration.getName(), e);
        }
        return result.build();
    }

    /**
     * Collects the required dependency scopes for target launch configuration.
     * <p/>
     * If the scopes cannot be obtained (e.g. the target configuration is not supported) or if the
     * current Gradle version doesn't provide scope information then the method returns and empty
     * set. Upon exception the message is logged and an empty set is returned.
     *
     * @param configuration the target launch configuration
     * @return the source set names
     */
    public static Set<String> collectDependencyScopes(ILaunchConfiguration configuration) {
        Builder<String> result = ImmutableSet.builder();
        try {
            Set<IPackageFragmentRoot> soureFolders = SupportedLaunchConfigType.collectSourceFolders(configuration);
            for (IPackageFragmentRoot sourceFolder : soureFolders) {
                result.addAll(dependencyScopesFor(sourceFolder.getRawClasspathEntry()));
            }
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot collect dependency scope information for launch configuration " + configuration.getName(), e);
        }
        return result.build();
    }

    /**
     * Returns {@code true} if dependency scope of the entry is part of the supplied set of scopes.
     * <p/>
     * If the entry doesn't define scope information or the set of scopes is empty then this method
     * returns {@code true}.
     *
     * @param scopes the name of the scopes to look for
     * @param entry the target classpath entry
     *
     * @return whether the scopes contain the the entry's dependency scope
     */
    public static boolean isScopesContainEntryDependencyScope(Set<String> scopes, IClasspathEntry entry) {
        if (scopes.isEmpty()) {
            return true;
        }

        Set<String> entryDependencyScopes = dependencyScopesFor(entry);
        if (entryDependencyScopes.isEmpty()) {
            return true;
        }

        return !Sets.intersection(scopes, entryDependencyScopes).isEmpty();
    }

    /**
     * Returns {@code true} if required dependency scopes of the entry has common elements with the
     * supplied set of scopes.
     * <p/>
     * If the entry doesn't define scope information or the set of scopes is empty then this method
     * returns {@code true}.
     *S
     * @param scopes the name of the scopes to look for
     * @param entry the target classpath entry
     *
     * @return if there's common scope
     */
    public static boolean isScopesContainEntryRequiredDependencyScope(Set<String> scopes, IClasspathEntry entry) {
        if (scopes.isEmpty()) {
            return true;
        }

        Set<String> entryRequiredDependencyScopes = requireDependencyScopesFor(entry);
        if (entryRequiredDependencyScopes.isEmpty()) {
            return true;
        }

        return !Sets.intersection(scopes, entryRequiredDependencyScopes).isEmpty();
    }

    private static Set<String> dependencyScopesFor(IClasspathEntry entry) {
        for (IClasspathAttribute attribute : entry.getExtraAttributes()) {
            if (attribute.getName().equals("gradle_scope")) {
                return Sets.newHashSet(attribute.getValue().split(","));
            }
        }
        return Collections.emptySet();
    }

    private static Set<String> requireDependencyScopesFor(IClasspathEntry entry) {
        for (IClasspathAttribute attribute : entry.getExtraAttributes()) {
            if (attribute.getName().equals("gradle_used_by_scope")) {
                return Sets.newHashSet(attribute.getValue().split(","));
            }
        }
        return Collections.emptySet();
    }
}
