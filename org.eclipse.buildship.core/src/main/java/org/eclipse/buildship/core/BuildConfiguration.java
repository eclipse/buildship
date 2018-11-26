/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * Describes a configuration of a Gradle build.
 *
 * @see GradleWorkspace#createBuild(BuildConfiguration)
 * @author Donat Csikos
 * @since 3.0
 * @noimplement this interface is not intended to be implemented by clients
 */
public final class BuildConfiguration {

    // TODO (donat) review Javadoc on this class

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
        this.gradleDistribution = Preconditions.checkNotNull(builder.gradleDistribution);
        this.gradleUserHome = builder.gradleUserHome;
        this.javaHome = builder.javaHome;
        this.buildScansEnabled = builder.buildScansEnabled;
        this.offlineMode = builder.offlineMode;
        this.autoSync = builder.autoSync;
        this.arguments = builder.arguments;
        this.jvmArguments = builder.jvmArguments;
        this.showConsoleView = builder.showConsoleView;
        this.showExecutionsView = builder.showExecutionsView;
    }

    public static BuildConfigurationBuilder forRootProjectDirectory(File rootProjectDirectory) {
        return new BuildConfigurationBuilder(rootProjectDirectory);
    }

    /**
     * If set to true, the workspace configuration is ignored and the attributes defined in the
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
     * Returns the root project directory of the current build.
     *
     * @return the root project directory
     */
    public File getRootProjectDirectory() {
        return this.rootProjectDirectory;
    }

    /**
     * Returns Gradle user home for this configuration.
     * <p>
     * If {@link #isOverrideWorkspaceConfiguration()} returns true then the Gradle user home from
     * this object is returned. Otherwise, the Gradle user home for the workspace configuration is
     * returned.
     * <p>
     * If no Gradle user home is specified then this method returns {@link Optional#empty()} .
     *
     * @return the Gradle user home
     */
    public Optional<File> getGradleUserHome() {
        return Optional.ofNullable(this.gradleUserHome);
    }

    /**
     * Returns the Java home for this configuration.
     * <p>
     * If {@link #isOverrideWorkspaceConfiguration()} returns true then the Java home from this
     * object is returned. Otherwise, the java home for the workspace configuration is returned.
     * <p>
     * If no Java home is specified then this method returns {@link Optional#empty()} .
     *
     * @return the Java home
     */
    public Optional<File> getJavaHome() {
        return Optional.ofNullable(this.javaHome);
    }

    /**
     * Returns whether build scans are enabled for this configuration.
     * <p>
     * If this attribute is enabled then the Gradle task executions automatically create a build.
     * scan.
     * <p>
     * If {@link #isOverrideWorkspaceConfiguration()} returns true then value from this object is
     * returned. Otherwise, the value for the workspace configuration is returned.
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
     * <p>
     * If {@link #isOverrideWorkspaceConfiguration()} returns true then value from this object is
     * returned. Otherwise, the value for the workspace configuration is returned.
     * <p>
     *
     * @return whether offline mode is enabled
     */
    public boolean isOfflineMode() {
        return this.offlineMode;
    }

    /**
     * Returns whether auto-sync feature is enabled for this configuration.
     * <p>
     * If the auto-sync feature is enabled then the project synchronization is triggered every time
     * the content of the build script changes.
     * <p>
     * If {@link #isOverrideWorkspaceConfiguration()} returns true then value from this object is
     * returned. Otherwise, the value for the workspace configuration is returned.
     * <p>
     *
     * @return whether the auto-sync feature is enabled
     */
    public boolean isAutoSync() {
        return this.autoSync;
    }

    /**
     * Returns the Gradle distribution for this configuration.
     * <p>
     * If {@link #isOverrideWorkspaceConfiguration()} returns true then the Gradle distribution from
     * this object is returned. Otherwise, the distribution for the workspace configuration is
     * returned.
     *
     * @return the Gradle distribution
     */
    public GradleDistribution getGradleDistribution() {
        return this.gradleDistribution;
    }

    // TODO (donat) javadoc

    public List<String> getArguments() {
        return this.arguments;
    }

    public List<String> getJvmArguments() {
        return this.jvmArguments;
    }

    public boolean isShowConsoleView() {
        return this.showConsoleView;
    }

    public boolean isShowExecutionsView() {
        return this.showExecutionsView;
    }

    public static final class BuildConfigurationBuilder {

        private final File rootProjectDirectory;
        private boolean overrideWorkspaceConfiguration = false;
        private GradleDistribution gradleDistribution = GradleDistribution.fromBuild();
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
