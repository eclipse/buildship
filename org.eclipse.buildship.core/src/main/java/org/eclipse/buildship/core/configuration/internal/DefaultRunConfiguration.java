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
import com.google.common.collect.Lists;

import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.configuration.RunConfiguration;
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration;

/**
 * Default implementation for {@link RunConfiguration}.
 */
public class DefaultRunConfiguration implements RunConfiguration {

    private final BuildConfiguration buildConfiguration;
    private final RunConfigurationProperties properties;

    public DefaultRunConfiguration(WorkspaceConfiguration workspaceConfiguration, BuildConfigurationProperties buildProperties, RunConfigurationProperties properties) {
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
        List<String> result = Lists.newArrayList(this.properties.getJvmArguments());
        if (isBuildScansEnabled()) {
            result.add("-Dscan");
        }
        return result;
    }

    private boolean isBuildScansEnabled() {
        if (this.properties.isOverrideBuildSettings()) {
            return this.properties.isBuildScansEnabled();
        } else {
            return this.buildConfiguration.isBuildScansEnabled();
        }
    }

    @Override
    public List<String> getArguments() {
        List<String> result = Lists.newArrayList(this.properties.getArguments());
        if (isOfflineMode()) {
            result.add("--offline");
        }
        result.addAll(CorePlugin.invocationCustomizer().getExtraArguments());
        return result;
    }

    private boolean isOfflineMode() {
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
    public FixedRequestAttributes toRequestAttributes() {
        return new FixedRequestAttributes(this.buildConfiguration.getRootProjectDirectory(),
                this.buildConfiguration.getWorkspaceConfiguration().getGradleUserHome(),
                getGradleDistribution(),
                getJavaHome(),
                getJvmArguments(),
                getArguments());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.buildConfiguration, this.properties);
    }
}
