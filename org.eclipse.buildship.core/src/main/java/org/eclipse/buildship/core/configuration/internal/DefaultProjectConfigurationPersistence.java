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

import com.gradleware.tooling.toolingclient.GradleDistribution;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration;
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
    public ProjectConfiguration readProjectConfiguration(IProject project) {
        Preconditions.checkNotNull(project);
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
        String projectDir = preferences.read(PREF_KEY_CONNECTION_PROJECT_DIR);
        String distribution = preferences.read(PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION);
        boolean overrideWorkspaceSettings = preferences.readBoolean(PREF_KEY_OVERRIDE_WORKSPACE_SETTINGS, false);
        boolean buildScansEnabled = preferences.readBoolean(PREF_KEY_BUILD_SCANS_ENABLED, false);
        boolean offlineMode = preferences.readBoolean(PREF_KEY_OFFLINE_MODE, false);

        File rootDir = rootProjectFile(project, projectDir);
        GradleDistribution gradleDistribution = GradleDistributionSerializer.INSTANCE.deserializeFromString(distribution);
        if (overrideWorkspaceSettings) {
            return ProjectConfiguration.from(rootDir, gradleDistribution, overrideWorkspaceSettings, buildScansEnabled, offlineMode);
        } else {
            WorkspaceConfiguration workspaceConfig = CorePlugin.workspaceConfigurationManager().loadWorkspaceConfiguration();
            return ProjectConfiguration.from(rootDir, gradleDistribution, false, workspaceConfig.isBuildScansEnabled(), workspaceConfig.isOffline());
        }
    }

    private static File rootProjectFile(IProject project, String pathToRootProject) {
        org.eclipse.core.runtime.Path rootPath = new org.eclipse.core.runtime.Path(pathToRootProject);
        if (rootPath.isAbsolute()) {
            return rootPath.toFile();
        } else {
            return RelativePathUtils.getAbsolutePath(project.getLocation(), rootPath).toFile();
        }
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

        String projectDir = relativePathToRootProject(project, projectConfiguration.getRootProjectDirectory());
        String gradleDistribution = GradleDistributionSerializer.INSTANCE.serializeToString(projectConfiguration.getGradleDistribution());
        try {
            PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
            preferences.write(PREF_KEY_CONNECTION_PROJECT_DIR, projectDir);
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
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot store project-scope preferences in project %s.", project.getName()), e);
        }
    }

    private static String relativePathToRootProject(IProject project, File rootProjectDir) {
        IPath rootProjectPath = new org.eclipse.core.runtime.Path(rootProjectDir.getPath());
        IPath projectPath = project.getLocation();
        return RelativePathUtils.getRelativePath(projectPath, rootProjectPath).toPortableString();
    }

    @Override
    public void deleteProjectConfiguration(IProject project) {
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(project.isAccessible());

        try {
            PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
            preferences.delete(PREF_KEY_CONNECTION_PROJECT_DIR);
            preferences.delete(PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION);
            preferences.delete(PREF_KEY_OVERRIDE_WORKSPACE_SETTINGS);
            preferences.delete(PREF_KEY_BUILD_SCANS_ENABLED);
            preferences.delete(PREF_KEY_OFFLINE_MODE);
            preferences.flush();
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot delete project-scope preferences in project %s.", project.getName()), e);
        }
    }

}
