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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.IAdaptable;

public class DefaultCompositeConfiguration implements CompositeConfiguration {

    private final File compositeDir;
    private final IAdaptable[] projectList;
    private final BuildConfiguration buildConfiguration;
    private final Boolean projectAsCompositeRoot;
    private final File rootProject;

    public DefaultCompositeConfiguration(File compositeDir, IAdaptable[] projectList, BuildConfiguration buildConfiguration, Boolean projectAsCompositeRoot, File rootProject) {
        this.compositeDir = Preconditions.checkNotNull(compositeDir);
        this.projectList = Preconditions.checkNotNull(projectList);
        this.buildConfiguration = Preconditions.checkNotNull(buildConfiguration);
        this.projectAsCompositeRoot = Preconditions.checkNotNull(projectAsCompositeRoot);
        this.rootProject = Preconditions.checkNotNull(rootProject);
    }

    @Override
    public File getCompositeDir() {
        return this.compositeDir;
    }

    @Override
    public IAdaptable[] getProjectList() {
        return this.projectList;
    }

    @Override
    public BuildConfiguration getBuildConfiguration() {
        return this.buildConfiguration;
    }

    @Override
    public File getRootProject() {
        return this.rootProject;
    }

    @Override
    public Boolean projectAsCompositeRoot() {
        return this.projectAsCompositeRoot;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultCompositeConfiguration) {
            DefaultCompositeConfiguration other = (DefaultCompositeConfiguration) obj;
            return Objects.equal(this.compositeDir, other.compositeDir)
                    && Objects.equal(this.buildConfiguration, other.buildConfiguration);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.compositeDir,
                this.buildConfiguration);
    }

}
