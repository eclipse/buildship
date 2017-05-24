/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.configuration.internal;

import java.io.File;
import java.util.List;

import com.google.common.base.Objects;

import com.gradleware.tooling.toolingclient.GradleDistribution;

/**
 * Properties backing a {@code RunConfiguration} instance.
 *
 * @author Donat Csikos
 */
final class RunConfigurationProperties {

    private final List<String> tasks;
    private final GradleDistribution gradleDistribution;
    private final File gradleUserHome;
    private final File javaHome;
    private final List<String> jvmArguments;
    private final List<String> arguments;
    private final boolean showConsoleView;
    private final boolean showExecutionsView;
    private final boolean overrideBuildSettings;
    private final boolean buildScansEnabled;
    private final boolean offlineMode;

    public RunConfigurationProperties(List<String> tasks, GradleDistribution gradleDistribution, File gradleUserHome, File javaHome, List<String> jvmArguments, List<String> arguments, boolean showConsoleView, boolean showExecutionsView, boolean overrideBuildSettings, boolean buildScansEnabled, boolean offlineMode) {
        this.tasks = tasks;
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

    public List<String> getTasks() {
        return this.tasks;
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
        if (obj instanceof RunConfigurationProperties) {
            RunConfigurationProperties other = (RunConfigurationProperties) obj;
            return Objects.equal(this.tasks, other.tasks)
                    && Objects.equal(this.gradleDistribution, other.gradleDistribution)
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
        return Objects.hashCode(this.tasks, this.gradleDistribution, this.gradleUserHome, this.javaHome, this.jvmArguments, this.arguments, this.showConsoleView, this.showExecutionsView, this.overrideBuildSettings, this.buildScansEnabled, this.offlineMode);
    }
}