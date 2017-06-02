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

import com.gradleware.tooling.toolingclient.GradleDistribution;

/**
 * Configuration to launch tasks and tests.
 *
 * @author Donat Csikos
 */
public interface RunConfiguration {

    BuildConfiguration getBuildConfiguration();

    List<String> getTasks();

    GradleDistribution getGradleDistribution();

    File getGradleUserHome();

    File getJavaHome();

    List<String> getArguments();

    List<String> getJvmArguments();

    boolean isShowExecutionView();

    boolean isShowConsoleView();

    void applyTo(GradleConnector gradleConnector);

    void applyTo(LongRunningOperation operation, BuildEnvironment environment);

    boolean isBuildScansEnabled();

    boolean isOfflineMode();
}
