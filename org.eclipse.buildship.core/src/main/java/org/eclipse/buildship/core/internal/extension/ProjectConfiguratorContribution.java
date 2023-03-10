/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
    public String toString() {
        return "ProjectConfiguratorContribution [id=" + getId() + ", runsBefore=" + this.runsBefore
                + ", runsAfter=" + this.runsAfter + "]";
    }
}
