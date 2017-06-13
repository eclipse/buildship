/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.configuration;

import java.io.File;
import java.util.List;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.model.build.BuildEnvironment;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingclient.GradleDistribution;

import org.eclipse.buildship.core.configuration.internal.ArgumentsCollector;

/**
 * Holds configuration values to apply on Tooling API objects.
 *
 * @author Donat Csikos
 */
public final class GradleArguments {

    private final File rootDir;
    private final GradleDistribution gradleDistribution;
    private final File gradleUserHome;
    private final File javaHome;
    private final boolean buildScansEnabled;
    private final boolean offlineMode;
    private final List<String> arguments;
    private final List<String> jvmArguments;

    private GradleArguments(File rootDir, GradleDistribution gradleDistribution, File gradleUserHome, File javaHome, boolean buildScansEnabled, boolean offlineMode, List<String> arguments, List<String> jvmArguments) {
        this.rootDir = Preconditions.checkNotNull(rootDir);
        this.gradleDistribution = Preconditions.checkNotNull(gradleDistribution);
        this.gradleUserHome = gradleUserHome;
        this.javaHome = javaHome;
        this.buildScansEnabled = buildScansEnabled;
        this.offlineMode = offlineMode;
        this.arguments = ImmutableList.copyOf(arguments);
        this.jvmArguments = ImmutableList.copyOf(jvmArguments);
    }

    public void applyTo(GradleConnector connector) {
        connector.forProjectDirectory(this.rootDir);
        connector.useGradleUserHomeDir(this.gradleUserHome);
        this.gradleDistribution.apply(connector);
    }

    public void applyTo(LongRunningOperation operation, BuildEnvironment environment) {
        operation.withArguments(ArgumentsCollector.collectArguments(this.arguments,
                 this.buildScansEnabled, this.offlineMode, environment));
        operation.setJavaHome(this.javaHome);
        operation.setJvmArguments(this.jvmArguments);
    }

    public static GradleArguments from(File rootDir, GradleDistribution gradleDistribution,
                                       File gradleUserHome, File javaHome,
                                       boolean buildScansEnabled,
                                       boolean offlineMode, List<String> arguments,
                                       List<String> jvmArguments) {
        return new GradleArguments(rootDir, gradleDistribution, gradleUserHome, javaHome, buildScansEnabled, offlineMode, arguments, jvmArguments);
    }

}