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

import com.gradleware.tooling.toolingmodel.Path;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.util.file.RelativePathUtils;
import org.eclipse.buildship.core.util.gradle.GradleDistributionSerializer;

/**
 * Value-holder class to transfer attributes between {@link ProjectConfiguration} and the preference storage.
 */
final class ProjectConfigurationProperties {

    private final String projectPath;
    private final String projectDir;
    private final String gradleDistribution;

    private ProjectConfigurationProperties(String projectPath, String projectDir, String gradleDistribution) {
        this.projectPath = projectPath;
        this.projectDir = projectDir;
        this.gradleDistribution = gradleDistribution;
    }

    String getProjectPath() {
        return this.projectPath;
    }

    String getProjectDir() {
        return this.projectDir;
    }

    String getGradleDistribution() {
        return this.gradleDistribution;
    }

    static ProjectConfigurationProperties from(String projectPath, String projectDir, String gradleDistribution) {
        return new ProjectConfigurationProperties(projectPath, projectDir, gradleDistribution);
    }

    static ProjectConfigurationProperties from(IProject project, ProjectConfiguration projectConfiguration) {
        String projectPath = projectConfiguration.getProjectPath().getPath();
        String projectDir = relativePathToRootProject(project, projectConfiguration.toRequestAttributes().getProjectDir());
        String gradleDistribution = GradleDistributionSerializer.INSTANCE.serializeToString(projectConfiguration.toRequestAttributes().getGradleDistribution());
        return from(projectPath, projectDir, gradleDistribution);
    }

    private static String relativePathToRootProject(IProject project, File rootProjectDir) {
        IPath rootProjectPath = new org.eclipse.core.runtime.Path(rootProjectDir.getPath());
        IPath projectPath = project.getLocation();
        return RelativePathUtils.getRelativePath(projectPath, rootProjectPath).toOSString();
    }

    ProjectConfiguration toProjectConfiguration(IProject project) {
        return ProjectConfiguration.from(rootProjectFile(project, getProjectDir()), GradleDistributionSerializer.INSTANCE.deserializeFromString(getGradleDistribution()), Path.from(getProjectPath()));
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
