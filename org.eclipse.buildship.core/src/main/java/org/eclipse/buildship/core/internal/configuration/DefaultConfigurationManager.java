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
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;

import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.CoreTraceScopes;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.launch.BaseRunConfigurationAttributes;
import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.internal.launch.GradleTestRunConfigurationAttributes;
import org.eclipse.buildship.core.internal.util.file.RelativePathUtils;

/**
 * Default implementation for {@link ConfigurationManager}.
 */
public class DefaultConfigurationManager implements ConfigurationManager {

    WorkspaceConfigurationPersistence workspaceConfigurationPersistence = new WorkspaceConfigurationPersistence();
    BuildConfigurationPersistence buildConfigurationPersistence = new BuildConfigurationPersistence();

    @Override
    public WorkspaceConfiguration loadWorkspaceConfiguration() {
        return this.workspaceConfigurationPersistence.readWorkspaceConfig();
    }

    @Override
    public void saveWorkspaceConfiguration(WorkspaceConfiguration config) {
        this.workspaceConfigurationPersistence.saveWorkspaceConfiguration(config);
    }

    @Override
    public BuildConfiguration createBuildConfiguration(File rootProjectDirectory, boolean overrideWorkspaceSettings, GradleDistribution gradleDistribution, File gradleUserHome,
            File javaHome, boolean buildScansEnabled, boolean offlineMode, boolean autoSync, List<String> arguments, List<String> jvmArguments,
            boolean showConsoleView, boolean showExecutionsView) {
        BuildConfigurationProperties persistentBuildConfigProperties = new BuildConfigurationProperties(rootProjectDirectory,
                                                                                                        gradleDistribution,
                                                                                                        gradleUserHome,
                                                                                                        javaHome,
                                                                                                        overrideWorkspaceSettings,
                                                                                                        buildScansEnabled,
                                                                                                        offlineMode,
                                                                                                        autoSync,
                                                                                                        arguments,
                                                                                                        jvmArguments,
                                                                                                        showConsoleView,
                                                                                                        showExecutionsView);
        return new DefaultBuildConfiguration(persistentBuildConfigProperties, loadWorkspaceConfiguration());
    }

    @Override
    public BuildConfiguration loadBuildConfiguration(File rootDir) {
        Preconditions.checkNotNull(rootDir);
        Preconditions.checkArgument(rootDir.exists());
        Optional<IProject> projectCandidate = CorePlugin.workspaceOperations().findProjectByLocation(rootDir);
        BuildConfigurationProperties buildConfigProperties;
        if (projectCandidate.isPresent() && projectCandidate.get().isAccessible()) {
            IProject project = projectCandidate.get();
            try {
                buildConfigProperties = this.buildConfigurationPersistence.readBuildConfiguratonProperties(project);
            } catch (Exception e) {
                // when the project is being imported, the configuration file might not be visible from the
                // Eclipse resource API; in that case we fall back to raw IO operations
                // a similar approach is used in JDT core to load the .classpath file
                // see org.eclipse.jdt.internal.core.JavaProject.readFileEntriesWithException(Map)
                buildConfigProperties = this.buildConfigurationPersistence.readBuildConfiguratonProperties(project.getLocation().toFile());
            }
        } else {
            buildConfigProperties = this.buildConfigurationPersistence.readBuildConfiguratonProperties(rootDir);
        }
        return new DefaultBuildConfiguration(buildConfigProperties, loadWorkspaceConfiguration());
    }

    @Override
    public void saveBuildConfiguration(BuildConfiguration configuration) {
        Preconditions.checkArgument(configuration instanceof DefaultBuildConfiguration, "Unknow configuration type: ", configuration.getClass());
        BuildConfigurationProperties properties = ((BuildConfiguration)configuration).getProperties();
        File rootDir = configuration.getRootProjectDirectory();
        Optional<IProject> rootProject = CorePlugin.workspaceOperations().findProjectByLocation(rootDir);
        if (rootProject.isPresent() && rootProject.get().isAccessible()) {
            this.buildConfigurationPersistence.saveBuildConfiguration(rootProject.get(), properties);
        } else {
            this.buildConfigurationPersistence.saveBuildConfiguration(rootDir, properties);
        }
    }

