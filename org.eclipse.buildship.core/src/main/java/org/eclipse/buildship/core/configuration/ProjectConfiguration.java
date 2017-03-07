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

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.util.configuration.FixedRequestAttributesBuilder;

/**
 * Describes the Gradle-specific configuration of an Eclipse project.
 */
public final class ProjectConfiguration {

    // TODO (donat) should reference the represented IProject in order to use FRAB.fromProjectSetttings()

    /**
     * Strategy that defines whether the workspace settings should be merged when a
     * ProjectConfiguration instance is converted to FixedRequestAttributes.
     */
    public enum ConversionStrategy {
        MERGE_WORKSPACE_SETTINGS {
            @Override
            protected FixedRequestAttributesBuilder getFixedRequestAttributesBuilder(File rootDir) {
                return FixedRequestAttributesBuilder.fromWorkspaceSettings(rootDir);
            }
        },

        IGNORE_WORKSPACE_SETTINGS {
            @Override
            protected FixedRequestAttributesBuilder getFixedRequestAttributesBuilder(File rootDir) {
                return FixedRequestAttributesBuilder.fromEmptySettings(rootDir);
            }
        };

        protected abstract FixedRequestAttributesBuilder getFixedRequestAttributesBuilder(File rootDir);
    }

    private final File rootProjectDirectory;
    private final GradleDistribution gradleDistribution;
    private final boolean overrideWorkspaceSettings;
    private final boolean buildScansEnabled;
    private final boolean offlineMode;

    private ProjectConfiguration(File rootProjectDirectory, GradleDistribution gradleDistribution, boolean overrideWorkspaceSettings, boolean buildScansEnabled, boolean offlineMode) {
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
        return strategy.getFixedRequestAttributesBuilder(this.rootProjectDirectory).gradleDistribution(this.gradleDistribution).build();
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
            return Objects.equal(this.rootProjectDirectory, other.rootProjectDirectory)
                    && Objects.equal(this.gradleDistribution, other.gradleDistribution)
                    && Objects.equal(this.overrideWorkspaceSettings, other.overrideWorkspaceSettings)
                    && Objects.equal(this.buildScansEnabled, other.buildScansEnabled)
                    && Objects.equal(this.offlineMode, other.offlineMode);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.rootProjectDirectory, this.gradleDistribution, this.overrideWorkspaceSettings, this.buildScansEnabled, this.offlineMode);
    }

    public static ProjectConfiguration from(FixedRequestAttributes build, OmniEclipseProject project) {
        WorkspaceConfiguration wsConfig = CorePlugin.workspaceConfigurationManager().loadWorkspaceConfiguration();
        return from(build, project, false, wsConfig.isBuildScansEnabled(), wsConfig.isOffline());
    }

    public static ProjectConfiguration from(FixedRequestAttributes build, OmniEclipseProject project, boolean overrideWorkspaceSettings, boolean buildScansEnabled, boolean offlineMode) {
        return from(build.getProjectDir(), build.getGradleDistribution(), overrideWorkspaceSettings, buildScansEnabled, offlineMode);
    }

    public static ProjectConfiguration from(File rootProjectDir, GradleDistribution gradleDistribution, boolean overrideWorkspaceSettings, boolean buildScansEnabled, boolean offlineMode) {
        return new ProjectConfiguration(rootProjectDir, gradleDistribution, overrideWorkspaceSettings, buildScansEnabled, offlineMode);
    }

}
