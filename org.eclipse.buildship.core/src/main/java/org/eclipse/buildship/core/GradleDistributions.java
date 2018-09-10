/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

import java.io.File;
import java.net.URI;

import org.eclipse.buildship.core.internal.DefaultGradleDistribution;

/**
 * Represents a Gradle distribution.
 *
 * @author Donat Csikos
 * @since 3.0
 * @noimplement this interface is not intended to be implemented by clients
 */
public abstract class GradleDistributions {

    /**
     * Creates a reference to a local Gradle installation.
     *
     * @param installationDir the local Gradle installation directory to use
     * @return a new distribution instance
     */
    public static GradleDistribution forLocalInstallation(File installationDir) {
        return DefaultGradleDistribution.forLocalInstallation(installationDir);
    }

    /**
     * Creates a reference to a remote Gradle distribution. The appropriate distribution is
     * downloaded and installed into the user's Gradle home directory.
     *
     * @param distributionUri the remote Gradle distribution location to use
     * @return a new distribution instance
     */
    public static GradleDistribution forRemoteDistribution(URI distributionUri) {
        return DefaultGradleDistribution.forRemoteDistribution(distributionUri);
    }

    /**
     * Creates a reference to a specific version of Gradle. The appropriate distribution is
     * downloaded and installed into the user's Gradle home directory.
     *
     * @param version the Gradle version to use
     * @return a new distribution instance
     */
    public static GradleDistribution forVersion(String version) {
        return DefaultGradleDistribution.forVersion(version);
    }

    /**
     * Creates a reference to a project-specific version of Gradle.
     *
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useBuildDistribution()
     */
    public static GradleDistribution fromBuild() {
        return DefaultGradleDistribution.fromBuild();
    }
}
