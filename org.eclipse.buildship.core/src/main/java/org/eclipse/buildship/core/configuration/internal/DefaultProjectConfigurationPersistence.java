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

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingclient.GradleDistribution;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.util.file.RelativePathUtils;
import org.eclipse.buildship.core.util.gradle.GradleDistributionSerializer;

/**
 * Default implementation for {@link ProjectConfigurationPersistence}.
 */
final class DefaultProjectConfigurationPersistence implements ProjectConfigurationPersistence {

    private static final String PREF_NODE = CorePlugin.PLUGIN_ID;

    private static final String PREF_KEY_CONNECTION_PROJECT_DIR = "connection.project.dir";
    private static final String PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION = "connection.gradle.distribution";
    private static final String PREF_KEY_OVERRIDE_WORKSPACE_SETTINGS = "override.workspace.settings";
    private static final String PREF_KEY_BUILD_SCANS_ENABLED = "build.scans.enabled";
    private static final String PREF_KEY_OFFLINE_MODE = "offline.mode";

    @Override
    public void saveProjectConfiguration(ProjectConfiguration projectConfiguration, IProject project) {
        Preconditions.checkNotNull(projectConfiguration);
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(project.isAccessible());
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
        savePreferences(projectConfiguration, preferences);
    }

    @Override
    public void saveProjectConfiguration(ProjectConfiguration projectConfiguration, File projectDir) {
        Preconditions.checkNotNull(projectConfiguration);
        Preconditions.checkNotNull(projectDir);
        PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(projectDir, PREF_NODE));
        savePreferences(projectConfiguration, preferences);
    }

    @Override
    public void saveRootProjectLocation(IProject project, File rootProjectDir) {
        Preconditions.checkNotNull(project);
        Preconditions.checkNotNull(rootProjectDir);
        Preconditions.checkArgument(project.isAccessible());
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
        saveRootDirPreference(projectRootToRelativePath(project, rootProjectDir), preferences);
    }

    @Override
    public void saveRootProjectLocation(File projectDir, File rootProjectDir) {
        Preconditions.checkNotNull(projectDir);
        Preconditions.checkNotNull(rootProjectDir);
        PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(projectDir, PREF_NODE));
        saveRootDirPreference(projectRootToRelativePath(projectDir, rootProjectDir), preferences);
    }

    @Override
    public void deleteRootProjectLocation(IProject project) {
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(project.isAccessible());
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
        deleteRootDirPreference(preferences);
    }

    @Override
    public ProjectConfiguration readProjectConfiguration(IProject project) {
        Preconditions.checkNotNull(project);
        try {
            PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
            return readPreferences(preferences, project.getLocation().toFile());
        } catch (Exception e) {
            // when the project is being imported, the configuration file might not be visible from the
            // Eclipse resource API; in that case we fall back to raw IO operations
            // a similar approach is used in JDT core to load the .classpath file
            // see org.eclipse.jdt.internal.core.JavaProject.readFileEntriesWithException(Map)
            PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(project, PREF_NODE));
            return readPreferences(preferences, project.getLocation().toFile());
        }
    }

    @Override
    public ProjectConfiguration readProjectConfiguration(File rootDir) {
        Preconditions.checkNotNull(rootDir);
        PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(rootDir, PREF_NODE));
        return readPreferences(preferences, rootDir);
    }

    @Override
    public File readRootProjectLocation(IProject project) {
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(project.isAccessible());
        try {
            PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
            return relativePathToProjectRoot(project, preferences.readString(PREF_KEY_CONNECTION_PROJECT_DIR));
        } catch (Exception exception) {
            PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(project.getLocation().toFile(), PREF_NODE));
            return relativePathToProjectRoot(project, preferences.readString(PREF_KEY_CONNECTION_PROJECT_DIR));
        }
    }

    @Override
    public File readRootProjectLocation(File projectDir) {
        Preconditions.checkNotNull(projectDir);
        PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(projectDir, PREF_NODE));
        return relativePathToProjectRoot(projectDir, preferences.readString(PREF_KEY_CONNECTION_PROJECT_DIR));
    }

    @Override
    public void deleteProjectConfiguration(IProject project) {
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(project.isAccessible());
        deletePreferences(PreferenceStore.forProjectScope(project, PREF_NODE));
    }

    @Override
    public void deleteProjectConfiguration(File projectDir) {
        Preconditions.checkNotNull(projectDir);
        deletePreferences(PreferenceStore.forPreferenceFile(getProjectPrefsFile(projectDir, PREF_NODE)));
    }

    private static void savePreferences(ProjectConfiguration projectConfiguration, PreferenceStore preferences) {
        String gradleDistribution = GradleDistributionSerializer.INSTANCE.serializeToString(projectConfiguration.getGradleDistribution());
        preferences.write(PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, gradleDistribution);
        if (projectConfiguration.isOverrideWorkspaceSettings()) {
            preferences.writeBoolean(PREF_KEY_OVERRIDE_WORKSPACE_SETTINGS, projectConfiguration.isOverrideWorkspaceSettings());
            preferences.writeBoolean(PREF_KEY_BUILD_SCANS_ENABLED, projectConfiguration.isBuildScansEnabled());
            preferences.writeBoolean(PREF_KEY_OFFLINE_MODE, projectConfiguration.isOfflineMode());
        } else {
            preferences.delete(PREF_KEY_OVERRIDE_WORKSPACE_SETTINGS);
            preferences.delete(PREF_KEY_BUILD_SCANS_ENABLED);
            preferences.delete(PREF_KEY_OFFLINE_MODE);
        }
        preferences.flush();
    }

    private static void saveRootDirPreference(String pathToRoot, PreferenceStore preferences) {
        preferences.write(PREF_KEY_CONNECTION_PROJECT_DIR, pathToRoot);
        preferences.flush();
    }

    private static void deleteRootDirPreference(PreferenceStore preferences) {
        preferences.delete(PREF_KEY_CONNECTION_PROJECT_DIR);
        preferences.flush();
    }

    private static ProjectConfiguration readPreferences(PreferenceStore preferences, File rootDir) {
        String distribution = preferences.readString(PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION);
        boolean overrideWorkspaceSettings = preferences.readBoolean(PREF_KEY_OVERRIDE_WORKSPACE_SETTINGS, false);
        boolean buildScansEnabled = preferences.readBoolean(PREF_KEY_BUILD_SCANS_ENABLED, false);
        boolean offlineMode = preferences.readBoolean(PREF_KEY_OFFLINE_MODE, false);

        GradleDistribution gradleDistribution = GradleDistributionSerializer.INSTANCE.deserializeFromString(distribution);
        if (overrideWorkspaceSettings) {
            return ProjectConfiguration.from(rootDir, gradleDistribution, overrideWorkspaceSettings, buildScansEnabled, offlineMode);
        } else {
            return ProjectConfiguration.fromWorkspaceConfig(rootDir, gradleDistribution);
        }
    }

    private static void deletePreferences(PreferenceStore preferences) {
        preferences.delete(PREF_KEY_CONNECTION_PROJECT_DIR);
        preferences.delete(PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION);
        preferences.delete(PREF_KEY_OVERRIDE_WORKSPACE_SETTINGS);
        preferences.delete(PREF_KEY_BUILD_SCANS_ENABLED);
        preferences.delete(PREF_KEY_OFFLINE_MODE);
        preferences.flush();
    }

    private static File getProjectPrefsFile(File projectDir, String node) {
        return new File(projectDir, ".settings/" + node + ".prefs");
    }

    private static File getProjectPrefsFile(IProject project, String node) {
        return getProjectPrefsFile(project.getLocation().toFile(), node);
    }

    private static String projectRootToRelativePath(IProject project, File rootDir) {
        IPath rootProjectPath = new Path(rootDir.getPath());
        IPath projectPath = project.getLocation();
        return RelativePathUtils.getRelativePath(projectPath, rootProjectPath).toPortableString();
    }

    private static String projectRootToRelativePath(File projectDir, File rootDir) {
        IPath rootProjectPath = new Path(rootDir.getPath());
        IPath projectPath = new Path(projectDir.getPath());
        return RelativePathUtils.getRelativePath(projectPath, rootProjectPath).toPortableString();
    }

    private static File relativePathToProjectRoot(IProject project, String path) {
        IPath pathToRoot = new Path(path);
        if (pathToRoot.isAbsolute()) {
            return pathToRoot.toFile();
        } else {
            return RelativePathUtils.getAbsolutePath(project.getLocation(), pathToRoot).toFile();
        }
    }

    private static File relativePathToProjectRoot(File projectDir, String path) {
        IPath pathToRoot = new Path(path);
        IPath projectPath = new Path(projectDir.getPath());
        if (pathToRoot.isAbsolute()) {
            return pathToRoot.toFile();
        } else {
            return RelativePathUtils.getAbsolutePath(projectPath, pathToRoot).toFile();
        }
    }

}
