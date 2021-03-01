/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration;

import java.io.File;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;

import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;

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
    private static final String PREF_KEY_JAVA_HOME = "java.home";
    private static final String PREF_KEY_BUILD_SCANS_ENABLED = "build.scans.enabled";
    private static final String PREF_KEY_OFFLINE_MODE = "offline.mode";
    private static final String PREF_KEY_AUTO_SYNC = "auto.sync";
    private static final String PREF_KEY_ARGUMENTS = "arguments";
    private static final String PREF_KEY_JVM_ARGUMENTS = "jvm.arguments";
    private static final String PREF_KEY_SHOW_CONSOLE_VIEW = "show.console.view";
    private static final String PREF_KEY_SHOW_EXECUTIONS_VIEW = "show.executions.view";

    public BuildConfigurationProperties readBuildConfiguratonProperties(IProject project) {
        Preconditions.checkNotNull(project);
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
        return readPreferences(preferences, project.getLocation().toFile());
    }

    public BuildConfigurationProperties readBuildConfiguratonProperties(File projectDir) {
        Preconditions.checkNotNull(projectDir);
        PreferenceStore preferences = PreferenceStore.forPreferenceFile(getProjectPrefsFile(projectDir, PREF_NODE));
        return readPreferences(preferences, projectDir);
    }

    public void saveBuildConfiguration(IProject project, BuildConfigurationProperties properties) {
        Preconditions.checkNotNull(project);
        Preconditions.checkNotNull(properties);
        PreferenceStore preferences = PreferenceStore.forProjectScope(project, PREF_NODE);
        savePreferences(properties, preferences);
    }

    public void saveBuildConfiguration(File projectDir, BuildConfigurationProperties properties) {
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

    private static BuildConfigurationProperties readPreferences(PreferenceStore preferences, File rootDir) {
        boolean overrideWorkspaceSettings = preferences.readBoolean(PREF_KEY_OVERRIDE_WORKSPACE_SETTINGS, false);

        String distributionString = preferences.readString(PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, null);
        GradleDistribution distribution;
        try {
            distribution = GradleDistribution.fromString(distributionString);
        } catch (RuntimeException ignore) {
            distribution = GradleDistribution.fromBuild();
        }

        String gradleUserHomeString = preferences.readString(PREF_KEY_GRADLE_USER_HOME, "");
        File gradleUserHome = gradleUserHomeString.isEmpty()
                ? null
                : new File(gradleUserHomeString);
        String javaHomeString = preferences.readString(PREF_KEY_JAVA_HOME, "");
        File javaHome = javaHomeString.isEmpty()
                ? null
                : new File(javaHomeString);

        boolean buildScansEnabled = preferences.readBoolean(PREF_KEY_BUILD_SCANS_ENABLED, false);
        boolean offlineMode = preferences.readBoolean(PREF_KEY_OFFLINE_MODE, false);
        boolean autoSync = preferences.readBoolean(PREF_KEY_AUTO_SYNC, false);
        List<String> arguments = Lists.newArrayList(Splitter.on(' ').omitEmptyStrings().split(preferences.readString(PREF_KEY_ARGUMENTS, "")));
        List<String> jvmArguments = Lists.newArrayList(Splitter.on(' ').omitEmptyStrings().split(preferences.readString(PREF_KEY_JVM_ARGUMENTS, "")));
        boolean showConsoleView = preferences.readBoolean(PREF_KEY_SHOW_CONSOLE_VIEW, false);
        boolean showExecutionsView = preferences.readBoolean(PREF_KEY_SHOW_EXECUTIONS_VIEW, false);

        return new BuildConfigurationProperties(rootDir, distribution, gradleUserHome, javaHome, overrideWorkspaceSettings, buildScansEnabled, offlineMode, autoSync, arguments, jvmArguments, showConsoleView, showExecutionsView);
    }

    private static void savePreferences(BuildConfigurationProperties properties, PreferenceStore preferences) {
        GradleDistribution gradleDistribution = properties.getGradleDistribution();
        String gradleDistributionString = gradleDistribution == null ? GradleDistribution.fromBuild().toString() : gradleDistribution.toString();
        preferences.write(PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, gradleDistributionString);
        preferences.write(PREF_KEY_GRADLE_USER_HOME, toPortableString(properties.getGradleUserHome()));
        preferences.write(PREF_KEY_JAVA_HOME, toPortableString(properties.getJavaHome()));
        preferences.writeBoolean(PREF_KEY_OVERRIDE_WORKSPACE_SETTINGS, properties.isOverrideWorkspaceSettings());
        preferences.writeBoolean(PREF_KEY_BUILD_SCANS_ENABLED, properties.isBuildScansEnabled());
        preferences.writeBoolean(PREF_KEY_OFFLINE_MODE, properties.isOfflineMode());
        preferences.writeBoolean(PREF_KEY_AUTO_SYNC, properties.isAutoSync());
        List<String> arguments = properties.getArguments();
        preferences.write(PREF_KEY_ARGUMENTS, arguments == null ? "" : Joiner.on(' ').join(arguments));
        List<String> jvmArguments = properties.getJvmArguments();
        preferences.write(PREF_KEY_JVM_ARGUMENTS, jvmArguments == null ? "" : Joiner.on(' ').join(jvmArguments));
        preferences.writeBoolean(PREF_KEY_SHOW_CONSOLE_VIEW, properties.isShowConsoleView());
        preferences.writeBoolean(PREF_KEY_SHOW_EXECUTIONS_VIEW, properties.isShowExecutionsView());
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
