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

package org.eclipse.buildship.core.configuration;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.Path;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.util.configuration.FixedRequestAttributesBuilder;

/**
 * Describes the Gradle-specific configuration of an Eclipse project.
 */
public final class ProjectConfiguration {

    /**
     * Strategy that defines whether the workspace settings should be merged when a
     * ProjectConfiguration instance is converted to FixedRequestAttributes.
     */
    public enum ConversionStrategy {
        MERGE_WORKSPACE_SETTINGS, IGNORE_WORKSPACE_SETTINGS
    }

    private final Path projectPath;
    private final File rootProjectDirectory;
    private final GradleDistribution gradleDistribution;

    private ProjectConfiguration(File rootProjectDirectory, GradleDistribution gradleDistribution, Path projectPath) {
        this.rootProjectDirectory = canonicalize(rootProjectDirectory);
        this.gradleDistribution = Preconditions.checkNotNull(gradleDistribution);
        this.projectPath = Preconditions.checkNotNull(projectPath);
    }

    private static File canonicalize(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public FixedRequestAttributes toRequestAttributes(ConversionStrategy strategy) {
        FixedRequestAttributesBuilder builder;
        switch(strategy) {
            case IGNORE_WORKSPACE_SETTINGS:
                builder = FixedRequestAttributesBuilder.fromEmptySettings(this.rootProjectDirectory);
                break;
            case MERGE_WORKSPACE_SETTINGS:
                builder = FixedRequestAttributesBuilder.fromWorkspaceSettings(this.rootProjectDirectory);
                break;
            default:
                throw new GradlePluginsRuntimeException("Invalid conversion strategy: " + strategy);
        }
        return builder.gradleDistribution(this.gradleDistribution).build();
    }

    public Path getProjectPath() {
        return this.projectPath;
    }

    public File getRootProjectDirectory() {
        return this.rootProjectDirectory;
    }

    public GradleDistribution getGradleDistribution() {
        return this.gradleDistribution;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProjectConfiguration) {
            ProjectConfiguration other = (ProjectConfiguration) obj;
            return Objects.equal(this.projectPath, other.projectPath)
                    && Objects.equal(this.rootProjectDirectory, other.rootProjectDirectory)
                    && Objects.equal(this.gradleDistribution, other.gradleDistribution);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.rootProjectDirectory, this.gradleDistribution, this.projectPath);
    }

    public static ProjectConfiguration from(FixedRequestAttributes build, OmniEclipseProject project) {
        return from(build.getProjectDir(), build.getGradleDistribution(), project.getPath());
    }

    public static ProjectConfiguration from(File rootProjectDir, GradleDistribution gradleDistribution, Path projectPath) {
        return new ProjectConfiguration(rootProjectDir, gradleDistribution, projectPath);
    }

}