    @Override
    public ProjectConfiguration createProjectConfiguration(BuildConfiguration configuration, File projectDir) {
        return new DefaultProjectConfiguration(projectDir, configuration);
    }

    @Override
    public ProjectConfiguration loadProjectConfiguration(IProject project) {
        String pathToRoot = this.buildConfigurationPersistence.readPathToRoot(project.getLocation().toFile());
        File rootDir = relativePathToProjectRoot(project.getLocation(), pathToRoot);
        BuildConfiguration buildConfig = loadBuildConfiguration(rootDir);
        return new DefaultProjectConfiguration(project.getLocation().toFile(), buildConfig);
    }

    @Override
    public ProjectConfiguration tryLoadProjectConfiguration(IProject project) {
        try {
            return loadProjectConfiguration(project);
        } catch(RuntimeException e) {
            CorePlugin.logger().trace(CoreTraceScopes.PREFERENCES, "Cannot load configuration for project " + project.getName(), e);
            return null;
        }
    }

    private ProjectConfiguration loadProjectConfiguration(File projectDir) {
        String pathToRoot = this.buildConfigurationPersistence.readPathToRoot(projectDir);
        File rootDir = relativePathToProjectRoot(new Path(projectDir.getAbsolutePath()), pathToRoot);
        BuildConfiguration buildConfig = loadBuildConfiguration(rootDir);
        return new DefaultProjectConfiguration(canonicalize(projectDir), buildConfig);
    }

    @Override
    public void saveProjectConfiguration(ProjectConfiguration projectConfiguration) {
        BuildConfiguration buildConfiguration = projectConfiguration.getBuildConfiguration();
        saveBuildConfiguration(buildConfiguration);

        File projectDir = projectConfiguration.getProjectDir();
        File rootDir = buildConfiguration.getRootProjectDirectory();
        String pathToRoot = projectRootToRelativePath(projectDir, rootDir);

        Optional<IProject> project = CorePlugin.workspaceOperations().findProjectByLocation(projectDir);
        if (project.isPresent() && project.get().isAccessible()) {
            this.buildConfigurationPersistence.savePathToRoot(project.get(), pathToRoot);
        } else {
            this.buildConfigurationPersistence.savePathToRoot(projectDir, pathToRoot);
        }
    }

    @Override
    public void deleteProjectConfiguration(IProject project) {
        if (project.isAccessible()) {
            this.buildConfigurationPersistence.deletePathToRoot(project);
        } else {
            this.buildConfigurationPersistence.deletePathToRoot(project.getLocation().toFile());
        }
    }

    @Override
    public RunConfiguration loadRunConfiguration(ILaunchConfiguration launchConfiguration) {
        GradleRunConfigurationAttributes attributes = GradleRunConfigurationAttributes.from(launchConfiguration);
        ProjectConfiguration projectConfiguration = loadProjectConfiguration(attributes);
        RunConfigurationProperties runConfigProperties = new RunConfigurationProperties(attributes.getTasks(),
                  attributes.getGradleDistribution(),
                  attributes.getGradleUserHome(),
                  attributes.getJavaHome(),
                  attributes.getJvmArguments(),
                  attributes.getArguments(),
                  attributes.isShowConsoleView(),
                  attributes.isShowExecutionView(),
                  attributes.isOverrideBuildSettings(),
                  attributes.isBuildScansEnabled(),
                  attributes.isOffline());
        return new DefaultRunConfiguration(projectConfiguration, runConfigProperties);
    }

    @Override
    public TestRunConfiguration loadTestRunConfiguration(ILaunchConfiguration launchConfiguration) {
        GradleTestRunConfigurationAttributes attributes = GradleTestRunConfigurationAttributes.from(launchConfiguration);
        ProjectConfiguration projectConfiguration = loadProjectConfiguration(attributes);
        TestRunConfigurationProperties runConfigProperties = new TestRunConfigurationProperties(attributes.getGradleDistribution(),
                  attributes.getGradleUserHome(),
                  attributes.getJavaHome(),
                  attributes.getJvmArguments(),
                  attributes.getArguments(),
                  attributes.isShowConsoleView(),
                  attributes.isShowExecutionView(),
                  attributes.isOverrideBuildSettings(),
                  attributes.isBuildScansEnabled(),
                  attributes.isOffline(),
                  attributes.getTests());
        return new DefaultTestRunConfiguration(projectConfiguration, runConfigProperties);
    }

