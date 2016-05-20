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
import java.io.IOException;

import com.google.common.base.Preconditions;

import org.eclipse.core.resources.IProject;

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
    private static final String PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION = "connection.gradle.distribution";
    private static final String DEPRECATED_PREF_KEY_CONNECTION_GRADLE_USER_HOME = "connection.gradle.user.home";
    private static final String DEPRECATED_PREF_KEY_CONNECTION_JAVA_HOME = "connection.java.home";
    private static final String DEPRECATED_PREF_KEY_CONNECTION_JVM_ARGUMENTS = "connection.jvm.arguments";
    private static final String DEPRECATED_PREF_KEY_CONNECTION_ARGUMENTS = "connection.arguments";

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
                throw new GradlePluginsRuntimeException(String.format("Cannot load project configuration for project %s.", project.getName()), e2);
            }
        }
    }

    private static ProjectConfiguration loadFromPreferencesApi(IProject project) {
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
        return readProjectConfiguration(project, preferences);
    }

    private static ProjectConfiguration readProjectConfiguration(IProject project, PreferenceStore preferences) {
        String projectPath = preferences.read(PREF_KEY_PROJECT_PATH);
        String projectDir = preferences.read(PREF_KEY_CONNECTION_PROJECT_DIR);
        String gradleDistribution = preferences.read(PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION);
        return ProjectConfigurationProperties.from(projectPath, projectDir, gradleDistribution).toProjectConfiguration(project);
    }

    private static ProjectConfiguration loadFromPropertiesFile(IProject project) throws IOException {
        // when the project is being imported, the configuration file might not be visible from the
        // Eclipse resource API; in that case we fall back to raw IO operations
        // a similar approach is used in JDT core to load the .classpath file
        // see org.eclipse.jdt.internal.core.JavaProject.readFileEntriesWithException(Map)
        PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(project, PREF_NODE));
        return readProjectConfiguration(project, preferences);
    }

    private static File getProjectPrefsFile(IProject project, String node) {
        return new File(project.getLocation().toFile(), ".settings/" + node + ".prefs");
    }

    @Override
    public void saveProjectConfiguration(ProjectConfiguration projectConfiguration, IProject project) {
        Preconditions.checkNotNull(projectConfiguration);
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(project.isAccessible());

        ProjectConfigurationProperties properties = ProjectConfigurationProperties.from(project, projectConfiguration);
        try {
            PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
            preferences.write(PREF_KEY_PROJECT_PATH, properties.getProjectPath());
            preferences.write(PREF_KEY_CONNECTION_PROJECT_DIR, properties.getProjectDir());
            preferences.write(PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, properties.getGradleDistribution());
            preferences.delete(DEPRECATED_PREF_KEY_CONNECTION_GRADLE_USER_HOME);
            preferences.delete(DEPRECATED_PREF_KEY_CONNECTION_JAVA_HOME);
            preferences.delete(DEPRECATED_PREF_KEY_CONNECTION_JVM_ARGUMENTS);
            preferences.delete(DEPRECATED_PREF_KEY_CONNECTION_ARGUMENTS);
            preferences.flush();
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot store project-scope preferences in project %s.", project.getName()), e);
        }
    }

    @Override
    public void deleteProjectConfiguration(IProject project) {
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(project.isAccessible());

        try {
            PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
            preferences.delete(PREF_KEY_PROJECT_PATH);
            preferences.delete(PREF_KEY_CONNECTION_PROJECT_DIR);
            preferences.delete(PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION);
            preferences.delete(DEPRECATED_PREF_KEY_CONNECTION_GRADLE_USER_HOME);
            preferences.delete(DEPRECATED_PREF_KEY_CONNECTION_JAVA_HOME);
            preferences.delete(DEPRECATED_PREF_KEY_CONNECTION_JVM_ARGUMENTS);
            preferences.delete(DEPRECATED_PREF_KEY_CONNECTION_ARGUMENTS);
            preferences.flush();
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot delete project-scope preferences in project %s.", project.getName()), e);
        }
    }

}
