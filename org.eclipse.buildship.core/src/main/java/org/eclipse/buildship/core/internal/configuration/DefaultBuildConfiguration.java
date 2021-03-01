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
import java.util.List;

import com.google.common.base.Objects;

import org.eclipse.buildship.core.GradleDistribution;

/**
 * Default implementation for {@link BuildConfiguration}.
 *
 * @author Donat Csikos
 */
// TODO (donat) remove public modifier
public final class DefaultBuildConfiguration implements BuildConfiguration {

    private final BuildConfigurationProperties properties;
    private final WorkspaceConfiguration workspaceConfiguration;

    public DefaultBuildConfiguration(BuildConfigurationProperties persistentProperties, WorkspaceConfiguration workspaceConfiguration) {
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
    public File getJavaHome() {
        if (this.properties.isOverrideWorkspaceSettings()) {
            return this.properties.getJavaHome();
        } else {
            return this.workspaceConfiguration.getJavaHome();
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
    public List<String> getArguments() {
        if (this.properties.isOverrideWorkspaceSettings()) {
            return this.properties.getArguments();
        } else {
            return this.workspaceConfiguration.getArguments();
        }
    }

    @Override
    public List<String> getJvmArguments() {
        if (this.properties.isOverrideWorkspaceSettings()) {
            return this.properties.getJvmArguments();
        } else {
            return this.workspaceConfiguration.getJvmArguments();
        }
    }

    @Override
    public boolean isShowConsoleView() {
        if (this.properties.isOverrideWorkspaceSettings()) {
            return this.properties.isShowConsoleView();
        } else {
            return this.workspaceConfiguration.isShowConsoleView();
        }
    }

    @Override
    public boolean isShowExecutionsView() {
        if (this.properties.isOverrideWorkspaceSettings()) {
            return this.properties.isShowExecutionsView();
        } else {
            return this.workspaceConfiguration.isShowExecutionsView();
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

    @Override
    public BuildConfigurationProperties getProperties() {
        return this.properties;
    }

    @Override
    public GradleArguments toGradleArguments() {
        return GradleArguments.from(getRootProjectDirectory(),
            getGradleDistribution(),
            getGradleUserHome(),
            getJavaHome(),
            isBuildScansEnabled(),
            isOfflineMode(),
            getArguments(),
            getJvmArguments());
    }

    @Override
    public boolean isAutoSync() {
        if (this.properties.isOverrideWorkspaceSettings()) {
            return this.properties.isAutoSync();
        } else {
            return this.workspaceConfiguration.isAutoSync();
        }
    }

    @Override
    public org.eclipse.buildship.core.BuildConfiguration toApiBuildConfiguration() {
        // TODO (donat) the API BuildConfiguration corresponds to BuildConfigurationProperties. We should merge those.
        return org.eclipse.buildship.core.BuildConfiguration
                .forRootProjectDirectory(this.properties.getRootProjectDirectory())
                .overrideWorkspaceConfiguration(this.properties.isOverrideWorkspaceSettings())
                .gradleDistribution(this.properties.getGradleDistribution())
                .gradleUserHome(this.properties.getGradleUserHome())
                .javaHome(this.properties.getJavaHome())
                .buildScansEnabled(this.properties.isBuildScansEnabled())
                .offlineMode(this.properties.isOfflineMode())
                .autoSync(this.properties.isAutoSync())
                .arguments(this.properties.getArguments())
                .jvmArguments(this.properties.getJvmArguments())
                .showConsoleView(this.properties.isShowConsoleView())
                .showExecutionsView(this.properties.isShowExecutionsView())
                .build();
    }
}
