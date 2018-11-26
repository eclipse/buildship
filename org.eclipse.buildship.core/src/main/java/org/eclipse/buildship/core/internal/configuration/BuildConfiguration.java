/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.configuration;

import java.io.File;
import java.util.List;

import org.eclipse.buildship.core.GradleDistribution;

/**
 * Configuration for for a Gradle project in the workspace.
 *
 * @author Donat Csikos
 */
public interface BuildConfiguration {

    WorkspaceConfiguration getWorkspaceConfiguration();

    File getRootProjectDirectory();

    boolean isOverrideWorkspaceSettings();

    File getGradleUserHome();

    File getJavaHome();

    GradleDistribution getGradleDistribution();

    boolean isBuildScansEnabled();

    boolean isOfflineMode();

    boolean isAutoSync();

    List<String> getArguments();

    List<String> getJvmArguments();

    boolean isShowConsoleView();

    boolean isShowExecutionsView();

    GradleArguments toGradleArguments();

    org.eclipse.buildship.core.BuildConfiguration toApiBuildConfiguration();
}
