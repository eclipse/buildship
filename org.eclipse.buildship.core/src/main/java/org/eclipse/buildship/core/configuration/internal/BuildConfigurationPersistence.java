/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.configuration.internal;

import java.io.File;

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingclient.GradleDistribution;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.util.gradle.GradleDistributionSerializer;

/**
 * Provides capability to read and save configuration properties on a target project.
 *
 * @author Donat Csikos
 */
final class BuildConfigurationPersistence {

    private static final String PREF_NODE = CorePlugin.PLUGIN_ID;

    private static final String PREF_KEY_CONNECTION_PROJECT_DIR = "connection.project.dir";
    private static final String PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION = "connection.gradle.distribution";
    private static final String PREF_KEY_OVERRIDE_WORKSPACE_SETTINGS = "override.workspace.settings";
    private static final String PREF_KEY_BUILD_SCANS_ENABLED = "build.scans.enabled";
    private static final String PREF_KEY_OFFLINE_MODE = "offline.mode";

    public BuildConfigurationProperties readBuildConfiguratonProperties(IProject project) {
        try {
            PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
            return readPreferences(preferences, project.getLocation().toFile());
        } catch (Exception e) {
            // when the project is being imported, the configuration file might not be visible from the
            // Eclipse resource API; in that case we fall back to raw IO operations
            // a similar approach is used in JDT core to load the .classpath file
            // see org.eclipse.jdt.internal.core.JavaProject.readFileEntriesWithException(Map)
            return readBuildConfiguratonProperties(project.getLocation().toFile());
        }
    }

    public BuildConfigurationProperties readBuildConfiguratonProperties(File projectDir) {
        PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(projectDir, PREF_NODE));
        return readPreferences(preferences, projectDir);
    }

    public void saveBuildConfiguration(IProject project, BuildConfigurationProperties properties) {
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
        savePreferences(properties, preferences);
    }

    public void saveBuildConfiguration(File projectDir, BuildConfigurationProperties properties) {
        PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(projectDir, PREF_NODE));
        savePreferences(properties, preferences);
    }

    public String readPathToRoot(IProject project) {
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
        return preferences.readString(PREF_KEY_CONNECTION_PROJECT_DIR);
    }

    public String readPathToRoot(File projectDir) {
        Preconditions.checkNotNull(projectDir);
        PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(projectDir, PREF_NODE));
        return preferences.readString(PREF_KEY_CONNECTION_PROJECT_DIR);
    }

    public void savePathToRoot(IProject project, String pathToRoot) {
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
        saveRootDirPreference(pathToRoot, preferences);

    }

    public void savePathToRoot(File projectDir, String pathToRoot) {
        PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(projectDir, PREF_NODE));
        saveRootDirPreference(pathToRoot, preferences);
    }

    public void deleteProjectConfiguration(IProject project) {
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
        deleteRootDirPreference(preferences);
    }

    public void deleteProjectConfiguration(File projectDir) {
        PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(projectDir, PREF_NODE));
        deleteRootDirPreference(preferences);
    }

    private static BuildConfigurationProperties readPreferences(PreferenceStore preferences, File rootDir) {
        String distribution = preferences.readString(PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION);
        boolean overrideWorkspaceSettings = preferences.readBoolean(PREF_KEY_OVERRIDE_WORKSPACE_SETTINGS, false);
        boolean buildScansEnabled = preferences.readBoolean(PREF_KEY_BUILD_SCANS_ENABLED, false);
        boolean offlineMode = preferences.readBoolean(PREF_KEY_OFFLINE_MODE, false);
        GradleDistribution gradleDistribution = GradleDistributionSerializer.INSTANCE.deserializeFromString(distribution);
        return new BuildConfigurationProperties(rootDir, gradleDistribution, overrideWorkspaceSettings, buildScansEnabled, offlineMode);
    }

    private static void savePreferences(BuildConfigurationProperties properties, PreferenceStore preferences) {
        String gradleDistribution = GradleDistributionSerializer.INSTANCE.serializeToString(properties.getGradleDistribution());
        preferences.write(PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, gradleDistribution);
        if (properties.isOverrideWorkspaceSettings()) {
            preferences.writeBoolean(PREF_KEY_OVERRIDE_WORKSPACE_SETTINGS, properties.isOverrideWorkspaceSettings());
            preferences.writeBoolean(PREF_KEY_BUILD_SCANS_ENABLED, properties.isBuildScansEnabled());
            preferences.writeBoolean(PREF_KEY_OFFLINE_MODE, properties.isOfflineMode());
        } else {
            preferences.delete(PREF_KEY_OVERRIDE_WORKSPACE_SETTINGS);
            preferences.delete(PREF_KEY_BUILD_SCANS_ENABLED);
            preferences.delete(PREF_KEY_OFFLINE_MODE);
        }
        preferences.flush();
    }

    private static File getProjectPrefsFile(File projectDir, String node) {
        return new File(projectDir, ".settings/" + node + ".prefs");
    }

    private void saveRootDirPreference(String pathToRoot, PreferenceStore preferences) {
        preferences.write(PREF_KEY_CONNECTION_PROJECT_DIR, pathToRoot);
        preferences.flush();
    }

    private void deleteRootDirPreference(PreferenceStore preferences) {
        preferences.delete(PREF_KEY_CONNECTION_PROJECT_DIR);
        preferences.flush();
    }
}
