/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.configuration.internal;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Objects;

import com.gradleware.tooling.toolingclient.GradleDistribution;

/**
 * Properties backing a {@code BuildConfiguration} instance.
 *
 * @author Donat Csikos
 */
final class BuildConfigurationProperties {

    private final File rootProjectDirectory;
    private final GradleDistribution gradleDistribution;
    private final File gradleUserHome;
    private final boolean overrideWorkspaceSettings;
    private final boolean buildScansEnabled;
    private final boolean offlineMode;

    public BuildConfigurationProperties(File rootProjectDirectory, GradleDistribution gradleDistribution, File gradleUserHome, boolean overrideWorkspaceSettings, boolean buildScansEnabled,
            boolean offlineMode) {
        this.rootProjectDirectory = canonicalize(rootProjectDirectory);
        this.gradleDistribution = gradleDistribution;
        this.gradleUserHome = gradleUserHome;
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

    public File getRootProjectDirectory() {
        return this.rootProjectDirectory;
    }

    public GradleDistribution getGradleDistribution() {
        return this.gradleDistribution;
    }

    public File getGradleUserHome() {
        return this.gradleUserHome;
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
        if (obj instanceof BuildConfigurationProperties) {
            BuildConfigurationProperties other = (BuildConfigurationProperties) obj;
            return Objects.equal(this.rootProjectDirectory, other.rootProjectDirectory)
                    && Objects.equal(this.gradleDistribution, other.gradleDistribution)
                    && Objects.equal(this.gradleUserHome, other.gradleUserHome)
                    && Objects.equal(this.overrideWorkspaceSettings, other.overrideWorkspaceSettings)
                    && Objects.equal(this.buildScansEnabled, other.buildScansEnabled)
                    && Objects.equal(this.offlineMode, other.offlineMode);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.rootProjectDirectory,
                this.gradleDistribution,
                this.gradleUserHome,
                this.overrideWorkspaceSettings,
                this.buildScansEnabled,
                this.offlineMode);
    }
}
