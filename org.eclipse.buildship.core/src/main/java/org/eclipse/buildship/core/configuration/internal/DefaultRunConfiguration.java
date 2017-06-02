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

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.model.build.BuildEnvironment;

import com.google.common.base.Objects;

import com.gradleware.tooling.toolingclient.GradleDistribution;

import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.configuration.RunConfiguration;
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration;

/**
 * Default implementation for {@link RunConfiguration}.
 */
public class DefaultRunConfiguration implements RunConfiguration {

    private final BuildConfiguration buildConfiguration;
    private final RunConfigurationProperties properties;

    public DefaultRunConfiguration(WorkspaceConfiguration workspaceConfiguration, DefaultBuildConfigurationProperties buildProperties, RunConfigurationProperties properties) {
        this.buildConfiguration = new DefaultBuildConfiguration(buildProperties, workspaceConfiguration);
        this.properties = properties;
    }

    @Override
    public BuildConfiguration getBuildConfiguration() {
        return this.buildConfiguration;
    }

    @Override
    public List<String> getTasks() {
        return this.properties.getTasks();
    }

    @Override
    public GradleDistribution getGradleDistribution() {
        if (this.properties.isOverrideBuildSettings()) {
            return this.properties.getGradleDistribution();
        } else {
            return this.buildConfiguration.getGradleDistribution();
        }
    }

    @Override
    public File getGradleUserHome() {
        if (this.properties.isOverrideBuildSettings()) {
            return this.properties.getGradleUserHome();
        } else {
            return this.buildConfiguration.getGradleUserHome();
        }
    }

    @Override
    public File getJavaHome() {
        return this.properties.getJavaHome();
    }

    @Override
    public List<String> getJvmArguments() {
        return this.properties.getJvmArguments();
    }

    @Override
    public List<String> getArguments() {
        return this.properties.getArguments();
    }

    @Override
    public boolean isBuildScansEnabled() {
        if (this.properties.isOverrideBuildSettings()) {
            return this.properties.isBuildScansEnabled();
        } else {
            return this.buildConfiguration.isBuildScansEnabled();
        }
    }

    @Override
    public boolean isOfflineMode() {
        if (this.properties.isOverrideBuildSettings()) {
            return this.properties.isOfflineMode();
        } else {
            return this.buildConfiguration.isOfflineMode();
        }
    }

    @Override
    public boolean isShowExecutionView() {
        return this.properties.isShowExecutionView();
    }

    @Override
    public boolean isShowConsoleView() {
        return this.properties.isShowConsoleView();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultRunConfiguration) {
            DefaultRunConfiguration other = (DefaultRunConfiguration) obj;
            return Objects.equal(this.buildConfiguration, other.buildConfiguration)
                    && Objects.equal(this.properties, other.properties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.buildConfiguration, this.properties);
    }

    @Override
    public void applyTo(GradleConnector gradleConnector) {
        gradleConnector.forProjectDirectory(getBuildConfiguration().getRootProjectDirectory()).useGradleUserHomeDir(getGradleUserHome());
        getGradleDistribution().apply(gradleConnector);
    }

    @Override
    public void applyTo(LongRunningOperation launcher, BuildEnvironment environment) {
        launcher.setJavaHome(getJavaHome());
        launcher.setJvmArguments(getJvmArguments());
        launcher.withArguments(ArgumentsCollector.collectArguments(getArguments(), isBuildScansEnabled(), isOfflineMode(), environment));

    }
}
