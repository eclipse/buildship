/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
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

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.IWorkingSet;

import org.eclipse.buildship.core.GradleDistribution;

/**
 * Defines how to access a hierarchy of preferences for the Gradle projects in the workspace.
 *
 * @author Donat Csikos
 */
public interface ConfigurationManager {

    WorkspaceConfiguration loadWorkspaceConfiguration();

    void saveWorkspaceConfiguration(WorkspaceConfiguration configuration);

    BuildConfiguration createBuildConfiguration(File rootProjectDirectory, boolean overrideWorkspaceSettings,
                                                GradleDistribution gradleDistribution, File gradleUserHome,
                                                File javaHome, boolean buildScansEnabled,
                                                boolean offlineMode, boolean autoSync,
                                                List<String> arguments, List<String> jvmArguments,
                                                boolean showConsoleView, boolean showExecutionsView);

    BuildConfiguration loadBuildConfiguration(File rootProject);

    void saveBuildConfiguration(BuildConfiguration configuration);

    ProjectConfiguration createProjectConfiguration(BuildConfiguration configuration, File projectDir);

    ProjectConfiguration tryLoadProjectConfiguration(IProject project);

    ProjectConfiguration loadProjectConfiguration(IProject project);

    void saveProjectConfiguration(ProjectConfiguration configuration);

    void deleteProjectConfiguration(IProject project);

    CompositeConfiguration loadCompositeConfiguration(IWorkingSet workingSet);

    void saveCompositeConfiguration(CompositeConfiguration compConf);

    RunConfiguration loadRunConfiguration(ILaunchConfiguration launchConfiguration);

    TestRunConfiguration loadTestRunConfiguration(ILaunchConfiguration configuration);

    TestRunConfiguration loadTestRunConfiguration(BaseRunConfiguration configuration);

    RunConfiguration createDefaultRunConfiguration(BuildConfiguration configuration);
}
