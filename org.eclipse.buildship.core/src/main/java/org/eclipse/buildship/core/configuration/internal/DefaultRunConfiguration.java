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

import org.eclipse.buildship.core.configuration.GradleArguments;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.configuration.RunConfiguration;
import org.eclipse.buildship.core.util.gradle.GradleDistribution;

/**
 * Default implementation for {@link RunConfiguration}.
 */
public class DefaultRunConfiguration implements RunConfiguration {

    private final ProjectConfiguration projectConfiguration;
    private final RunConfigurationProperties properties;

    public DefaultRunConfiguration(ProjectConfiguration projectConfiguration, RunConfigurationProperties properties) {
        this.projectConfiguration = projectConfiguration;
        this.properties = properties;
    }

    @Override
    public ProjectConfiguration getProjectConfiguration() {
        return this.projectConfiguration;
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
            return this.projectConfiguration.getBuildConfiguration().getGradleDistribution();
        }
    }

    @Override
    public File getGradleUserHome() {
        if (this.properties.isOverrideBuildSettings()) {
            return this.properties.getGradleUserHome();
        } else {
            return this.projectConfiguration.getBuildConfiguration().getGradleUserHome();
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

    private boolean isBuildScansEnabled() {
        if (this.properties.isOverrideBuildSettings()) {
            return this.properties.isBuildScansEnabled();
        } else {
            return this.projectConfiguration.getBuildConfiguration().isBuildScansEnabled();
        }
    }

    private boolean isOfflineMode() {
        if (this.properties.isOverrideBuildSettings()) {
            return this.properties.isOfflineMode();
        } else {
            return this.projectConfiguration.getBuildConfiguration().isOfflineMode();
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
            return Objects.equal(this.projectConfiguration, other.projectConfiguration)
                    && Objects.equal(this.properties, other.properties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.projectConfiguration, this.properties);
    }

    @Override
    public GradleArguments toGradleArguments() {
        return GradleArguments.from(getProjectConfiguration().getProjectDir(),
            getGradleDistribution(),
            getGradleUserHome(),
            getJavaHome(),
            isBuildScansEnabled(),
            isOfflineMode(),
            getArguments(),
            getJvmArguments());
    }
}
