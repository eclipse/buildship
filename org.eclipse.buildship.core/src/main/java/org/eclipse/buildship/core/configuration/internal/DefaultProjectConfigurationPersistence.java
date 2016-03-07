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
import java.util.Properties;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;

/**
 * Default implementation for {@link ProjectConfigurationPersistence}.
 */
final class DefaultProjectConfigurationPersistence implements ProjectConfigurationPersistence {

    private static final String PREF_NODE = CorePlugin.PLUGIN_ID;

    private static final String PREF_KEY_PROJECT_PATH = "project.path";
    private static final String PREF_KEY_CONNECTION_PROJECT_DIR = "connection.project.dir";
    private static final String PREF_KEY_CONNECTION_GRADLE_USER_HOME = "connection.gradle.user.home";
    private static final String PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION = "connection.gradle.distribution";
    private static final String PREF_KEY_CONNECTION_JAVA_HOME = "connection.java.home";
    private static final String PREF_KEY_CONNECTION_JVM_ARGUMENTS = "connection.jvm.arguments";
    private static final String PREF_KEY_CONNECTION_ARGUMENTS = "connection.arguments";

    @Override
    public ProjectConfiguration readProjectConfiguration(IProject project) {
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(project.isAccessible());
        try {
            return loadFromPreferencesApi(project);
        } catch (Exception e1) {
            try {
                return loadFromPropertiesFile(project);
            } catch (IOException e2) {
                throw new GradlePluginsRuntimeException(String.format("Cannot load project configuration for project %s", project.getName()), e2);
            }
        }
    }

    private static ProjectConfiguration loadFromPreferencesApi(IProject project) {
        IEclipsePreferences preferences = getEclipsePreferences(project);
        String projectPath = readPreference(preferences, PREF_KEY_PROJECT_PATH);
        String projectDir = readPreference(preferences, PREF_KEY_CONNECTION_PROJECT_DIR);
        String gradleUserHome = readPreference(preferences, PREF_KEY_CONNECTION_GRADLE_USER_HOME);
        String gradleDistribution = readPreference(preferences, PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION);
        String javaHome = readPreference(preferences, PREF_KEY_CONNECTION_JAVA_HOME);
        String jvmArguments = readPreference(preferences, PREF_KEY_CONNECTION_JVM_ARGUMENTS);
        String arguments = readPreference(preferences, PREF_KEY_CONNECTION_ARGUMENTS);
        return ProjectConfigurationProperties.from(projectPath, projectDir, gradleUserHome, gradleDistribution, javaHome, jvmArguments, arguments).toProjectConfiguration(project);
    }

    private static IEclipsePreferences getEclipsePreferences(IProject project) {
        ProjectScope scope = new ProjectScope(project);
        IEclipsePreferences preferences = scope.getNode(PREF_NODE);
        return preferences;
    }

    private static String readPreference(IEclipsePreferences preferences, String key) {
        String value = preferences.get(key, null);
        if ("null".equals(value)) {
            return null;
        } else if (value != null) {
            return value;
        } else {
            throw new GradlePluginsRuntimeException(String.format("No value is found for key %s in project-scoped preference node %s", key, PREF_NODE));
        }
    }

    private static ProjectConfiguration loadFromPropertiesFile(IProject project) throws IOException {
        // when the project is being imported, the configuration file might not be visible from the
        // Eclipse resource API; in that case we fall back to raw IO operations
        // a similar approach is used in JDT core to load the .classpath file
        // see org.eclipse.jdt.internal.core.JavaProject.readFileEntriesWithException(Map)
        InputStreamReader reader = null;
        try {
            File propertiesFile = getProjectPrefsFile(project, PREF_NODE);
            reader = new InputStreamReader(new FileInputStream(propertiesFile), Charsets.UTF_8);
            Properties properties = new Properties();
            properties.load(reader);
            String projectPath = readProperty(properties, PREF_KEY_PROJECT_PATH);
            String projectDir = readProperty(properties, PREF_KEY_CONNECTION_PROJECT_DIR);
            String gradleUserHome = readProperty(properties, PREF_KEY_CONNECTION_GRADLE_USER_HOME);
            String gradleDistribution = readProperty(properties, PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION);
            String javaHome = readProperty(properties, PREF_KEY_CONNECTION_JAVA_HOME);
            String jvmArguments = readProperty(properties, PREF_KEY_CONNECTION_JVM_ARGUMENTS);
            String arguments = readProperty(properties, PREF_KEY_CONNECTION_ARGUMENTS);
            return ProjectConfigurationProperties.from(projectPath, projectDir, gradleUserHome, gradleDistribution, javaHome, jvmArguments, arguments)
                    .toProjectConfiguration(project);
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

    private static String readProperty(Properties properties, String key) {
        String value = properties.getProperty(key, null);
        if ("null".equals(value)) {
            return null;
        } else if (value != null) {
            return value;
        } else {
            throw new GradlePluginsRuntimeException(String.format("No value is found for key %s in properties", key));
        }
    }

    @Override
    public void saveProjectConfiguration(ProjectConfiguration projectConfiguration, IProject project) {
        Preconditions.checkNotNull(projectConfiguration);
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(project.isAccessible());

        ProjectConfigurationProperties properties = ProjectConfigurationProperties.from(project, projectConfiguration);
        try {
            IEclipsePreferences preferences = getEclipsePreferences(project);
            writePreference(preferences, PREF_KEY_PROJECT_PATH, properties.getProjectPath());
            writePreference(preferences, PREF_KEY_CONNECTION_PROJECT_DIR, properties.getProjectDir());
            writePreference(preferences, PREF_KEY_CONNECTION_GRADLE_USER_HOME, properties.getGradleUserHome());
            writePreference(preferences, PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, properties.getGradleDistribution());
            writePreference(preferences, PREF_KEY_CONNECTION_JAVA_HOME, properties.getJavaHome());
            writePreference(preferences, PREF_KEY_CONNECTION_JVM_ARGUMENTS, properties.getJvmArguments());
            writePreference(preferences, PREF_KEY_CONNECTION_ARGUMENTS, properties.getArguments());
            preferences.flush();
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot store project-scope preferences %s in project %s", project.getName()), e);
        }
    }

    private static void writePreference(IEclipsePreferences preferences, String key, String value) {
        String rawValue = value == null ? "null" : value;
        preferences.put(key, rawValue);
    }

    @Override
    public void deleteProjectConfiguration(IProject project) {
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(project.isAccessible());

        try {
            IEclipsePreferences preferences = getEclipsePreferences(project);
            preferences.remove(PREF_KEY_PROJECT_PATH);
            preferences.remove(PREF_KEY_CONNECTION_PROJECT_DIR);
            preferences.remove(PREF_KEY_CONNECTION_GRADLE_USER_HOME);
            preferences.remove(PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION);
            preferences.remove(PREF_KEY_CONNECTION_JAVA_HOME);
            preferences.remove(PREF_KEY_CONNECTION_JVM_ARGUMENTS);
            preferences.remove(PREF_KEY_CONNECTION_ARGUMENTS);
            preferences.flush();
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot delete project-scope preferences in project %s", project.getName()), e);
        }
    }

}
