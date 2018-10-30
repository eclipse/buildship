/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.extension;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.buildship.core.ProjectConfigurator;

/**
 * Describes a valid or invalid contributed project configurator.
 *
 * @author Donat Csikos
 */
public final class ProjectConfiguratorContribution {

    private final IConfigurationElement extension;
    private final String contributorPluginId;
    private final String id;
    private final List<String> runsBefore;
    private final List<String> runsAfter;

    private ProjectConfigurator configurator;

    private ProjectConfiguratorContribution(IConfigurationElement extension, String id, String contributorPluginId, List<String> runsBefore, List<String> runsAfter) {
        this.extension = extension;
        this.id = id;
        this.contributorPluginId = contributorPluginId;
        this.runsBefore = runsBefore;
        this.runsAfter = runsAfter;
    }

    public ProjectConfigurator createConfigurator() throws CoreException {
        if (this.configurator == null) {
            this.configurator = ProjectConfigurator.class.cast(this.extension.createExecutableExtension("class"));
        }

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

    public List<String> getRunsBefore() {
        return this.runsBefore;
    }

    public List<String> getRunsAfter() {
        return this.runsAfter;
    }

    static ProjectConfiguratorContribution from(IConfigurationElement extension) {
        String pluginId = extension.getContributor().getName();
        String id = extension.getAttribute("id");

        Splitter splitter = Splitter.on(',').trimResults().omitEmptyStrings();
        String runsBeforeString = extension.getAttribute("runsBefore");
        List<String> runsBefore = runsBeforeString == null
                ? Collections.emptyList()
                : Lists.newArrayList(splitter.split(runsBeforeString));

        String runsAfterString = extension.getAttribute("runsAfter");
        List<String> runsAfter = runsAfterString == null
                ? Collections.emptyList()
                : Lists.newArrayList(splitter.split(runsAfterString));

        return new ProjectConfiguratorContribution(extension, id, pluginId, runsBefore, runsAfter);
    }

    public static ProjectConfiguratorContribution from(ProjectConfiguratorContribution contribuion, List<String> runsBefore, List<String> runsAfter) {
        return new ProjectConfiguratorContribution(contribuion.extension, contribuion.id, contribuion.contributorPluginId, runsBefore, runsAfter);
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
