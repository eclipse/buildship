/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
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
 * Common implementation for run and test run configurations.
 */
public class AbstractRunConfiguration<T extends BaseRunConfigurationProperties> {

    protected final ProjectConfiguration projectConfiguration;
    protected final T properties;

    public AbstractRunConfiguration(ProjectConfiguration projectConfiguration, T properties) {
        this.projectConfiguration = projectConfiguration;
        this.properties = properties;
    }

    public ProjectConfiguration getProjectConfiguration() {
        return this.projectConfiguration;
    }

    T getProperties() {
        return this.properties;
    }

    public GradleDistribution getGradleDistribution() {
        if (this.properties.isOverrideBuildSettings()) {
            return this.properties.getGradleDistribution();
        } else {
            return this.projectConfiguration.getBuildConfiguration().getGradleDistribution();
        }
    }

    public File getGradleUserHome() {
        if (this.properties.isOverrideBuildSettings()) {
            return this.properties.getGradleUserHome();
        } else {
            return this.projectConfiguration.getBuildConfiguration().getGradleUserHome();
        }
    }

    public File getJavaHome() {
        if (this.properties.isOverrideBuildSettings()) {
            return this.properties.getJavaHome();
        } else {
            return this.projectConfiguration.getBuildConfiguration().getJavaHome();
        }
    }

    public List<String> getJvmArguments() {
        if (this.properties.isOverrideBuildSettings()) {
            return this.properties.getJvmArguments();
        } else {
            return this.projectConfiguration.getBuildConfiguration().getJvmArguments();
        }
    }

    public List<String> getArguments() {
        if (this.properties.isOverrideBuildSettings()) {
            return this.properties.getArguments();
        } else {
            return this.projectConfiguration.getBuildConfiguration().getArguments();
        }
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

    public boolean isShowExecutionView() {
        if (this.properties.isOverrideBuildSettings()) {
            return this.properties.isShowExecutionView();
        } else {
            return this.projectConfiguration.getBuildConfiguration().isShowExecutionsView();
        }
    }

    public boolean isShowConsoleView() {
        if (this.properties.isOverrideBuildSettings()) {
            return this.properties.isShowConsoleView();
        } else {
            return this.projectConfiguration.getBuildConfiguration().isShowConsoleView();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractRunConfiguration<?>) {
            AbstractRunConfiguration<?> other = (AbstractRunConfiguration<?>) obj;
            return Objects.equal(this.projectConfiguration, other.projectConfiguration)
                    && Objects.equal(this.properties, other.properties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.projectConfiguration, this.properties);
    }

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
