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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;

import org.osgi.service.prefs.BackingStoreException;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.util.file.RelativePathUtils;

/**
 * Cleans up project artifacts created by Buildship versions < 1.0.10.
 */
final class LegacyProjectConfigurationUtils {

    private static final String GRADLE_PREFERENCES_LOCATION = ".settings/gradle.prefs";
    private static final String GRADLE_PREFERENCES_FILE_NAME_WITHOUT_EXTENSION = "gradle";

    private LegacyProjectConfigurationUtils() {
    }

    public static boolean hasLegacyConfiguration(IProject project) {
        return getLegacyConfigurationFile(project).exists();
    }

    private static File getLegacyConfigurationFile(IProject project) {
        return new File(project.getLocation().toFile(), GRADLE_PREFERENCES_LOCATION);
    }

    public static Map<ProjectConfigurationProperties, String> readLegacyConfiguration(IProject project) {
        try {
            File gradlePrefsFile = getLegacyConfigurationFile(project);
            String json = getContents(gradlePrefsFile);

            Gson gson = new GsonBuilder().create();
            Map<String, Object> config = gson.fromJson(json, createMapTypeToken());
            Map<String, String> projectConfig = getProjectConfigForVersion(config);

            Map<ProjectConfigurationProperties, String> legacyConfig = Maps.newHashMap();
            legacyConfig.put(ProjectConfigurationProperties.PROJECT_PATH, projectConfig.get("project_path"));
            legacyConfig.put(ProjectConfigurationProperties.CONNECTION_PROJECT_DIR, relativePathToRootProject(project, new Path(projectConfig.get("connection_project_dir")))) ;
            legacyConfig.put(ProjectConfigurationProperties.CONNECTION_GRADLE_USER_HOME, projectConfig.get("connection_gradle_user_home"));
            legacyConfig.put(ProjectConfigurationProperties.CONNECTION_GRADLE_DISTRIBUTION, projectConfig.get("connection_gradle_distribution"));
            legacyConfig.put(ProjectConfigurationProperties.CONNECTION_JAVA_HOME, projectConfig.get("connection_java_home"));
            legacyConfig.put(ProjectConfigurationProperties.CONNECTION_JVM_ARGUMENTS, projectConfig.get("connection_jvm_arguments"));
            legacyConfig.put(ProjectConfigurationProperties.CONNECTION_ARGUMENTS, projectConfig.get("connection_arguments"));

            return legacyConfig;
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException("Cannot retrieve legacy project configuration", e);
        }
    }

    private static String getContents(File file) throws IOException {
        InputStreamReader reader = null;
        try {
            return CharStreams.toString(reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
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

    @SuppressWarnings("serial")
    private static Type createMapTypeToken() {
        return new TypeToken<Map<String, Object>>() {
        }.getType();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getProjectConfigForVersion(Map<String, Object> config) {
        return (Map<String, String>) config.get("1.0");
    }

    private static String relativePathToRootProject(IProject workspaceProject, IPath rootProjectPath) {
        if (rootProjectPath.isAbsolute()) {
            IPath projectPath = workspaceProject.getLocation();
            return RelativePathUtils.getRelativePath(projectPath, rootProjectPath).toOSString();
        } else {
            return rootProjectPath.toOSString();
        }
    }

    public static void cleanup(IProject project) {
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(project.isAccessible());

        if (hasLegacyConfiguration(project)) {
            try {
                ensureNoProjectPreferencesLoadedFrom(project);
                getLegacyConfigurationFile(project).delete();
            } catch (Exception e) {
                throw new GradlePluginsRuntimeException("Cannot clean up legacy project configuration", e);
            }
        }
    }

    private static void ensureNoProjectPreferencesLoadedFrom(IProject project) throws BackingStoreException {
        // The ${project_name}/.settings/gradle.prefs file is automatically loaded as project
        // preferences by the core runtime since the fie extension is '.prefs'. If the preferences
        // are loaded, then deleting the prefs file results in a BackingStoreException.
        ProjectScope projectScope = new ProjectScope(project);
        IEclipsePreferences node = projectScope.getNode(GRADLE_PREFERENCES_FILE_NAME_WITHOUT_EXTENSION);
        if (node != null) {
            node.removeNode();
        }
    }

}
