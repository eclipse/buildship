/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.extension;

import org.eclipse.buildship.core.ProjectConfigurator;

public final class ProjectConfiguratorContribution {

    private final ProjectConfigurator configurator;
    private final String contributorPluginId;

    private ProjectConfiguratorContribution(ProjectConfigurator configurator, String contributorPluginId) {
        this.configurator = configurator;
        this.contributorPluginId = contributorPluginId;
    }

    public ProjectConfigurator getConfigurator() {
        return this.configurator;
    }

    public String getContributorPluginId() {
        return this.contributorPluginId;
    }

    static ProjectConfiguratorContribution create(ProjectConfigurator configurator, String contributorPluginId) {
        return new ProjectConfiguratorContribution(configurator, contributorPluginId);
    }
}
