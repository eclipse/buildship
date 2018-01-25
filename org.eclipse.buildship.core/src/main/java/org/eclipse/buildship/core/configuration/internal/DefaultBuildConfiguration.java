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

import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.configuration.GradleArguments;
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration;
import org.eclipse.buildship.core.util.gradle.GradleDistribution;

/**
 * Default implementation for {@link BuildConfiguration}.
 *
 * @author Donat Csikos
 */
class DefaultBuildConfiguration implements BuildConfiguration {

    private final DefaultBuildConfigurationProperties properties;
    private final WorkspaceConfiguration workspaceConfiguration;

    public DefaultBuildConfiguration(DefaultBuildConfigurationProperties persistentProperties, WorkspaceConfiguration workspaceConfiguration) {
        this.properties = persistentProperties;
        this.workspaceConfiguration = workspaceConfiguration;
    }

    @Override
    public WorkspaceConfiguration getWorkspaceConfiguration() {
        return this.workspaceConfiguration;
    }

    @Override
    public File getRootProjectDirectory() {
        return this.properties.getRootProjectDirectory();
    }

    @Override
    public boolean isOverrideWorkspaceSettings() {
        return this.properties.isOverrideWorkspaceSettings();
    }

    @Override
    public File getGradleUserHome() {
        if (this.properties.isOverrideWorkspaceSettings()) {
            return this.properties.getGradleUserHome();
        } else {
            return this.workspaceConfiguration.getGradleUserHome();
        }
    }

    @Override
    public GradleDistribution getGradleDistribution() {
        if (this.properties.isOverrideWorkspaceSettings()) {
            return this.properties.getGradleDistribution();
        } else {
            return this.workspaceConfiguration.getGradleDistribution();
        }
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

    public DefaultBuildConfigurationProperties getProperties() {
        return this.properties;
    }

    @Override
    public GradleArguments toGradleArguments() {
        return GradleArguments.from(getRootProjectDirectory(),
            getGradleDistribution(),
            getGradleUserHome(),
            null, // Java home
            isBuildScansEnabled(),
            isOfflineMode(),
            Collections.<String>emptyList(), // arguments
            Collections.<String>emptyList()); // JVM arguments
    }

    @Override
    public boolean isAutoSync() {
        if (this.properties.isOverrideWorkspaceSettings()) {
            return this.properties.isAutoSync();
        } else {
            return this.workspaceConfiguration.isAutoSync();
        }
    }
}
