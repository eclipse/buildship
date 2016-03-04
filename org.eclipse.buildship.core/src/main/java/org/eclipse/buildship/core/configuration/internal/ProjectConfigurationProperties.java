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

package org.eclipse.buildship.core.configuration.internal;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.util.preference.ProjectScopeUtils;

/**
 * Project configuration properties.
 */
public enum ProjectConfigurationProperties {

    PROJECT_PATH("project.path"),
    CONNECTION_PROJECT_DIR("connection.project.dir"),
    CONNECTION_GRADLE_USER_HOME("connection.gradle.user.home"),
    CONNECTION_GRADLE_DISTRIBUTION( "connection.gradle.distribution"),
    CONNECTION_JAVA_HOME("connection.java.home"),
    CONNECTION_JVM_ARGUMENTS("connection.jvm.arguments"),
    CONNECTION_ARGUMENTS("connection.arguments");

    private static final Map<String, ProjectConfigurationProperties> keyToEntryMap;

    static {
        ImmutableMap.Builder<String, ProjectConfigurationProperties> map = ImmutableMap.builder();
        for (ProjectConfigurationProperties pref : ProjectConfigurationProperties.values()) {
            map.put(pref.key, pref);
        }
        keyToEntryMap = map.build();
    }

    private final String key;

    ProjectConfigurationProperties(String key) {
        this.key = key;
    }

    /**
     * Loads the project configuration properties from the project preferences.
     *
     * @param project the source project
     * @return the project configuration properties
     */
    public static Map<ProjectConfigurationProperties, String> loadAll(IProject project) {
        Map<String, String> preferences = ProjectScopeUtils.load(project, CorePlugin.PLUGIN_ID, keyToEntryMap.keySet());
        Map<ProjectConfigurationProperties, String> result = Maps.newHashMap();
        for (String key : preferences.keySet()) {
            String value = preferences.get(key);
            if (value.equals("null")) {
                value = null;
            }
            result.put(keyToEntryMap.get(key), value);
        }
        return result;
    }

    /**
     * Store all project configuration properties in the project preferences.
     *
     * @param project the target project
     * @param values the values to store
     */
    public static void storeAll(IProject project, Map<ProjectConfigurationProperties, String> values) {
        Map<String, String> rawValues = Maps.newHashMap();
        for (ProjectConfigurationProperties pref : values.keySet()) {
            String value = values.get(pref);
            if (value == null) {
                value = "null";
            }
            rawValues.put(pref.key, value);
        }
        ProjectScopeUtils.store(project, CorePlugin.PLUGIN_ID, rawValues);
    }

    /**
     * Deletes all project configuration properties from the project preferences.
     *
     * @param project the target project
     */
    public static void deleteAll(IProject project) {
        ProjectScopeUtils.delete(project, CorePlugin.PLUGIN_ID, keyToEntryMap.keySet());
    }
}
