/*
 * Copyright (c) 2015 the original author or authors.
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
import java.util.Properties;

import org.osgi.service.prefs.BackingStoreException;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.gradleware.tooling.toolingmodel.Path;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.util.collections.CollectionsUtils;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.core.util.file.RelativePathUtils;
import org.eclipse.buildship.core.util.gradle.GradleDistributionSerializer;

/**
 * Manages reading and writing of the Gradle-specific configuration of an Eclipse project.
 */
final class ProjectConfigurationPersistence {

    private static final String PROJECT_PATH = "project_path";
    private static final String CONNECTION_PROJECT_DIR = "connection_project_dir";
    private static final String CONNECTION_GRADLE_USER_HOME = "connection_gradle_user_home";
    private static final String CONNECTION_GRADLE_DISTRIBUTION = "connection_gradle_distribution";
    private static final String CONNECTION_JAVA_HOME = "connection_java_home";
    private static final String CONNECTION_JVM_ARGUMENTS = "connection_jvm_arguments";
    private static final String CONNECTION_ARGUMENTS = "connection_arguments";

    static final String GRADLE_PROJECT_CONFIGURATION = "GRADLE_PROJECT_CONFIGURATION";
    private static final String PROJECT_PREFS_FILE =  ".settings/" + CorePlugin.PLUGIN_ID + ".prefs";

    /**
     * Saves the given Gradle project configuration in the Eclipse project's <i>.settings</i>
     * folder.
     *
     * @param projectConfiguration the Gradle configuration to persist
     * @param workspaceProject     the Eclipse project for which to persist the Gradle configuration
     */
    public void saveProjectConfiguration(ProjectConfiguration projectConfiguration, IProject workspaceProject) {
        Map<String, String> projectConfig = Maps.newLinkedHashMap();
        projectConfig.put(PROJECT_PATH, projectConfiguration.getProjectPath().getPath());
        projectConfig.put(CONNECTION_PROJECT_DIR, relativePathToRootProject(workspaceProject, projectConfiguration.getRequestAttributes().getProjectDir()));
        projectConfig.put(CONNECTION_GRADLE_USER_HOME, FileUtils.getAbsolutePath(projectConfiguration.getRequestAttributes().getGradleUserHome()).orNull());
        projectConfig.put(CONNECTION_GRADLE_DISTRIBUTION, GradleDistributionSerializer.INSTANCE.serializeToString(projectConfiguration.getRequestAttributes().getGradleDistribution()));
        projectConfig.put(CONNECTION_JAVA_HOME, FileUtils.getAbsolutePath(projectConfiguration.getRequestAttributes().getJavaHome()).orNull());
        projectConfig.put(CONNECTION_JVM_ARGUMENTS, CollectionsUtils.joinWithSpace(projectConfiguration.getRequestAttributes().getJvmArguments()));
        projectConfig.put(CONNECTION_ARGUMENTS, CollectionsUtils.joinWithSpace(projectConfiguration.getRequestAttributes().getArguments()));

        Map<String, Object> config = Maps.newLinkedHashMap();
        config.put("1.0", projectConfig);

        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        String json = gson.toJson(config, createMapTypeToken());

        try {
            ProjectScope scope = new ProjectScope(workspaceProject);
            IEclipsePreferences preferences = scope.getNode(CorePlugin.PLUGIN_ID);
            preferences.put(GRADLE_PROJECT_CONFIGURATION, json);
            preferences.flush();
        } catch (BackingStoreException e) {
            String message = String.format("Cannot persist Gradle configuration for project %s.", workspaceProject.getName());
            CorePlugin.logger().error(message, e);
            throw new GradlePluginsRuntimeException(message, e);
        }
    }

    private static String relativePathToRootProject(IProject workspaceProject, File rootProjectDir) {
        IPath rootProjectPath = new org.eclipse.core.runtime.Path(rootProjectDir.getPath());
        IPath projectPath = workspaceProject.getLocation();
        return RelativePathUtils.getRelativePath(projectPath, rootProjectPath).toOSString();
    }

    /**
     * Reads the Gradle project configuration from the Eclipse project's <i>.settings</i> folder.
     *
     * @param workspaceProject the Eclipse project from which to read the Gradle configuration
     * @return the persisted Gradle configuration
     */
    public ProjectConfiguration readProjectConfiguration(IProject workspaceProject) {
        LegacyProjectConfigurationUtils.cleanup(workspaceProject);
        String json;
        try {
            ProjectScope scope = new ProjectScope(workspaceProject);
            IEclipsePreferences preferences = scope.getNode(CorePlugin.PLUGIN_ID);
            json = preferences.get(ProjectConfigurationPersistence.GRADLE_PROJECT_CONFIGURATION, null);
            if (json == null) {
                json = readProjectConfigurationFromFile(workspaceProject);
            }
        } catch (Exception e) {
            String message = String.format("Cannot read Gradle configuration for project %s.", workspaceProject.getName());
            CorePlugin.logger().error(message, e);
            throw new GradlePluginsRuntimeException(message, e);
        }

        Gson gson = new GsonBuilder().create();
        Map<String, Object> config = gson.fromJson(json, createMapTypeToken());
        Map<String, String> projectConfig = getProjectConfigForVersion(config);

        FixedRequestAttributes requestAttributes = new FixedRequestAttributes(
                rootProjectFile(workspaceProject, projectConfig.get(CONNECTION_PROJECT_DIR)),
                FileUtils.getAbsoluteFile(projectConfig.get(CONNECTION_GRADLE_USER_HOME)).orNull(),
                GradleDistributionSerializer.INSTANCE.deserializeFromString(projectConfig.get(CONNECTION_GRADLE_DISTRIBUTION)),
                FileUtils.getAbsoluteFile(projectConfig.get(CONNECTION_JAVA_HOME)).orNull(),
                CollectionsUtils.splitBySpace(projectConfig.get(CONNECTION_JVM_ARGUMENTS)),
                CollectionsUtils.splitBySpace(projectConfig.get(CONNECTION_ARGUMENTS)));
        return ProjectConfiguration.from(requestAttributes, Path.from(projectConfig.get(PROJECT_PATH)));
    }

    private static String readProjectConfigurationFromFile(IProject workspaceProject) throws IOException {
        // when the project is being imported, the configuration file might not be visible from the
        // Eclipse resource API; in that case we fall back to raw IO operations
        // a similar approach is used in JDT core to load the .classpath file
        // see org.eclipse.jdt.internal.core.JavaProject.readFileEntriesWithException(Map)
        InputStreamReader reader = null;
        try {
            File file = new File(workspaceProject.getLocation().toFile(), PROJECT_PREFS_FILE);
            reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8);
            Properties properties = new Properties();
            properties.load(reader);
            return properties.getProperty(GRADLE_PROJECT_CONFIGURATION);
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

    private static File rootProjectFile(IProject workspaceProject, String pathToRootProject) {
        IPath path = new org.eclipse.core.runtime.Path(pathToRootProject);
        // prior to Buildship 1.0.10 the root project dir is stored as an absolute path
        return path.isAbsolute()
                ? path.toFile()
                : RelativePathUtils.getAbsolutePath(workspaceProject.getLocation(), new org.eclipse.core.runtime.Path(pathToRootProject)).toFile();
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

    public void deleteProjectConfiguration(IProject workspaceProject) {
        ProjectScope scope = new ProjectScope(workspaceProject);
        IEclipsePreferences preferences = scope.getNode(CorePlugin.PLUGIN_ID);
        preferences.remove(GRADLE_PROJECT_CONFIGURATION);
    }

}
