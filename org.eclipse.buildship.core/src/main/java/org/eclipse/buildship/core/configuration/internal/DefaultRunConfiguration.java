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
import org.eclipse.buildship.core.configuration.RunConfiguration;
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration;

/**
 * Default implementation for {@link RunConfiguration}.
 */
public class DefaultRunConfiguration extends DefaultBuildConfiguration implements RunConfiguration {

    private final RunConfigurationProperties runConfigurationProperties;

    public DefaultRunConfiguration(WorkspaceConfiguration workspaceConfiguration, BuildConfigurationProperties buildConfigurationProperties, RunConfigurationProperties runConfigurationProperties) {
        super(buildConfigurationProperties, workspaceConfiguration);
        this.runConfigurationProperties = runConfigurationProperties;
    }

    @Override
    public List<String> getTasks() {
        return this.runConfigurationProperties.getTasks();
    }

    @Override
    public File getJavaHome() {
        return this.runConfigurationProperties.getJavaHome();
    }

    @Override
    public List<String> getJvmArguments() {
        List<String> result = Lists.newArrayList(this.runConfigurationProperties.getJvmArguments());
        if (isBuildScansEnabled()) {
            result.add("-Dscan");
        }
        return result;
    }

    @Override
    public List<String> getArguments() {
        List<String> result = Lists.newArrayList(this.runConfigurationProperties.getArguments());
        if (isOfflineMode()) {
            result.add("--offline");
        }
        result.addAll(CorePlugin.invocationCustomizer().getExtraArguments());
        return result;
    }

    @Override
    public boolean isShowExecutionView() {
        return this.runConfigurationProperties.isShowExecutionView();
    }

    @Override
    public boolean isShowConsoleView() {
        return this.runConfigurationProperties.isShowConsoleView();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultRunConfiguration) {
            DefaultRunConfiguration other = (DefaultRunConfiguration) obj;
            return super.equals(other) && Objects.equal(this.runConfigurationProperties, other.runConfigurationProperties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), this.runConfigurationProperties);
    }
}
