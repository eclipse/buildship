/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.configuration.impl;

import java.io.File;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.configuration.ProjectConfiguration;

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
