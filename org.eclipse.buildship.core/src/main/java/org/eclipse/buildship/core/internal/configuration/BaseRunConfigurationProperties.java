/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration;

import java.io.File;
import java.util.List;

import com.google.common.base.Objects;

import org.eclipse.buildship.core.GradleDistribution;

abstract class BaseRunConfigurationProperties {
    protected final GradleDistribution gradleDistribution;
    protected final File gradleUserHome;
    protected final File javaHome;
    protected final List<String> jvmArguments;
    protected final List<String> arguments;
    protected final boolean showConsoleView;
    protected final boolean showExecutionsView;
    protected final boolean overrideBuildSettings;
    protected final boolean buildScansEnabled;
    protected final boolean offlineMode;

    protected BaseRunConfigurationProperties(GradleDistribution gradleDistribution, File gradleUserHome, File javaHome, List<String> jvmArguments, List<String> arguments, boolean showConsoleView, boolean showExecutionsView, boolean overrideBuildSettings, boolean buildScansEnabled, boolean offlineMode) {
        this.gradleDistribution = gradleDistribution;
        this.gradleUserHome = gradleUserHome;
        this.javaHome = javaHome;
        this.jvmArguments = jvmArguments;
        this.arguments = arguments;
        this.showConsoleView = showConsoleView;
        this.showExecutionsView = showExecutionsView;
        this.overrideBuildSettings = overrideBuildSettings;
        this.buildScansEnabled = buildScansEnabled;
        this.offlineMode = offlineMode;
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

    public List<String> getJvmArguments() {
        return this.jvmArguments;
    }

    public List<String> getArguments() {
        return this.arguments;
    }

    public boolean isShowConsoleView() {
        return this.showConsoleView;
    }

    public boolean isShowExecutionView() {
        return this.showExecutionsView;
    }

    public boolean isOverrideBuildSettings() {
        return this.overrideBuildSettings;
    }

    public boolean isBuildScansEnabled() {
        return this.buildScansEnabled;
    }

    public boolean isOfflineMode() {
        return this.offlineMode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseRunConfigurationProperties) {
            BaseRunConfigurationProperties other = (BaseRunConfigurationProperties) obj;
            return Objects.equal(this.gradleDistribution, other.gradleDistribution)
                    && Objects.equal(this.gradleUserHome, other.gradleUserHome)
                    && Objects.equal(this.javaHome, other.javaHome)
                    && Objects.equal(this.jvmArguments, other.jvmArguments)
                    && Objects.equal(this.arguments, other.arguments)
                    && Objects.equal(this.showConsoleView, other.showConsoleView)
                    && Objects.equal(this.showExecutionsView, other.showExecutionsView)
                    && Objects.equal(this.overrideBuildSettings, other.overrideBuildSettings)
                    && Objects.equal(this.buildScansEnabled, other.buildScansEnabled)
                    && Objects.equal(this.offlineMode, other.offlineMode);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.gradleDistribution, this.gradleUserHome, this.javaHome, this.jvmArguments, this.arguments, this.showConsoleView, this.showExecutionsView, this.overrideBuildSettings, this.buildScansEnabled, this.offlineMode);
    }
}
