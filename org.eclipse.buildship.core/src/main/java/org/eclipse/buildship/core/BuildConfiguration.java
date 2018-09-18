/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

import java.io.File;
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

    private final File rootProjectDirectory;

    private final boolean overrideWorkspaceConfiguration;
    private final GradleDistribution gradleDistribution;
    private final File gradleUserHome;
    private final boolean buildScansEnabled;
    private final boolean offlineMode;
    private final boolean autoSync;

    private BuildConfiguration(BuildConfigurationBuilder builder) {
        this.rootProjectDirectory = Preconditions.checkNotNull(builder.rootProjectDirectory);
        this.overrideWorkspaceConfiguration = builder.overrideWorkspaceConfiguration;
        this.gradleDistribution = Preconditions.checkNotNull(builder.gradleDistribution);
        this.gradleUserHome = builder.gradleUserHome;
        this.buildScansEnabled = builder.buildScansEnabled;
        this.offlineMode = builder.offlineMode;
        this.autoSync = builder.autoSync;
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

    public static final class BuildConfigurationBuilder {

        private final File rootProjectDirectory;
        private boolean overrideWorkspaceConfiguration = false;
        private GradleDistribution gradleDistribution = GradleDistribution.fromBuild();
        private File gradleUserHome = null;
        private boolean buildScansEnabled = false;
        private boolean offlineMode = false;
        private boolean autoSync = false;

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

        public BuildConfiguration build() {
            return new BuildConfiguration(this);
        }
    }
}
