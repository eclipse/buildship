/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.launch.impl;

import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.util.classpath.ClasspathUtils;

/**
 * Represents the scope associated with the current launch configuration.
 *
 * @author Donat Csikos
 */
public abstract class LaunchConfigurationScope {

    /**
     * Launch configuration scope that doesn't filter any entries.
     */
    public static final LaunchConfigurationScope INCLUDE_ALL = new IncludeAllLaunchConfigurationScope();

    /**
     * Returns {@code true} if the classpath entry is part of this scope.
     *
     * @param entry the target classpath entry
     * @param scopes the name of the scopes to look for
     *
     * @return whether the scopes contain the the entry's dependency scope
     */
    public abstract boolean isEntryIncluded(IClasspathEntry entry);

    /**
     * Creates a launch configuration scope from the target launch configuration. If the scope
     * information cannot be calculated then the result scope doesn't filter any entries.
     *
     * @param configuration the target launch configuration
     * @return the created scope
     */
    public static LaunchConfigurationScope from(ILaunchConfiguration configuration) {
        Set<String> result = Sets.newHashSet();
        try {
            Set<IPackageFragmentRoot> soureFolders = SupportedLaunchConfigType.collectSourceFolders(configuration);
            for (IPackageFragmentRoot sourceFolder : soureFolders) {
                Optional<Set<String>> scope = ClasspathUtils.scopesFor(sourceFolder.getRawClasspathEntry());
                if (!scope.isPresent()) {
                    return INCLUDE_ALL;
                }
                result.addAll(scope.get());
            }
            return new FilteringLaunchConfigurationScope(result);
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot collect dependency scope information for launch configuration " + configuration.getName(), e);
            return INCLUDE_ALL;
        }
    }


    /**
     * Doesn't filter any entries.
     */
    private static final class IncludeAllLaunchConfigurationScope extends LaunchConfigurationScope {

        @Override
        public boolean isEntryIncluded(IClasspathEntry entry) {
            return true;
        }
    }

    /**
     * Filters entries if they are not used by the represented scopes.
     */
    private static final class FilteringLaunchConfigurationScope extends LaunchConfigurationScope {

        private final Set<String> scopes;

        public FilteringLaunchConfigurationScope(Set<String> scopes) {
            this.scopes = scopes;
        }

        @Override
        public boolean isEntryIncluded(IClasspathEntry entry) {
            if (this.scopes == null || this.scopes.isEmpty()) {
                return true;
            }

            Optional<Set<String>> entryUsedByScopes = ClasspathUtils.usedByScopesFor(entry);
            if (!entryUsedByScopes.isPresent() || entryUsedByScopes.get().isEmpty()) {
                return true;
            }

            return !Sets.intersection(this.scopes, entryUsedByScopes.get()).isEmpty();
        }
    }
}
