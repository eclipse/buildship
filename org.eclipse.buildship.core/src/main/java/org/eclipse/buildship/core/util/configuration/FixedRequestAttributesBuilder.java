/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.util.configuration;

import java.io.File;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration;

/**
 * Builder class for {@link FixedRequestAttributes}.
 *
 * @author Donat Csikos
 */
public final class FixedRequestAttributesBuilder {

    private final File projectDir;
    private File gradleUserHome = null;
    private GradleDistribution gradleDistribution = GradleDistribution.fromBuild();
    private File javaHome = null;
    private final List<String> jvmArguments = Lists.newArrayList();
    private final List<String> arguments = Lists.newArrayList();
    private boolean isOfflineMode = false;
    private boolean isBuildScansEnabled = false;

    private FixedRequestAttributesBuilder(File projectDir) {
        this.projectDir = Preconditions.checkNotNull(projectDir);
    }

    public FixedRequestAttributesBuilder gradleUserHome(File gradleUserHome) {
        this.gradleUserHome = gradleUserHome;
        return this;
    }

    public FixedRequestAttributesBuilder gradleDistribution(GradleDistribution gradleDistribution) {
        this.gradleDistribution = gradleDistribution == null ? GradleDistribution.fromBuild() : gradleDistribution;
        return this;
    }

    public FixedRequestAttributesBuilder javaHome(File javaHome) {
        this.javaHome = javaHome;
        return this;
    }

    public FixedRequestAttributesBuilder jvmArguments(List<String> jvmArguments) {
        this.jvmArguments.addAll(jvmArguments);
        return this;
    }

    public FixedRequestAttributesBuilder arguments(List<String> arguments) {
        this.arguments.addAll(arguments);
        return this;
    }

    public FixedRequestAttributesBuilder offlineMode(boolean isOfflineMode) {
        this.isOfflineMode = isOfflineMode;
        return this;
    }

    public FixedRequestAttributesBuilder buildScansEnabled(boolean isBuildScansEnabled) {
        this.isBuildScansEnabled = isBuildScansEnabled;
        return this;
    }

    public FixedRequestAttributes build() {
        List<String> jvmArgs = Lists.newArrayList(this.jvmArguments);
        List<String> args = Lists.newArrayList(this.arguments);
        if (this.isBuildScansEnabled) {
            jvmArgs.add("-Dscans");
        }
        if (this.isOfflineMode) {
            args.add("--offline");
        }
        return new FixedRequestAttributes(this.projectDir, this.gradleUserHome, this.gradleDistribution, this.javaHome, jvmArgs, args);
    }

    public static FixedRequestAttributesBuilder fromEmptySettings(File projectDir) {
        return new FixedRequestAttributesBuilder(projectDir);
    }

    public static FixedRequestAttributesBuilder fromWorkspaceSettings(File projectDir) {
        WorkspaceConfiguration configuration = CorePlugin.workspaceConfigurationManager().loadWorkspaceConfiguration();
        return fromEmptySettings(projectDir)
                .gradleUserHome(configuration.getGradleUserHome())
                .offlineMode(configuration.isOffline())
                .buildScansEnabled(configuration.isBuildScansEnabled())
                .arguments(CorePlugin.invocationCustomizer().getExtraArguments());
     }

    public static FixedRequestAttributesBuilder fromProjectSettings(IProject project) {
        WorkspaceConfiguration workspaceConfig = CorePlugin.workspaceConfigurationManager().loadWorkspaceConfiguration();
        ProjectConfiguration projectConfig = CorePlugin.projectConfigurationManager().readProjectConfiguration(project);
        return fromEmptySettings(projectConfig.getRootProjectDirectory())
                .gradleDistribution(projectConfig.getGradleDistribution())
                .gradleUserHome(workspaceConfig.getGradleUserHome())
                .offlineMode(projectConfig.isOfflineMode())
                .buildScansEnabled(projectConfig.isBuildScansEnabled())
                .arguments(CorePlugin.invocationCustomizer().getExtraArguments());
    }
}
