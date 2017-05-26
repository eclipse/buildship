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
import org.eclipse.core.runtime.Path;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
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
    private static final String PREF_KEY_GRADLE_USER_HOME = "gradle.user.home";
    private static final String PREF_KEY_BUILD_SCANS_ENABLED = "build.scans.enabled";
    private static final String PREF_KEY_OFFLINE_MODE = "offline.mode";

    public DefaultBuildConfigurationProperties readBuildConfiguratonProperties(IProject project) {
        Preconditions.checkNotNull(project);
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
        return readPreferences(preferences, project.getLocation().toFile());
    }

    public DefaultBuildConfigurationProperties readBuildConfiguratonProperties(File projectDir) {
        Preconditions.checkNotNull(projectDir);
        PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(projectDir, PREF_NODE));
        return readPreferences(preferences, projectDir);
    }

    public void saveBuildConfiguration(IProject project, DefaultBuildConfigurationProperties properties) {
        Preconditions.checkNotNull(project);
        Preconditions.checkNotNull(properties);
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
        savePreferences(properties, preferences);
    }

    public void saveBuildConfiguration(File projectDir, DefaultBuildConfigurationProperties properties) {
        Preconditions.checkNotNull(projectDir);
        Preconditions.checkNotNull(properties);
        PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(projectDir, PREF_NODE));
        savePreferences(properties, preferences);
    }

    public String readPathToRoot(IProject project) {
        Preconditions.checkNotNull(project);
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
        String result = preferences.readString(PREF_KEY_CONNECTION_PROJECT_DIR, null);
        if (result == null) {
            throw new GradlePluginsRuntimeException("Can't read root project location for project " + project.getName());
        }
        return result;
    }

    public String readPathToRoot(File projectDir) {
        Preconditions.checkNotNull(projectDir);
        PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(projectDir, PREF_NODE));
        String result = preferences.readString(PREF_KEY_CONNECTION_PROJECT_DIR, null);
        if (result == null) {
            throw new GradlePluginsRuntimeException("Can't read root project location for project located at " + projectDir.getAbsolutePath());
        }
        return result;
    }

    public void savePathToRoot(IProject project, String pathToRoot) {
        Preconditions.checkNotNull(project);
        Preconditions.checkNotNull(pathToRoot);
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
        saveRootDirPreference(pathToRoot, preferences);

    }

    public void savePathToRoot(File projectDir, String pathToRoot) {
        Preconditions.checkNotNull(projectDir);
        Preconditions.checkNotNull(pathToRoot);
        PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(projectDir, PREF_NODE));
        saveRootDirPreference(pathToRoot, preferences);
    }

    public void deletePathToRoot(IProject project) {
        Preconditions.checkNotNull(project);
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
        deleteRootDirPreference(preferences);
    }

    public void deletePathToRoot(File projectDir) {
        Preconditions.checkNotNull(projectDir);
        PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(projectDir, PREF_NODE));
        deleteRootDirPreference(preferences);
    }

    private static DefaultBuildConfigurationProperties readPreferences(PreferenceStore preferences, File rootDir) {
        boolean overrideWorkspaceSettings = preferences.readBoolean(PREF_KEY_OVERRIDE_WORKSPACE_SETTINGS, false);

        String distributionString = preferences.readString(PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, null);
        if (overrideWorkspaceSettings && distributionString == null) {
            throw new GradlePluginsRuntimeException("Invalid build configuration for project located at " + rootDir.getAbsolutePath());
        }
        GradleDistribution distribution = distributionString == null
                ? GradleDistribution.fromBuild()
                : GradleDistributionSerializer.INSTANCE.deserializeFromString(distributionString);

        String gradleUserHomeString = preferences.readString(PREF_KEY_GRADLE_USER_HOME, "");
        File gradleUserHome = gradleUserHomeString.isEmpty()
                ? null
                : new File(gradleUserHomeString);

        boolean buildScansEnabled = preferences.readBoolean(PREF_KEY_BUILD_SCANS_ENABLED, false);
        boolean offlineMode = preferences.readBoolean(PREF_KEY_OFFLINE_MODE, false);

        return new DefaultBuildConfigurationProperties(rootDir, distribution, gradleUserHome, overrideWorkspaceSettings, buildScansEnabled, offlineMode);
    }

    private static void savePreferences(DefaultBuildConfigurationProperties properties, PreferenceStore preferences) {
        if (properties.isOverrideWorkspaceSettings()) {
            String gradleDistribution = GradleDistributionSerializer.INSTANCE.serializeToString(properties.getGradleDistribution());
            preferences.write(PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, gradleDistribution);
            preferences.write(PREF_KEY_GRADLE_USER_HOME, toPortableString(properties.getGradleUserHome()));
            preferences.writeBoolean(PREF_KEY_OVERRIDE_WORKSPACE_SETTINGS, properties.isOverrideWorkspaceSettings());
            preferences.writeBoolean(PREF_KEY_BUILD_SCANS_ENABLED, properties.isBuildScansEnabled());
            preferences.writeBoolean(PREF_KEY_OFFLINE_MODE, properties.isOfflineMode());
        } else {
            preferences.delete(PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION);
            preferences.delete(PREF_KEY_GRADLE_USER_HOME);
            preferences.delete(PREF_KEY_OVERRIDE_WORKSPACE_SETTINGS);
            preferences.delete(PREF_KEY_BUILD_SCANS_ENABLED);
            preferences.delete(PREF_KEY_OFFLINE_MODE);
        }
        preferences.flush();
    }

    private static String toPortableString(File file) {
        return file == null ? "" : new Path(file.getPath()).toPortableString();
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
