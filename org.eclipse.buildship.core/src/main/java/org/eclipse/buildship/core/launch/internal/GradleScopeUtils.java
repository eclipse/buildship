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
     * Collects the scopes used by the target launch configuration.
     * <p/>
     * If the scopes cannot be obtained (e.g. the target configuration is not supported) or if the
     * current Gradle version doesn't provide scope information then the method returns and empty
     * set. Upon exception the message is logged and an empty set is returned.
     *
     * @param configuration the target launch configuration
     * @return the source set names
     */
    public static Set<String> collectScopes(ILaunchConfiguration configuration) {
        try {
            Set<IPackageFragmentRoot> soureFolders = SupportedLaunchConfigType.collectSourceFolders(configuration);
            Builder<String> result = ImmutableSet.builder();
            for (IPackageFragmentRoot sourceFolder : soureFolders) {
                result.addAll(usedByScopesFor(sourceFolder.getRawClasspathEntry()));
            }
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot collect source set information for launch configuration " + configuration.getName(), e);
        }
        return Collections.emptySet();
    }

    /**
     * Returns {@code true} if the target classpath entry is part of the target scopes.
     * <p/>
     * If the entry doesn't define scope information or the set of scopes isempty then this method
     * returns {@code true}.
     *
     * @param entry the target classpath entry
     * @param scopes the name of the scopes to look for
     * @return
     */
    public static boolean isEntryInScope(IClasspathEntry entry, Set<String> scopes) {
        if (scopes.isEmpty()) {
            return true;
        }

        Set<String> libraryScopes = scopesFor(entry);
        if (libraryScopes.isEmpty()) {
            return true;
        }

        return !Sets.intersection(scopes, libraryScopes).isEmpty();
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
