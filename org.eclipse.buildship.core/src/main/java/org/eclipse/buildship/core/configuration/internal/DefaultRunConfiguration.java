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

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.configuration.RunConfiguration;
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration;

/**
 * Default implementation for {@link RunConfiguration}.
 */
public class DefaultRunConfiguration implements RunConfiguration {

    private final BuildConfiguration buildConfiguration;
    private final RunConfigurationProperties runProperties;

    public DefaultRunConfiguration(WorkspaceConfiguration workspaceConfiguration, BuildConfigurationProperties buildProperties, RunConfigurationProperties runProperties) {
        this.buildConfiguration = new DefaultBuildConfiguration(buildProperties, workspaceConfiguration);
        this.runProperties = runProperties;
    }

    @Override
    public BuildConfiguration getBuildConfiguration() {
        return this.buildConfiguration;
    }

    @Override
    public List<String> getTasks() {
        return this.runProperties.getTasks();
    }

    @Override
    public File getJavaHome() {
        return this.runProperties.getJavaHome();
    }

    @Override
    public List<String> getJvmArguments() {
        List<String> result = Lists.newArrayList(this.runProperties.getJvmArguments());
        if (this.buildConfiguration.isBuildScansEnabled()) {
            result.add("-Dscan");
        }
        return result;
    }

    @Override
    public List<String> getArguments() {
        List<String> result = Lists.newArrayList(this.runProperties.getArguments());
        if (this.buildConfiguration.isOfflineMode()) {
            result.add("--offline");
        }
        result.addAll(CorePlugin.invocationCustomizer().getExtraArguments());
        return result;
    }

    @Override
    public boolean isShowExecutionView() {
        return this.runProperties.isShowExecutionView();
    }

    @Override
    public boolean isShowConsoleView() {
        return this.runProperties.isShowConsoleView();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultRunConfiguration) {
            DefaultRunConfiguration other = (DefaultRunConfiguration) obj;
            return Objects.equal(this.buildConfiguration, other.buildConfiguration)
                    && Objects.equal(this.runProperties, other.runProperties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.buildConfiguration, this.runProperties);
    }
}