    private ProjectConfiguration loadProjectConfiguration(BaseRunConfigurationAttributes attributes) {
        ProjectConfiguration projectConfiguration;
        try {
            projectConfiguration = loadProjectConfiguration(attributes.getWorkingDir());
        } catch (Exception e) {
            CorePlugin.logger().trace(CoreTraceScopes.PREFERENCES, "Can't load build config from " + attributes.getWorkingDir(), e);
            BuildConfigurationProperties buildConfigProperties = new BuildConfigurationProperties(
                    attributes.getWorkingDir(),
                    attributes.getGradleDistribution(),
                    attributes.getGradleUserHome(),
                    attributes.getJavaHome(),
                    attributes.isOverrideBuildSettings(),
                    attributes.isBuildScansEnabled(),
                    attributes.isOffline(),
                    false,
                    attributes.getArguments(),
                    attributes.getJvmArguments(),
                    attributes.isShowConsoleView(),
                    attributes.isShowExecutionView());
            BuildConfiguration buildConfiguration = new DefaultBuildConfiguration(buildConfigProperties, loadWorkspaceConfiguration());
            projectConfiguration = new DefaultProjectConfiguration(canonicalize(attributes.getWorkingDir()), buildConfiguration);
        }
        return projectConfiguration;
    }

    @Override
    public TestRunConfiguration loadTestRunConfiguration(BaseRunConfiguration runConfig) {
        if (runConfig instanceof TestRunConfiguration) {
            return (TestRunConfiguration) runConfig;
        } else if (runConfig instanceof DefaultRunConfiguration) {
           DefaultRunConfiguration source = (DefaultRunConfiguration) runConfig;
           RunConfigurationProperties props = source.getProperties();
           TestRunConfigurationProperties properties = new TestRunConfigurationProperties(props.getGradleDistribution(),
                   props.getGradleUserHome(),
                   props.getJavaHome(),
                   props.getJvmArguments(),
                   props.getArguments(),
                   props.isShowConsoleView(),
                   props.isShowExecutionView(),
                   props.isOverrideBuildSettings(),
                   props.isBuildScansEnabled(),
                   props.isOfflineMode(),
                   Collections.emptyList());
           return new DefaultTestRunConfiguration(source.getProjectConfiguration(), properties);
        } else {
            throw new GradlePluginsRuntimeException("Unknown configuration type: " + runConfig.getClass().getSimpleName());
        }

    }

    @Override
    public RunConfiguration createDefaultRunConfiguration(BuildConfiguration configuration) {
        Preconditions.checkArgument(configuration instanceof DefaultBuildConfiguration, "Unknow configuration type: ", configuration.getClass());
        ProjectConfiguration projectConfiguration = new DefaultProjectConfiguration(configuration.getRootProjectDirectory(), configuration);
        RunConfigurationProperties runConfig = new RunConfigurationProperties(Collections.<String>emptyList(),
                GradleDistribution.fromBuild(),
                null,
                null,
                Collections.<String>emptyList(),
                Collections.<String>emptyList(),
                false,
                false,
                false,
                false,
                false);
        return new DefaultRunConfiguration(projectConfiguration, runConfig);
    }

    private static File relativePathToProjectRoot(IPath projectPath, String path) {
        IPath pathToRoot = new Path(path);
        IPath absolutePathToRoot = pathToRoot.isAbsolute() ? pathToRoot : RelativePathUtils.getAbsolutePath(projectPath, pathToRoot);
        return canonicalize(absolutePathToRoot.toFile());
    }

    private static File canonicalize(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String projectRootToRelativePath(File projectDir, File rootDir) {
        IPath rootProjectPath = new Path(rootDir.getPath());
        IPath projectPath = new Path(projectDir.getPath());
        return RelativePathUtils.getRelativePath(projectPath, rootProjectPath).toPortableString();
    }
}
