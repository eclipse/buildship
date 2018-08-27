/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.util.gradle;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.gradle.tooling.GradleConnector;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;

/**
 * Represents a Gradle distribution that can be located locally or remotely, be a fixed version, or
 * be project-specific.
 *
 * @author Etienne Studer
 */
public class GradleDistribution {

    private final GradleDistributionInfo distributionInfo;

    private GradleDistribution(GradleDistributionType type, String configuration) {
        this(new GradleDistributionInfo(type, configuration));
    }

    private GradleDistribution(GradleDistributionInfo distributionInfo) {
        Optional<String> validationError = distributionInfo.validate();
        Preconditions.checkArgument(!validationError.isPresent(), validationError.or(""));
        this.distributionInfo = distributionInfo;
    }

    public GradleDistributionInfo getDistributionInfo() {
        return this.distributionInfo;
    }

    public GradleDistributionType getType() {
        return this.distributionInfo.getType();
    }

    public String getConfiguration() {
        return this.distributionInfo.getConfiguration();
    }

    /**
     * Configures the specified connector with this distribution.
     *
     * @param connector the connector to configure
     */
    public void apply(GradleConnector connector) {
        switch (this.distributionInfo.getType()) {
            case LOCAL_INSTALLATION:
                connector.useInstallation(new File(this.distributionInfo.getConfiguration()));
                break;
            case REMOTE_DISTRIBUTION:
                connector.useDistribution(createURI(this.distributionInfo.getConfiguration()));
                break;
            case VERSION:
                connector.useGradleVersion(this.distributionInfo.getConfiguration());
                break;
            case WRAPPER:
                connector.useBuildDistribution();
                break;
            default:
                throw new GradlePluginsRuntimeException("Invalid distribution type: " + this.distributionInfo.getType());
        }
    }

    public String serializeToString() {
        return this.distributionInfo.serializeToString();
    }

    private static URI createURI(String path) {
        try {
            return new URI(path);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        GradleDistribution that = (GradleDistribution) other;
        return Objects.equal(this.distributionInfo, that.distributionInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.distributionInfo, this.distributionInfo);
    }

    @Override
    public String toString() {
        return this.distributionInfo.toString();
    }

    /**
     * Creates a reference to a local Gradle installation.
     *
     * @param installationDir the local Gradle installation directory to use
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useInstallation(java.io.File)
     */
    public static GradleDistribution forLocalInstallation(String installationDir) {
        return new GradleDistribution(GradleDistributionType.LOCAL_INSTALLATION, installationDir);
    }

    /**
     * Creates a reference to a local Gradle installation.
     *
     * @param installationDir the local Gradle installation directory to use
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useInstallation(java.io.File)
     */
    public static GradleDistribution forLocalInstallation(File installationDir) {
        return new GradleDistribution(GradleDistributionType.LOCAL_INSTALLATION, installationDir.getAbsolutePath());
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
        return new GradleDistribution(GradleDistributionType.REMOTE_DISTRIBUTION, distributionUri);
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
        return new GradleDistribution(GradleDistributionType.REMOTE_DISTRIBUTION, distributionUri.toString());
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
        return new GradleDistribution(GradleDistributionType.VERSION, version);
    }

    /**
     * Creates a reference to a project-specific version of Gradle.
     *
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useBuildDistribution()
     */
    public static GradleDistribution fromBuild() {
        return new GradleDistribution(GradleDistributionType.WRAPPER, null);
    }

    static GradleDistribution fromDistributionInfo(GradleDistributionInfo distributionInfo) {
        return new GradleDistribution(distributionInfo);
    }
}
