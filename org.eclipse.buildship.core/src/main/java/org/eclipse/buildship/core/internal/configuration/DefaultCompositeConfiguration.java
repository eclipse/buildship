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
import com.google.common.base.Preconditions;

public class DefaultCompositeConfiguration implements CompositeConfiguration {

    private final String compositeName;
    private final List<File> projectList;
    private final BuildConfiguration buildConfiguration;
    private final Boolean projectAsCompositeRoot;

    public DefaultCompositeConfiguration(String compositeName, List<File> projectList, BuildConfiguration buildConfiguration, Boolean projectAsCompositeRoot) {
        this.compositeName = Preconditions.checkNotNull(compositeName);
        this.projectList = Preconditions.checkNotNull(projectList);
        this.buildConfiguration = Preconditions.checkNotNull(buildConfiguration);
        this.projectAsCompositeRoot = Preconditions.checkNotNull(projectAsCompositeRoot);
    }

    @Override
    public String getCompositeName() {
        return this.compositeName;
    }

    @Override
    public List<File> getIncludedBuilds() {
        return this.projectList;
    }

    @Override
    public BuildConfiguration getBuildConfiguration() {
        return this.buildConfiguration;
    }

    @Override
    public Boolean projectAsCompositeRoot() {
        return this.projectAsCompositeRoot;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultCompositeConfiguration) {
            DefaultCompositeConfiguration other = (DefaultCompositeConfiguration) obj;
            return Objects.equal(this.compositeName, other.compositeName)
            		&& Objects.equal(this.projectList, other.projectList)
                    && Objects.equal(this.buildConfiguration, other.buildConfiguration)
                    && Objects.equal(this.projectAsCompositeRoot, other.projectAsCompositeRoot);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.compositeName,
                this.buildConfiguration);
    }

}
