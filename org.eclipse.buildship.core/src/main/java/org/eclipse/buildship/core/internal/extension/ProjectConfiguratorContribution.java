/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.extension;

import com.google.common.base.Preconditions;

import org.eclipse.buildship.core.ProjectConfigurator;

public final class ProjectConfiguratorContribution {

    private final ProjectConfigurator configurator;
    private final String contributorPluginId;
    private final String id;

    private ProjectConfiguratorContribution(ProjectConfigurator configurator, String id, String contributorPluginId) {
        this.configurator = Preconditions.checkNotNull(configurator);
        this.id = Preconditions.checkNotNull(id);
        this.contributorPluginId = Preconditions.checkNotNull(contributorPluginId);
    }

    public ProjectConfigurator getConfigurator() {
        return this.configurator;
    }

    public String getId() {
        return this.id;
    }

    public String getContributorPluginId() {
        return this.contributorPluginId;
    }

    public String getFullyQualifiedId() {
        return this.contributorPluginId + "." + this.id;
    }

    static ProjectConfiguratorContribution create(ProjectConfigurator configurator, String id, String contributorPluginId) {
        return new ProjectConfiguratorContribution(configurator, id, contributorPluginId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.contributorPluginId == null) ? 0 : this.contributorPluginId.hashCode());
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProjectConfiguratorContribution other = (ProjectConfiguratorContribution) obj;
        if (this.contributorPluginId == null) {
            if (other.contributorPluginId != null) {
                return false;
            }
        } else if (!this.contributorPluginId.equals(other.contributorPluginId)) {
            return false;
        }
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
