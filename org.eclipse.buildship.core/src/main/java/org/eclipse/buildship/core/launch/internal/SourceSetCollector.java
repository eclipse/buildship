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
 * Contains utility methods to collect Gradle source set information.
 *
 * @author Donat Csikos
 */
public final class SourceSetCollector {

    private SourceSetCollector() {
    }

    /**
     * TODO (donat) adjust method name and phrasing so that it also applies to junit executions
     *
     * Finds the main class referenced by the target launch configuration and collects the Gradle
     * source sets that contains the class.
     * <p/>
     * If the main class cannot be obtained (e.g. the target configuration is not a JDT launch) or
     * if source folder of the class doesn't define any information about the Gradle source sets
     * then the method returns and empty set. Upon exception the message is logged and an empty set
     * is returned.
     *
     * @param configuration the target launch configuration
     * @return the source set names
     */
    public static Set<String> mainClassSourceSets(ILaunchConfiguration configuration) {
        try {
            Set<IPackageFragmentRoot> soureFolders = SupportedLaunchConfigType.collectRelatedSourceFolders(configuration);
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
     * Determines whether any of the specified source sets contain the target classpath entry.
     * <p/>
     * If the entry doesn't define source set information or the source set names are empty then
     * this method returns {@code true}.
     *
     * @param entry the target classpath entry
     * @param sourceSetNames the name of the source sets to look for
     * @return
     */
    public static boolean isEntryInSourceSets(IClasspathEntry entry, Set<String> sourceSetNames) {
        if (sourceSetNames.isEmpty()) {
            return true;
        }

        Set<String> librarySourceSets = scopesFor(entry);
        if (librarySourceSets.isEmpty()) {
            return true;
        }

        return !Sets.intersection(sourceSetNames, librarySourceSets).isEmpty();
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
