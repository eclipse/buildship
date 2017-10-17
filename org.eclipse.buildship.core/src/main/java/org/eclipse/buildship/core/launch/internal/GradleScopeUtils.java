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
     * Collects the dependency scopes for the target launch configuration.
     * <p/>
     * An empty set is returned if
     * <ul>
     * <li>the scopes cannot be obtained (e.g. the target configuration is not supported),</li>
     * <li>the current Gradle version doesn't provide scope information, or</li>
     * <li>an exception is thrown</li>
     * </ul>
     * .
     *
     * @param configuration the target launch configuration
     * @return the source set names
     */
    public static Set<String> collectScopes(ILaunchConfiguration configuration) {
        Builder<String> result = ImmutableSet.builder();
        try {
            Set<IPackageFragmentRoot> soureFolders = SupportedLaunchConfigType.collectSourceFolders(configuration);
            for (IPackageFragmentRoot sourceFolder : soureFolders) {
                result.addAll(scopesFor(sourceFolder.getRawClasspathEntry()));
            }
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot collect dependency scope information for launch configuration " + configuration.getName(), e);
        }
        return result.build();
    }

    /**
     * Returns {@code true} if the classpath entry is part of the supplied scopes.
     * <p/>
     * If the entry doesn't define scope information or the set of scopes is empty then this method
     * returns {@code true}.
     *
     * @param entry the target classpath entry
     * @param scopes the name of the scopes to look for
     *
     * @return whether the scopes contain the the entry's dependency scope
     */
    public static boolean isEntryUsedByScopes(IClasspathEntry entry, Set<String> scopes) {
        if (scopes.isEmpty()) {
            return true;
        }

        Set<String> entryDependencyScopes = usedByScopesFor(entry);
        if (entryDependencyScopes.isEmpty()) {
            return true;
        }

        return !Sets.intersection(scopes, entryDependencyScopes).isEmpty();
    }

    private static Set<String> scopesFor(IClasspathEntry entry) {
        for (IClasspathAttribute attribute : entry.getExtraAttributes()) {
            if (attribute.getName().equals("gradle_scope")) {
                return Sets.newHashSet(attribute.getValue().split(","));
            }
        }
        return Collections.emptySet();
    }

    private static Set<String> usedByScopesFor(IClasspathEntry entry) {
        for (IClasspathAttribute attribute : entry.getExtraAttributes()) {
            if (attribute.getName().equals("gradle_used_by_scope")) {
                return Sets.newHashSet(attribute.getValue().split(","));
            }
        }
        return Collections.emptySet();
    }
}
