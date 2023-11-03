/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * Describes a Gradle build configuration.
 *
 * @see GradleWorkspace#createBuild(BuildConfiguration)
 * @author Donat Csikos
 * @since 3.0
 * @noimplement this interface is not intended to be implemented by clients
 */
public final class BuildConfiguration {

    private final File rootProjectDirectory;

    private final boolean overrideWorkspaceConfiguration;
    private final GradleDistribution gradleDistribution;
    private final File gradleUserHome;
    private final File javaHome;
    private final boolean buildScansEnabled;
    private final boolean offlineMode;
    private final boolean autoSync;
    private final List<String> arguments;
    private final List<String> jvmArguments;
    private final boolean showConsoleView;
    private final boolean showExecutionsView;

    private BuildConfiguration(BuildConfigurationBuilder builder) {
        this.rootProjectDirectory = Preconditions.checkNotNull(builder.rootProjectDirectory);
        this.overrideWorkspaceConfiguration = builder.overrideWorkspaceConfiguration;
        this.gradleDistribution = builder.gradleDistribution == null ? GradleDistribution.fromBuild() : builder.gradleDistribution;
        this.gradleUserHome = builder.gradleUserHome;
        this.javaHome = builder.javaHome;
        this.buildScansEnabled = builder.buildScansEnabled;
        this.offlineMode = builder.offlineMode;
        this.autoSync = builder.autoSync;
        this.arguments = builder.arguments == null ? Collections.emptyList() : builder.arguments;
        this.jvmArguments = builder.jvmArguments == null ? Collections.emptyList() : builder.jvmArguments;
        this.showConsoleView = builder.showConsoleView;
        this.showExecutionsView = builder.showExecutionsView;
    }

    /**
     * Builder pattern to create new build configuration instances.
     *
     * @param rootProjectDirectory the root project directory of the Gradle build
     * @return the build configuration builder
     */
    public static BuildConfigurationBuilder forRootProjectDirectory(File rootProjectDirectory) {
        return new BuildConfigurationBuilder(rootProjectDirectory);
    }

    /**
     * If set to true, the workspace configuration is ignored and the attributes defined in this
     * build configuration will be used. If set to false, the created build configuration will
     * inherit all attributes from the current workspace configuration and the build configuration
     * attributes will be ignored.
     *
     * @return whether this configuration overrides the workspace configuration
     */
    public boolean isOverrideWorkspaceConfiguration() {
        return this.overrideWorkspaceConfiguration;
    }

    /**
     * @return the root project directory of the current build
     */
    public File getRootProjectDirectory() {
        return this.rootProjectDirectory;
    }

    /**
     * @return the Gradle user home or {@link Optional#empty()} if not specified.
     */
    public Optional<File> getGradleUserHome() {
        return Optional.ofNullable(this.gradleUserHome);
    }

    /**
     * @return the Java home or {@link Optional#empty()} if not specified.
     */
    public Optional<File> getJavaHome() {
        return Optional.ofNullable(this.javaHome);
    }

    /**
     * Returns whether build scans are enabled for this configuration.
     *
     * <p>
     * If this attribute is enabled then the Gradle task executions automatically create a build
     * scan.
     *
     * @return whether build scans are enabled
     */
    public boolean isBuildScansEnabled() {
        return this.buildScansEnabled;
    }

    /**
     * Returns whether the offline mode is enabled for this configuration.
     * <p>
     * If the offline mode is enabled then all project synchronizations and the task executions are
     * executed in offline mode (e.g. the {@code --offline} parameter is added to all invocations).
     *
     * @return whether offline mode is enabled
     */
    public boolean isOfflineMode() {
        return this.offlineMode;
    }

    /**
     * Returns whether auto-sync feature is enabled for this configuration.
     *
     * <p>
     * If the auto-sync feature is enabled then the project synchronization is triggered every time
     * the content of the build script changes.
     *
     * @return whether the auto-sync feature is enabled
     */
    public boolean isAutoSync() {
        return this.autoSync;
    }

    /**
     * @return the Gradle distribution
     */
    public GradleDistribution getGradleDistribution() {
        return this.gradleDistribution;
    }

    /**
     *
     * @return the list of program arguments
     */
    public List<String> getArguments() {
        return this.arguments;
    }

    /**
     * @return the list of JVM arguments
     */
    public List<String> getJvmArguments() {
        return this.jvmArguments;
    }

    /**
     * @return {@code true} if the Console view should be visible at the beginning of each build
     */
    public boolean isShowConsoleView() {
        return this.showConsoleView;
    }

    /**
     * @return {@code true} if the Executions view should be visible at the beginning of each build
     */
    public boolean isShowExecutionsView() {
        return this.showExecutionsView;
    }

    public static final class BuildConfigurationBuilder {

        private final File rootProjectDirectory;
        private boolean overrideWorkspaceConfiguration = false;
        private GradleDistribution gradleDistribution;
        private File gradleUserHome = null;
        private File javaHome = null;
        private boolean buildScansEnabled = false;
        private boolean offlineMode = false;
        private boolean autoSync = false;
        private List<String> arguments = new ArrayList<>();
        private List<String> jvmArguments = new ArrayList<>();
        private boolean showConsoleView = true;
        private boolean showExecutionsView = true;

        private BuildConfigurationBuilder(File rootProjectDirectory) {
            this.rootProjectDirectory = rootProjectDirectory;
        }

        public BuildConfigurationBuilder overrideWorkspaceConfiguration(boolean overrideWorkspaceConfiguration) {
            this.overrideWorkspaceConfiguration = overrideWorkspaceConfiguration;
            return this;
        }

        public BuildConfigurationBuilder gradleDistribution(GradleDistribution gradleDistribution) {
            this.gradleDistribution = gradleDistribution;
            return this;
        }

        public BuildConfigurationBuilder gradleUserHome(File gradleUserHome) {
            this.gradleUserHome = gradleUserHome;
            return this;
        }

        public BuildConfigurationBuilder javaHome(File javaHome) {
            this.javaHome = javaHome;
            return this;
        }

        public BuildConfigurationBuilder buildScansEnabled(boolean buildScansEnabled) {
            this.buildScansEnabled = buildScansEnabled;
            return this;
        }

        public BuildConfigurationBuilder offlineMode(boolean offlineMode) {
            this.offlineMode = offlineMode;
            return this;
        }

        public BuildConfigurationBuilder autoSync(boolean autoSync) {
            this.autoSync = autoSync;
            return this;
        }

        public BuildConfigurationBuilder arguments(List<String> arguments) {
            this.arguments = arguments;
            return this;
        }

        public BuildConfigurationBuilder jvmArguments(List<String> jvmArguments) {
            this.jvmArguments = jvmArguments;
            return this;
        }

        public BuildConfigurationBuilder showConsoleView(boolean showConsoleView) {
            this.showConsoleView = showConsoleView;
            return this;
        }

        public BuildConfigurationBuilder showExecutionsView(boolean showExecutionsView) {
            this.showExecutionsView = showExecutionsView;
            return this;
        }

        public BuildConfiguration build() {
            return new BuildConfiguration(this);
        }
    }
}
