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

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration;

/**
 * Builder object for {@link FixedRequestAttributes}. The {@link #fromEmptySettings(File)} creates an empty builder object,
 * whereas the object created by {@link #fromWorkspaceSettings(File)} is preconfigured with the workspace settings.
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
        addUnique(this.jvmArguments, jvmArguments);
        return this;
    }

    public FixedRequestAttributesBuilder arguments(List<String> arguments) {
        addUnique(this.arguments, arguments);
        return this;
    }

    private static void addUnique(List<String> target, List<String> newElements) {
        for (String element : newElements) {
            if (!target.contains(element)) {
                target.add(element);
            }
        }
    }

    public FixedRequestAttributes build() {
        return new FixedRequestAttributes(this.projectDir, this.gradleUserHome, this.gradleDistribution, this.javaHome, this.jvmArguments, this.arguments);
    }

    public static FixedRequestAttributesBuilder fromEmptySettings(File projectDir) {
        return new FixedRequestAttributesBuilder(projectDir);
    }

    public static FixedRequestAttributesBuilder fromWorkspaceSettings(File projectDir) {
        FixedRequestAttributesBuilder result = fromEmptySettings(projectDir);
        WorkspaceConfiguration configuration = CorePlugin.workspaceConfigurationManager().loadWorkspaceConfiguration();
        result.gradleUserHome(configuration.getGradleUserHome());
        if (configuration.isOffline()) {
            result.arguments.add("--offline");
        }
        return result;
     }
}
