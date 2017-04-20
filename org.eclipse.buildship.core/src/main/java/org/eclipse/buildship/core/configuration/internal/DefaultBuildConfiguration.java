/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.configuration.internal;

import java.io.File;
import java.util.Collections;

import com.google.common.base.Objects;

import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration;

/**
 * Default implementation for {@link BuildConfiguration}.
 *
 * @author Donat Csikos
 */
class DefaultBuildConfiguration implements BuildConfiguration {

    private final BuildConfigurationProperties properties;
    private final WorkspaceConfiguration workspaceConfiguration;

    public DefaultBuildConfiguration(BuildConfigurationProperties persistentProperties, WorkspaceConfiguration workspaceConfig) {
        this.properties = persistentProperties;
        this.workspaceConfiguration = workspaceConfig;
    }

    @Override
    public File getRootProjectDirectory() {
        return this.properties.getRootProjectDirectory();
    }

    @Override
    public GradleDistribution getGradleDistribution() {
        return this.properties.getGradleDistribution();
    }

    @Override
    public boolean isOverrideWorkspaceSettings() {
        return this.properties.isOverrideWorkspaceSettings();
    }

    @Override
    public boolean isBuildScansEnabled() {
        if (this.properties.isOverrideWorkspaceSettings()) {
            return this.properties.isBuildScansEnabled();
        } else {
            return this.workspaceConfiguration.isBuildScansEnabled();
        }
    }

    @Override
    public boolean isOfflineMode() {
        if (this.properties.isOverrideWorkspaceSettings()) {
            return this.properties.isOfflineMode();
        } else {
            return this.workspaceConfiguration.isOffline();
        }
    }

    @Override
    public FixedRequestAttributes toRequestAttributes() {
        return new FixedRequestAttributes(getRootProjectDirectory(), this.workspaceConfiguration.getGradleUserHome(), getGradleDistribution(), null, Collections.<String>emptyList(), Collections.<String>emptyList());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultBuildConfiguration) {
            DefaultBuildConfiguration other = (DefaultBuildConfiguration) obj;
            return Objects.equal(this.properties, other.properties)
                    && Objects.equal(this.workspaceConfiguration, other.workspaceConfiguration);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.properties,
                this.workspaceConfiguration);
    }

    public BuildConfigurationProperties getProperties() {
        return this.properties;
    }
}
