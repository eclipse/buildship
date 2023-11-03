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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Default implementation for {@link ProjectConfiguration}.
 */
final class DefaultProjectConfiguration implements ProjectConfiguration {

    private final File projectDir;
    private final BuildConfiguration buildConfiguration;

    public DefaultProjectConfiguration(File projectDir, BuildConfiguration buildConfiguration) {
        this.projectDir = Preconditions.checkNotNull(projectDir);
        this.buildConfiguration = Preconditions.checkNotNull(buildConfiguration);
    }

    @Override
    public File getProjectDir() {
        return this.projectDir;
    }

    @Override
    public BuildConfiguration getBuildConfiguration() {
        return this.buildConfiguration;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultProjectConfiguration) {
            DefaultProjectConfiguration other = (DefaultProjectConfiguration) obj;
            return Objects.equal(this.projectDir, other.projectDir)
                    && Objects.equal(this.buildConfiguration, other.buildConfiguration);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.projectDir,
                this.buildConfiguration);
    }
}
