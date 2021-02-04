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

    BuildConfigurationProperties getProperties();
}
