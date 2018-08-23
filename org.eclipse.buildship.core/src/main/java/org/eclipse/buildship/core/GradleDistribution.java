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

import org.gradle.tooling.GradleConnector;

import org.eclipse.buildship.core.internal.DefaultGradleDistribution;
import org.eclipse.buildship.core.util.gradle.GradleDistributionInfo;

/**
 * Represents a Gradle distribution that can be located locally or remotely, be a fixed version, or
 * be project-specific.
 *
 * @author Etienne Studer
 */
public abstract class GradleDistribution {

    public abstract GradleDistributionInfo getDistributionInfo();

    public abstract GradleDistributionType getType();

    public abstract String getConfiguration();

    /**
     * Configures the specified connector with this distribution.
     *
     * @param connector the connector to configure
     */
    public abstract void apply(GradleConnector connector);

    public abstract String serializeToString();

    /**
     * Creates a reference to a local Gradle installation.
     *
     * @param installationDir the local Gradle installation directory to use
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useInstallation(java.io.File)
     */
    public static GradleDistribution forLocalInstallation(String installationDir) {
        return DefaultGradleDistribution.forLocalInstallation(installationDir);
    }

    /**
     * Creates a reference to a local Gradle installation.
     *
     * @param installationDir the local Gradle installation directory to use
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useInstallation(java.io.File)
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
     * @see org.gradle.tooling.GradleConnector#useDistribution(java.net.URI)
     */
    public static GradleDistribution forRemoteDistribution(String distributionUri) {
        return DefaultGradleDistribution.forRemoteDistribution(distributionUri);
    }

    /**
     * Creates a reference to a remote Gradle distribution. The appropriate distribution is
     * downloaded and installed into the user's Gradle home directory.
     *
     * @param distributionUri the remote Gradle distribution location to use
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useDistribution(java.net.URI)
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
     * @see org.gradle.tooling.GradleConnector#useGradleVersion(String)
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

    public static GradleDistribution fromDistributionInfo(GradleDistributionInfo distributionInfo) {
        return DefaultGradleDistribution.fromDistributionInfo(distributionInfo);
    }
}
