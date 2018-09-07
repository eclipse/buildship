/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.configuration;

import java.io.File;

import com.google.common.base.Preconditions;

import org.eclipse.buildship.core.GradleWorkspace;
import org.eclipse.buildship.core.internal.util.gradle.GradleDistribution;

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

    private boolean overrideWorkspaceConfiguration;
    private final GradleDistribution gradleDistribution;

    private BuildConfiguration(BuildConfigurationBuilder builder) {
        this.rootProjectDirectory = Preconditions.checkNotNull(builder.rootProjectDirectory);
        this.overrideWorkspaceConfiguration = builder.overrideWorkspaceConfiguration;
        this.gradleDistribution = Preconditions.checkNotNull(builder.gradleDistribution);
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

        private BuildConfigurationBuilder(File rootProjectDirectory) {
            this.rootProjectDirectory = rootProjectDirectory;
        }

        public BuildConfigurationBuilder overrideWorkspaceConfiguration(boolean overrideWorkspaceConfiguration) {
            this.overrideWorkspaceConfiguration = overrideWorkspaceConfiguration;
            return this;
        }

         public BuildConfigurationBuilder gradleDistribution(org.eclipse.buildship.core.GradleDistribution gradleDistribution) {
             this.gradleDistribution = (GradleDistribution) gradleDistribution;
             return this;
         }

        public BuildConfiguration build() {
            return new BuildConfiguration(this);
        }
    }
}
