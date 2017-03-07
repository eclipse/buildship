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
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.CorePlugin;
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
        MERGE_PROJECT_SETTINGS {
            @Override
            protected FixedRequestAttributesBuilder getFixedRequestAttributesBuilder(IProject project) {
                return FixedRequestAttributesBuilder.fromProjectSettings(project);
            }
        },

        IGNORE_PROJECT_SETTINGS {
            @Override
            protected FixedRequestAttributesBuilder getFixedRequestAttributesBuilder(IProject project) {
                return FixedRequestAttributesBuilder.fromEmptySettings(project.getLocation().toFile());
            }
        };

        protected abstract FixedRequestAttributesBuilder getFixedRequestAttributesBuilder(IProject project);
    }

    private final IProject workspaceProject;
    private final File rootProjectDirectory;
    private final GradleDistribution gradleDistribution;
    private final boolean overrideWorkspaceSettings;
    private final boolean buildScansEnabled;
    private final boolean offlineMode;

    private ProjectConfiguration(IProject workspaceProject, File rootProjectDirectory, GradleDistribution gradleDistribution, boolean overrideWorkspaceSettings, boolean buildScansEnabled, boolean offlineMode) {
        this.workspaceProject = Preconditions.checkNotNull(workspaceProject);
        this.rootProjectDirectory = canonicalize(rootProjectDirectory);
        this.gradleDistribution = Preconditions.checkNotNull(gradleDistribution);
        this.overrideWorkspaceSettings = overrideWorkspaceSettings;
        this.buildScansEnabled = buildScansEnabled;
        this.offlineMode = offlineMode;
    }

    private static File canonicalize(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public FixedRequestAttributes toRequestAttributes(ConversionStrategy strategy) {
        return strategy.getFixedRequestAttributesBuilder(this.workspaceProject).gradleDistribution(this.gradleDistribution).build();
    }

    public File getRootProjectDirectory() {
        return this.rootProjectDirectory;
    }

    public GradleDistribution getGradleDistribution() {
        return this.gradleDistribution;
    }

    public boolean isOverrideWorkspaceSettings() {
        return this.overrideWorkspaceSettings;
    }

    public boolean isBuildScansEnabled() {
        return this.buildScansEnabled;
    }

    public boolean isOfflineMode() {
        return this.offlineMode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProjectConfiguration) {
            ProjectConfiguration other = (ProjectConfiguration) obj;
            return Objects.equal(this.workspaceProject, other.workspaceProject)
                    && Objects.equal(this.rootProjectDirectory, other.rootProjectDirectory)
                    && Objects.equal(this.gradleDistribution, other.gradleDistribution)
                    && Objects.equal(this.overrideWorkspaceSettings, other.overrideWorkspaceSettings)
                    && Objects.equal(this.buildScansEnabled, other.buildScansEnabled)
                    && Objects.equal(this.offlineMode, other.offlineMode);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.workspaceProject, this.rootProjectDirectory, this.gradleDistribution, this.overrideWorkspaceSettings, this.buildScansEnabled, this.offlineMode);
    }

    // TODO (donat) do we really need all these static factories?
    // TODO (donat) there are some redundant arguments below
    // TODO (donat) consolidate argument names

    public static ProjectConfiguration from(IProject workspaceProject, FixedRequestAttributes build, OmniEclipseProject project) {
        WorkspaceConfiguration wsConfig = CorePlugin.workspaceConfigurationManager().loadWorkspaceConfiguration();
        return from(workspaceProject, build, project, false, wsConfig.isBuildScansEnabled(), wsConfig.isOffline());
    }

    public static ProjectConfiguration from(IProject workspaceProject, FixedRequestAttributes build, OmniEclipseProject project, boolean overrideWorkspaceSettings, boolean buildScansEnabled, boolean offlineMode) {
        return from(workspaceProject, build.getProjectDir(), build.getGradleDistribution(), overrideWorkspaceSettings, buildScansEnabled, offlineMode);
    }

    public static ProjectConfiguration from(IProject workspaceProject, File rootProjectDir, GradleDistribution gradleDistribution, boolean overrideWorkspaceSettings, boolean buildScansEnabled, boolean offlineMode) {
        return new ProjectConfiguration(workspaceProject, rootProjectDir, gradleDistribution, overrideWorkspaceSettings, buildScansEnabled, offlineMode);
    }

}
