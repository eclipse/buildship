/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.base.Objects;

import org.eclipse.buildship.core.GradleDistribution;

/**
 * Properties backing a {@code BuildConfiguration} instance.
 *
 * @author Donat Csikos
 */
public final class BuildConfigurationProperties {

    private final File rootProjectDirectory;
    private final GradleDistribution gradleDistribution;
    private final File gradleUserHome;
    private final File javaHome;
    private final boolean overrideWorkspaceSettings;
    private final boolean buildScansEnabled;
    private final boolean offlineMode;
    private final boolean autoSync;
    private final List<String> arguments;
    private final List<String> jvmArguments;
    private final boolean showConsoleView;
    private final boolean showExecutionsView;

    public BuildConfigurationProperties(File rootProjectDirectory, GradleDistribution gradleDistribution, File gradleUserHome, File javaHome, boolean overrideWorkspaceSettings, boolean buildScansEnabled,
            boolean offlineMode, boolean autoSync, List<String> arguments, List<String> jvmArguments, boolean showConsoleView, boolean showExecutionsView) {
        this.rootProjectDirectory = canonicalize(rootProjectDirectory);
        this.gradleDistribution = gradleDistribution;
        this.gradleUserHome = gradleUserHome;
        this.javaHome = javaHome;
        this.overrideWorkspaceSettings = overrideWorkspaceSettings;
        this.buildScansEnabled = buildScansEnabled;
        this.offlineMode = offlineMode;
        this.autoSync = autoSync;
        this.arguments = arguments;
        this.jvmArguments = jvmArguments;
        this.showConsoleView = showConsoleView;
        this.showExecutionsView = showExecutionsView;
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

    public File getJavaHome() {
        return this.javaHome;
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

    public boolean isAutoSync() {
        return this.autoSync;
    }

    public List<String> getArguments() {
        return this.arguments;
    }

    public List<String> getJvmArguments() {
        return this.jvmArguments;
    }

    public boolean isShowConsoleView() {
        return this.showConsoleView;
    }

    public boolean isShowExecutionsView() {
        return this.showExecutionsView;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BuildConfigurationProperties) {
            BuildConfigurationProperties other = (BuildConfigurationProperties) obj;
            return Objects.equal(this.rootProjectDirectory, other.rootProjectDirectory)
                    && Objects.equal(this.gradleDistribution, other.gradleDistribution)
                    && Objects.equal(this.gradleUserHome, other.gradleUserHome)
                    && Objects.equal(this.javaHome, other.javaHome)
                    && Objects.equal(this.overrideWorkspaceSettings, other.overrideWorkspaceSettings)
                    && Objects.equal(this.buildScansEnabled, other.buildScansEnabled)
                    && Objects.equal(this.offlineMode, other.offlineMode)
                    && Objects.equal(this.autoSync, other.autoSync)
                    && Objects.equal(this.arguments, other.arguments)
                    && Objects.equal(this.jvmArguments, other.jvmArguments)
                    && Objects.equal(this.showConsoleView, other.showConsoleView)
                    && Objects.equal(this.showExecutionsView, other.showExecutionsView);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.rootProjectDirectory,
                this.gradleDistribution,
                this.gradleUserHome,
                this.javaHome,
                this.overrideWorkspaceSettings,
                this.buildScansEnabled,
                this.offlineMode,
                this.autoSync,
                this.arguments,
                this.jvmArguments,
                this.showConsoleView,
                this.showExecutionsView);
    }
}
