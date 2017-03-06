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

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.configuration.ProjectConfiguration.ConversionStrategy;
import org.eclipse.buildship.core.util.file.RelativePathUtils;
import org.eclipse.buildship.core.util.gradle.GradleDistributionSerializer;

/**
 * Value-holder class to transfer attributes between {@link ProjectConfiguration} and the preference storage.
 */
final class ProjectConfigurationProperties {

    private final String projectDir;
    private final String gradleDistribution;
    private final String overrideWorkspaceSettings;
    private final String buildScansEnabled;
    private final String offlineMode;

    private ProjectConfigurationProperties(String projectDir, String gradleDistribution, String overrideWorkspaceSettings, String buildScansEnabled, String offlineMode) {
        this.projectDir = projectDir;
        this.gradleDistribution = gradleDistribution;
        this.overrideWorkspaceSettings = overrideWorkspaceSettings;
        this.buildScansEnabled = buildScansEnabled;
        this.offlineMode = offlineMode;
    }

    String getProjectDir() {
        return this.projectDir;
    }

    String getGradleDistribution() {
        return this.gradleDistribution;
    }

    String overrideWorkspaceSettings() {
        return this.overrideWorkspaceSettings;
    }

    String buildScansEnabled() {
        return this.buildScansEnabled;
    }

    String offlineMode() {
        return this.offlineMode;
    }

    static ProjectConfigurationProperties from(String projectDir, String gradleDistribution, String overrideWorkspaceSettings, String buildScansEnabled, String offlineMode) {
        return new ProjectConfigurationProperties(projectDir, gradleDistribution, overrideWorkspaceSettings, buildScansEnabled, offlineMode);
    }

    static ProjectConfigurationProperties from(IProject project, ProjectConfiguration projectConfiguration) {
        FixedRequestAttributes requestAttributes = projectConfiguration.toRequestAttributes(ConversionStrategy.IGNORE_WORKSPACE_SETTINGS);
        String projectDir = relativePathToRootProject(project, requestAttributes.getProjectDir());
        String gradleDistribution = GradleDistributionSerializer.INSTANCE.serializeToString(requestAttributes.getGradleDistribution());
        String overrideWorkspaceSettings = String.valueOf(projectConfiguration.isOverrideWorkspaceSettings());
        String buildScansEnabled = String.valueOf(projectConfiguration.isBuildScansEnabled());
        String offlineMode = String.valueOf(projectConfiguration.isOfflineMode());
        return from(projectDir, gradleDistribution, overrideWorkspaceSettings, buildScansEnabled, offlineMode);
    }

    private static String relativePathToRootProject(IProject project, File rootProjectDir) {
        IPath rootProjectPath = new org.eclipse.core.runtime.Path(rootProjectDir.getPath());
        IPath projectPath = project.getLocation();
        return RelativePathUtils.getRelativePath(projectPath, rootProjectPath).toPortableString();
    }

    ProjectConfiguration toProjectConfiguration(IProject project) {
        boolean overrideWorkspaceSettings = Boolean.valueOf(this.overrideWorkspaceSettings);
        boolean buildScansEnabled = Boolean.valueOf(this.buildScansEnabled);
        boolean offlineMode = Boolean.valueOf(this.offlineMode);
        return ProjectConfiguration.from(rootProjectFile(project, getProjectDir()), GradleDistributionSerializer.INSTANCE.deserializeFromString(getGradleDistribution()), overrideWorkspaceSettings, buildScansEnabled, offlineMode);
    }

    private static File rootProjectFile(IProject project, String pathToRootProject) {
        org.eclipse.core.runtime.Path rootPath = new org.eclipse.core.runtime.Path(pathToRootProject);
        if (rootPath.isAbsolute()) {
            return rootPath.toFile();
        } else {
            return RelativePathUtils.getAbsolutePath(project.getLocation(), rootPath).toFile();
        }
    }

}
