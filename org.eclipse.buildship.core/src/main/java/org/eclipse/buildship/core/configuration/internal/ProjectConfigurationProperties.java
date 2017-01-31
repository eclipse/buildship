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

    private ProjectConfigurationProperties(String projectDir, String gradleDistribution) {
        this.projectDir = projectDir;
        this.gradleDistribution = gradleDistribution;
    }

    String getProjectDir() {
        return this.projectDir;
    }

    String getGradleDistribution() {
        return this.gradleDistribution;
    }

    static ProjectConfigurationProperties from(String projectDir, String gradleDistribution) {
        return new ProjectConfigurationProperties(projectDir, gradleDistribution);
    }

    static ProjectConfigurationProperties from(IProject project, ProjectConfiguration projectConfiguration) {
        FixedRequestAttributes requestAttributes = projectConfiguration.toRequestAttributes(ConversionStrategy.IGNORE_WORKSPACE_SETTINGS);
        String projectDir = relativePathToRootProject(project, requestAttributes.getProjectDir());
        String gradleDistribution = GradleDistributionSerializer.INSTANCE.serializeToString(requestAttributes.getGradleDistribution());
        return from(projectDir, gradleDistribution);
    }

    private static String relativePathToRootProject(IProject project, File rootProjectDir) {
        IPath rootProjectPath = new org.eclipse.core.runtime.Path(rootProjectDir.getPath());
        IPath projectPath = project.getLocation();
        return RelativePathUtils.getRelativePath(projectPath, rootProjectPath).toPortableString();
    }

    ProjectConfiguration toProjectConfiguration(IProject project) {
        return ProjectConfiguration.from(rootProjectFile(project, getProjectDir()), GradleDistributionSerializer.INSTANCE.deserializeFromString(getGradleDistribution()));
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
