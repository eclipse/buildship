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

/**
 * Describes a configuration of a Gradle build.
 *
 * @see GradleWorkspace#createBuild(BuildConfiguration)
 * @author Donat Csikos
 * @since 3.0
 */
public final class BuildConfiguration {

    private final File rootProjectDirectory;

    private BuildConfiguration(BuildConfigurationBuilder builder) {
        this.rootProjectDirectory = builder.rootProjectDirectory;
    }

    public static BuildConfigurationBuilder forRootProjectDirectory(File rootProjectDirectory) {
        return new BuildConfigurationBuilder(rootProjectDirectory);
    }

    public File getRootProjectDirectory() {
        return this.rootProjectDirectory;
    }

    public static final class BuildConfigurationBuilder {

        private final File rootProjectDirectory;

        private BuildConfigurationBuilder(File rootProjectDirectory) {
            this.rootProjectDirectory = Preconditions.checkNotNull(rootProjectDirectory);
        }

        public BuildConfiguration build() {
            return new BuildConfiguration(this);
        }
    }
}
