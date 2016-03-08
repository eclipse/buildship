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
import java.lang.reflect.Type;
import java.util.Map;

import org.osgi.service.prefs.BackingStoreException;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;

/**
 * Persistence delegate which cleans up the legacy, json-based project configuration format.
 */
final class LegacyCleaningProjectConfigurationPersistence implements ProjectConfigurationPersistence {

    private static final String LEGACY_GRADLE_PREFERENCES_LOCATION = ".settings/gradle.prefs";
    private static final String LEGACY_GRADLE_PREFERENCES_FILE_NAME_WITHOUT_EXTENSION = "gradle";

    private final ProjectConfigurationPersistence delegate;

    LegacyCleaningProjectConfigurationPersistence(ProjectConfigurationPersistence delegate) {
        this.delegate = Preconditions.checkNotNull(delegate);
    }

    @Override
    public void saveProjectConfiguration(ProjectConfiguration configuration, IProject project) {
        cleanupLegacyConfiguration(project);
        this.delegate.saveProjectConfiguration(configuration, project);
    }

    @Override
    public void deleteProjectConfiguration(IProject project) {
        this.delegate.deleteProjectConfiguration(project);
    }

    @Override
    public ProjectConfiguration readProjectConfiguration(IProject project) {
        if (hasLegacyConfiguration(project)) {
            return readLegacyProjectConfiguration(project);
        } else {
            return this.delegate.readProjectConfiguration(project);
        }
    }

    private static boolean hasLegacyConfiguration(IProject project) {
        return getLegacyConfigurationFile(project).exists();
    }

    private static File getLegacyConfigurationFile(IProject project) {
        return new File(project.getLocation().toFile(), LEGACY_GRADLE_PREFERENCES_LOCATION);
    }

    private static ProjectConfiguration readLegacyProjectConfiguration(IProject workspaceProject) {
        return readLegacyConfiguration(workspaceProject).toProjectConfiguration(workspaceProject);
    }

    private static ProjectConfigurationProperties readLegacyConfiguration(IProject project) {
        File gradlePrefsFile = getLegacyConfigurationFile(project);
        Map<String, Map<String, String>> parsedJson = null;
        try {
            String json = Files.toString(gradlePrefsFile, Charsets.UTF_8);
            Gson gson = new GsonBuilder().create();
            parsedJson = gson.fromJson(json, createMapTypeToken());
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot retrieve legacy project configuration for project %s.", project.getName()), e);
        }
        Map<String, String> config = parsedJson.get("1.0");

        String projectPath = config.get("project_path");
        String projectDir = config.get("connection_project_dir");
        String gradleUserHome = config.get("connection_gradle_user_home");
        String gradleDistribution = config.get("connection_gradle_distribution");
        String javaHome = config.get("connection_java_home");
        String jvmArguments = config.get("connection_jvm_arguments");
        String arguments = config.get("connection_arguments");
        return ProjectConfigurationProperties.from(projectPath, projectDir, gradleUserHome, gradleDistribution, javaHome, jvmArguments, arguments);
    }

    @SuppressWarnings("serial")
    private static Type createMapTypeToken() {
        return new TypeToken<Map<String, Map<String, String>>>() {}.getType();
    }

    private static void cleanupLegacyConfiguration(IProject project) {
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(project.isAccessible());

        if (hasLegacyConfiguration(project)) {
            try {
                ensureNoProjectPreferencesLoadedFrom(project);
                getLegacyConfigurationFile(project).delete();
            } catch (Exception e) {
                throw new GradlePluginsRuntimeException(String.format("Cannot clean up legacy project configuration for project %s.", project.getName()), e);
            }
        }
    }

    /*
     * The ${project_name}/.settings/gradle.prefs file is automatically loaded as project
     * preferences by the core runtime since the fie extension is '.prefs'. If the preferences
     * are loaded, then deleting the prefs file results in a BackingStoreException.
     */
    private static void ensureNoProjectPreferencesLoadedFrom(IProject project) throws BackingStoreException {
        ProjectScope projectScope = new ProjectScope(project);
        IEclipsePreferences node = projectScope.getNode(LEGACY_GRADLE_PREFERENCES_FILE_NAME_WITHOUT_EXTENSION);
        if (node != null) {
            node.removeNode();
        }
    }

}
