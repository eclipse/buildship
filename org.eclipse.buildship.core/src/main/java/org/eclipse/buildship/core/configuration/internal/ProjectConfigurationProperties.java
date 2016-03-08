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

import com.gradleware.tooling.toolingmodel.Path;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.util.collections.CollectionsUtils;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.core.util.file.RelativePathUtils;
import org.eclipse.buildship.core.util.gradle.GradleDistributionSerializer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import java.io.File;

/**
 * Value-holder class to transfer attributes between {@link ProjectConfiguration} and the preference storage.
 */
final class ProjectConfigurationProperties {

    private final String projectPath;
    private final String projectDir;
    private final String gradleUserHome;
    private final String gradleDistribution;
    private final String javaHome;
    private final String jvmArguments;
    private final String arguments;

    private ProjectConfigurationProperties(String projectPath, String projectDir, String gradleUserHome, String gradleDistribution,
                                           String javaHome, String jvmArguments, String arguments) {
        this.projectPath = projectPath;
        this.projectDir = projectDir;
        this.gradleUserHome = gradleUserHome;
        this.gradleDistribution = gradleDistribution;
        this.javaHome = javaHome;
        this.jvmArguments = jvmArguments;
        this.arguments = arguments;
    }

    String getProjectPath() {
        return this.projectPath;
    }

    String getProjectDir() {
        return this.projectDir;
    }

    String getGradleUserHome() {
        return this.gradleUserHome;
    }

    String getGradleDistribution() {
        return this.gradleDistribution;
    }

    String getJavaHome() {
        return this.javaHome;
    }

    String getJvmArguments() {
        return this.jvmArguments;
    }

    String getArguments() {
        return this.arguments;
    }

    static ProjectConfigurationProperties from(String projectPath, String projectDir, String gradleUserHome, String gradleDistribution,
                                               String javaHome, String jvmArguments, String arguments) {
        return new ProjectConfigurationProperties(projectPath, projectDir, gradleUserHome, gradleDistribution, javaHome, jvmArguments, arguments);
    }

    static ProjectConfigurationProperties from(IProject project, ProjectConfiguration projectConfiguration) {
        String projectPath = projectConfiguration.getProjectPath().getPath();
        String projectDir = relativePathToRootProject(project, projectConfiguration.getRequestAttributes().getProjectDir());
        String gradleUserHome = FileUtils.getAbsolutePath(projectConfiguration.getRequestAttributes().getGradleUserHome()).orNull();
        String gradleDistribution = GradleDistributionSerializer.INSTANCE.serializeToString(projectConfiguration.getRequestAttributes().getGradleDistribution());
        String javaHome = FileUtils.getAbsolutePath(projectConfiguration.getRequestAttributes().getJavaHome()).orNull();
        String jvmArguments = CollectionsUtils.joinWithSpace(projectConfiguration.getRequestAttributes().getJvmArguments());
        String arguments = CollectionsUtils.joinWithSpace(projectConfiguration.getRequestAttributes().getArguments());
        return from(projectPath, projectDir, gradleUserHome, gradleDistribution, javaHome, jvmArguments, arguments);
    }

    private static String relativePathToRootProject(IProject project, File rootProjectDir) {
        IPath rootProjectPath = new org.eclipse.core.runtime.Path(rootProjectDir.getPath());
        IPath projectPath = project.getLocation();
        return RelativePathUtils.getRelativePath(projectPath, rootProjectPath).toOSString();
    }

    ProjectConfiguration toProjectConfiguration(IProject project) {
        FixedRequestAttributes requestAttributes = new FixedRequestAttributes(
            rootProjectFile(project, getProjectDir()),
            FileUtils.getAbsoluteFile(getGradleUserHome()).orNull(),
            GradleDistributionSerializer.INSTANCE.deserializeFromString(getGradleDistribution()),
            FileUtils.getAbsoluteFile(getJavaHome()).orNull(),
            CollectionsUtils.splitBySpace(getJvmArguments()),
            CollectionsUtils.splitBySpace(getArguments())
        );
        return ProjectConfiguration.from(requestAttributes, Path.from(getProjectPath()));
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
