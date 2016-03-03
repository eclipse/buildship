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
import java.util.Map;

import com.google.common.collect.Maps;

import com.gradleware.tooling.toolingmodel.Path;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

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

    /**
     * Saves the given Gradle project configuration in the Eclipse project's <i>.settings</i>
     * folder.
     *
     * @param projectConfiguration the Gradle configuration to persist
     * @param workspaceProject     the Eclipse project for which to persist the Gradle configuration
     */
    public void saveProjectConfiguration(ProjectConfiguration projectConfiguration, IProject workspaceProject) {
        LegacyProjectConfigurationUtils.cleanup(workspaceProject);

        Map<ProjectConfigurationProperties, String> projectConfig = Maps.newLinkedHashMap();
        projectConfig.put(ProjectConfigurationProperties.PROJECT_PATH, projectConfiguration.getProjectPath().getPath());
        projectConfig.put(ProjectConfigurationProperties.CONNECTION_PROJECT_DIR, relativePathToRootProject(workspaceProject, projectConfiguration.getRequestAttributes().getProjectDir()));
        projectConfig.put(ProjectConfigurationProperties.CONNECTION_GRADLE_USER_HOME, FileUtils.getAbsolutePath(projectConfiguration.getRequestAttributes().getGradleUserHome()).orNull());
        projectConfig.put(ProjectConfigurationProperties.CONNECTION_GRADLE_DISTRIBUTION, GradleDistributionSerializer.INSTANCE.serializeToString(projectConfiguration.getRequestAttributes().getGradleDistribution()));
        projectConfig.put(ProjectConfigurationProperties.CONNECTION_JAVA_HOME, FileUtils.getAbsolutePath(projectConfiguration.getRequestAttributes().getJavaHome()).orNull());
        projectConfig.put(ProjectConfigurationProperties.CONNECTION_JVM_ARGUMENTS, CollectionsUtils.joinWithSpace(projectConfiguration.getRequestAttributes().getJvmArguments()));
        projectConfig.put(ProjectConfigurationProperties.CONNECTION_ARGUMENTS, CollectionsUtils.joinWithSpace(projectConfiguration.getRequestAttributes().getArguments()));

        try {
            ProjectConfigurationProperties.storeAll(workspaceProject, projectConfig);
        } catch (Exception e) {
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
        Map<ProjectConfigurationProperties, String> projectConfig;
        if (LegacyProjectConfigurationUtils.hasLegacyConfiguration(workspaceProject)) {
            projectConfig = LegacyProjectConfigurationUtils.readLegacyConfiguration(workspaceProject);
        } else {
            projectConfig = ProjectConfigurationProperties.loadAll(workspaceProject);
        }

        FixedRequestAttributes requestAttributes = new FixedRequestAttributes(
                rootProjectFile(workspaceProject, projectConfig.get(ProjectConfigurationProperties.CONNECTION_PROJECT_DIR)),
                FileUtils.getAbsoluteFile(projectConfig.get(ProjectConfigurationProperties.CONNECTION_GRADLE_USER_HOME)).orNull(),
                GradleDistributionSerializer.INSTANCE.deserializeFromString(projectConfig.get(ProjectConfigurationProperties.CONNECTION_GRADLE_DISTRIBUTION)),
                FileUtils.getAbsoluteFile(projectConfig.get(ProjectConfigurationProperties.CONNECTION_JAVA_HOME)).orNull(),
                CollectionsUtils.splitBySpace(projectConfig.get(ProjectConfigurationProperties.CONNECTION_JVM_ARGUMENTS)),
                CollectionsUtils.splitBySpace(projectConfig.get(ProjectConfigurationProperties.CONNECTION_ARGUMENTS)));
        return ProjectConfiguration.from(requestAttributes, Path.from(projectConfig.get(ProjectConfigurationProperties.PROJECT_PATH)));
    }


    private static File rootProjectFile(IProject workspaceProject, String pathToRootProject) {
        return RelativePathUtils.getAbsolutePath(workspaceProject.getLocation(), new org.eclipse.core.runtime.Path(pathToRootProject)).toFile();
    }

    public void deleteProjectConfiguration(IProject workspaceProject) {
        ProjectConfigurationProperties.deleteAll(workspaceProject);
    }

}
