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

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;

import org.osgi.service.prefs.BackingStoreException;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.gradleware.tooling.toolingmodel.Path;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.util.gradle.GradleDistributionSerializer;
import org.eclipse.buildship.core.util.collections.CollectionsUtils;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * Manages reading and writing of the Gradle-specific configuration of an Eclipse project.
 */
final class ProjectConfigurationPersistence {

    private static final String PROJECT_PATH = "project_path";
    private static final String PROJECT_DIR = "project_dir";
    private static final String CONNECTION_PROJECT_DIR = "connection_project_dir";
    private static final String CONNECTION_GRADLE_USER_HOME = "connection_gradle_user_home";
    private static final String CONNECTION_GRADLE_DISTRIBUTION = "connection_gradle_distribution";
    private static final String CONNECTION_JAVA_HOME = "connection_java_home";
    private static final String CONNECTION_JVM_ARGUMENTS = "connection_jvm_arguments";
    private static final String CONNECTION_ARGUMENTS = "connection_arguments";

    private static final String ECLIPSE_SETTINGS_FOLDER = ".settings";
    private static final String GRADLE_PREFERENCES_FILE = "gradle.prefs";
    private static final String GRADLE_PREFERENCES_FILE_WITHOUT_EXTENSION = GRADLE_PREFERENCES_FILE.substring(0, GRADLE_PREFERENCES_FILE.indexOf('.'));

    /**
     * Saves the given Gradle project configuration in the Eclipse project's <i>.settings</i>
     * folder.
     *
     * @param projectConfiguration the Gradle configuration to persist
     * @param workspaceProject the Eclipse project for which to persist the Gradle configuration
     */
    public void saveProjectConfiguration(ProjectConfiguration projectConfiguration, IProject workspaceProject) {
        Map<String, String> projectConfig = Maps.newLinkedHashMap();
        projectConfig.put(PROJECT_PATH, projectConfiguration.getProjectPath().getPath());
        projectConfig.put(PROJECT_DIR, projectConfiguration.getProjectDir().getAbsolutePath());
        projectConfig.put(CONNECTION_PROJECT_DIR, projectConfiguration.getRequestAttributes().getProjectDir().getAbsolutePath());
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
            IFile configFile = createConfigFile(workspaceProject);
            InputStream inputStream = new ByteArrayInputStream(json.getBytes(Charsets.UTF_8));
            if (configFile.exists()) {
                configFile.setContents(inputStream, true, false, null);
            } else {
                configFile.create(inputStream, true, null);
            }
        } catch (CoreException e) {
            String message = String.format("Cannot persist Gradle configuration for project %s.", workspaceProject.getName());
            CorePlugin.logger().error(message, e);
            throw new GradlePluginsRuntimeException(message, e);
        }
    }

    private IFile createConfigFile(IProject workspaceProject) throws CoreException {
        IFolder folder = workspaceProject.getFolder(ECLIPSE_SETTINGS_FOLDER);
        FileUtils.ensureFolderHierarchyExists(folder);
        return folder.getFile(GRADLE_PREFERENCES_FILE);
    }

    /**
     * Reads the Gradle project configuration from the Eclipse project's <i>.settings</i> folder.
     *
     * @param workspaceProject the Eclipse project from which to read the Gradle configuration
     * @return the persisted Gradle configuration
     */
    public ProjectConfiguration readProjectConfiguration(IProject workspaceProject) {
        String json;
        try {
            IFile configFile = getConfigFile(workspaceProject);
            InputStream inputStream = configFile.getContents();
            try {
                json = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        } catch (Exception e) {
            String message = String.format("Cannot read Gradle configuration for project %s.", workspaceProject.getName());
            CorePlugin.logger().error(message, e);
            throw new GradlePluginsRuntimeException(message, e);
        }

        Gson gson = new GsonBuilder().create();
        Map<String, Object> config = gson.fromJson(json, createMapTypeToken());
        Map<String, String> projectConfig = getProjectConfigForVersion(config);

        FixedRequestAttributes requestAttributes = new FixedRequestAttributes(new File(projectConfig.get(CONNECTION_PROJECT_DIR)), FileUtils.getAbsoluteFile(
                projectConfig.get(CONNECTION_GRADLE_USER_HOME)).orNull(), GradleDistributionSerializer.INSTANCE.deserializeFromString(projectConfig
                .get(CONNECTION_GRADLE_DISTRIBUTION)), FileUtils.getAbsoluteFile(projectConfig.get(CONNECTION_JAVA_HOME)).orNull(), CollectionsUtils.splitBySpace(projectConfig
                .get(CONNECTION_JVM_ARGUMENTS)), CollectionsUtils.splitBySpace(projectConfig.get(CONNECTION_ARGUMENTS)));
        return ProjectConfiguration.from(requestAttributes, Path.from(projectConfig.get(PROJECT_PATH)), new File(projectConfig.get(PROJECT_DIR)));
    }

    private IFile getConfigFile(IProject workspaceProject) throws CoreException {
        return workspaceProject.getFolder(ECLIPSE_SETTINGS_FOLDER).getFile(GRADLE_PREFERENCES_FILE);
    }

    @SuppressWarnings("serial")
    private Type createMapTypeToken() {
        return new TypeToken<Map<String, Object>>() {
        }.getType();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getProjectConfigForVersion(Map<String, Object> config) {
        return (Map<String, String>) config.get("1.0");
    }

    public void deleteProjectConfiguration(IProject workspaceProject) {
        try {
            IFile configFile = getConfigFile(workspaceProject);
            ensureNoProjectPreferencesLoadedFrom(workspaceProject, GRADLE_PREFERENCES_FILE_WITHOUT_EXTENSION);
            deleteConfigFileIfExists(configFile);
        } catch (Exception e) {
            String message = String.format("Cannot delete Gradle configuration for project %s.", workspaceProject.getName());
            CorePlugin.logger().error(message, e);
            throw new GradlePluginsRuntimeException(message, e);
        }
    }

    private static void ensureNoProjectPreferencesLoadedFrom(IProject project, String preferenceNodeName) throws BackingStoreException {
        // The ${project_name}/.settings/gradle.prefs file is automatically loaded as project
        // preferences by the core runtime since the fie extension is '.prefs'. If the preferences
        // are loaded, then deleting the prefs file results in a BackingStoreException.
        ProjectScope projectScope = new ProjectScope(project);
        IEclipsePreferences node = projectScope.getNode(preferenceNodeName);
        if (node != null) {
            node.removeNode();
        }
    }

    private static void deleteConfigFileIfExists(IFile configFile) throws CoreException {
        if (configFile.exists()) {
            configFile.delete(true, false, new NullProgressMonitor());
        }
    }

}
