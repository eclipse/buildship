/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.util.preference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;

/**
 * Utility methods to work with project-scoped Eclipse preferences.
 */
public class ProjectScopeUtils {

    private ProjectScopeUtils() {
    }

    /**
     * Reads preferences stored in the project scope.
     * <p/>
     * If the scope is not yet readable (for instance when the project is being opened with the
     * {@code IResource.BACKGROUND_REFRESH} flag), than the method falls back to read the
     * {@code $project_loc/.settings/$node.prefs} properties file.
     *
     * @param project the project to read the preferences from
     * @param node the preference node to read
     * @param keys the preference keys to read
     * @throws IllegalArgumentException if the project is not accessible
     * @throws GradlePluginsRuntimeException if the preferences cannot be loaded or if at least the
     *             one of the requested preference is not present
     *
     * @return the map of read preferences
     *
     * @see IEclipsePreferences
     */
    public static Map<String, String> load(IProject project, String node, Set<String> keys) {
        Preconditions.checkNotNull(project);
        Preconditions.checkNotNull(node);
        Preconditions.checkNotNull(keys);
        Preconditions.checkArgument(project.isAccessible());

        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            return loadFromPreferencesApi(project, node, keys);
        } catch (Exception e1) {
            try {
                return loadFromPropertiesFile(project, node, keys);
            } catch (IOException e2) {
                throw new GradlePluginsRuntimeException(String.format("Cannot load project-scope preference in node %s in project %s", node, project.getName()), e2);
            }
        }
    }

    private static Map<String, String> loadFromPreferencesApi(IProject project, String node, Set<String> keys) {
        ProjectScope scope = new ProjectScope(project);
        IEclipsePreferences preferences = scope.getNode(node);
        ImmutableMap.Builder<String, String> result = ImmutableMap.builder();
        for (String key : keys) {
            String value = preferences.get(key, null);
            if (value != null) {
                result.put(key, value);
            } else {
                throw new GradlePluginsRuntimeException(
                        String.format("No value is found for key %s in project-scoped preference node %s in project %s", key, node, project.getName()));
            }
        }
        return result.build();
    }

    private static Map<String, String> loadFromPropertiesFile(IProject project, String node, Set<String> keys) throws IOException {
        // when the project is being imported, the configuration file might not be visible from the
        // Eclipse resource API; in that case we fall back to raw IO operations
        // a similar approach is used in JDT core to load the .classpath file
        // see org.eclipse.jdt.internal.core.JavaProject.readFileEntriesWithException(Map)
        InputStreamReader reader = null;
        try {
            File propertiesFile = getProjectPrefsFile(project, node);
            reader = new InputStreamReader(new FileInputStream(propertiesFile), Charsets.UTF_8);
            Properties properties = new Properties();
            properties.load(reader);
            ImmutableMap.Builder<String, String> result = ImmutableMap.builder();
            for (String key : keys) {
                String value = properties.getProperty(key, null);
                if (value != null) {
                    result.put(key, value);
                } else {
                    throw new GradlePluginsRuntimeException(
                            String.format("No value is found for key %s in project-scoped preference node %s in project %s", key, node, project.getName()));
                }
            }
            return result.build();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private static File getProjectPrefsFile(IProject project, String node) {
        return new File(project.getLocation().toFile(), ".settings/" + node + ".prefs");
    }

    /**
     * Stores preferences in the project scope.
     *
     * @param project the target project where to store the preferences
     * @param node the node where to store the preferences
     * @param entries the key-value pairs to store
     * @throws IllegalArgumentException if the project is not accessible
     * @throws GradlePluginsRuntimeException if the preferences can't be persisted
     * @see IEclipsePreferences
     */
    public static void store(IProject project, String node, Map<String, String> entries) {
        Preconditions.checkNotNull(project);
        Preconditions.checkNotNull(node);
        Preconditions.checkNotNull(entries);
        Preconditions.checkArgument(project.isAccessible());

        if (entries.isEmpty()) {
            return;
        }

        try {
            ProjectScope scope = new ProjectScope(project);
            IEclipsePreferences preferences = scope.getNode(node);
            for (String key : entries.keySet()) {
                preferences.put(key, entries.get(key));
            }

            preferences.flush();
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot store project-scope preferences %s from node %s from project %s", entries, node, project.getName()), e);
        }
    }

    /**
     * Deletes preferences from the project scope.
     *
     * @param project the target project where to delete preferences from
     * @param node the node to remove preferences from
     * @param keys the preference keys to remove
     * @throws IllegalArgumentException if the project is not accessible
     * @throws GradlePluginsRuntimeException if the changes can't be persisted
     * @see IEclipsePreferences
     */
    public static void delete(IProject project, String node, Set<String> keys) {
        Preconditions.checkNotNull(project);
        Preconditions.checkNotNull(node);
        Preconditions.checkNotNull(keys);
        Preconditions.checkArgument(project.isAccessible());

        if (keys.isEmpty()) {
            return;
        }

        try {
            ProjectScope scope = new ProjectScope(project);
            IEclipsePreferences preferences = scope.getNode(node);
            for (String key : keys) {
                preferences.remove(key);
            }
            preferences.flush();
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot delete project-scope preferences %s from node %s from project %s", keys, node, project.getName()), e);
        }

    }
}
