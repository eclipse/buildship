/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.util.gradle;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.gradle.tooling.GradleConnector;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a Gradle distribution that can be located locally or remotely, be a fixed version, or be project-specific.
 *
 * @author Etienne Studer
 */
public final class GradleDistribution {

    private final File localInstallationDir;
    private final URI remoteDistributionUri;
    private final String version;

    private GradleDistribution(File localInstallationDir, URI remoteDistributionUri, String version) {
        this.localInstallationDir = localInstallationDir;
        this.remoteDistributionUri = remoteDistributionUri;
        this.version = version;

        checkNoMoreThanOneAttributeSet(localInstallationDir, remoteDistributionUri, version);
    }

    private void checkNoMoreThanOneAttributeSet(File localInstallationDir, URI remoteDistributionUri, String version) {
        int count = 0;
        List<Object> items = Arrays.<Object>asList(localInstallationDir, remoteDistributionUri, version);
        for (Object item : items) {
            if (item != null) {
                count++;
            }
        }
        Preconditions.checkArgument(count <= 1, "Attributes of more than one distribution type specified.");
    }

    /**
     * Configures the specified connector with this distribution.
     *
     * @param connector the connector to configure
     */
    public void apply(GradleConnector connector) {
        Preconditions.checkNotNull(connector);
        if (this.localInstallationDir != null) {
            connector.useInstallation(this.localInstallationDir);
        } else if (this.remoteDistributionUri != null) {
            connector.useDistribution(this.remoteDistributionUri);
        } else if (this.version != null) {
            connector.useGradleVersion(this.version);
        } else {
            connector.useBuildDistribution();
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
        return Objects.equal(this.localInstallationDir, that.localInstallationDir) &&
                Objects.equal(this.remoteDistributionUri, that.remoteDistributionUri) &&
                Objects.equal(this.version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.localInstallationDir, this.remoteDistributionUri, this.version);
    }

    @Override
    public String toString() {
        if (this.localInstallationDir != null) {
            return String.format("Gradle installation %s", this.localInstallationDir.getAbsolutePath());
        } else if (this.remoteDistributionUri != null) {
            return String.format("Gradle distribution %s", this.remoteDistributionUri);
        } else if (this.version != null) {
            return String.format("Gradle version %s", this.version);
        } else {
            return "Gradle version of target build";
        }
    }

    /**
     * Creates a reference to a local Gradle installation.
     *
     * @param installationDir the local Gradle installation directory to use
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useInstallation(java.io.File)
     */
    public static GradleDistribution forLocalInstallation(File installationDir) {
        Preconditions.checkNotNull(installationDir);
        return new GradleDistribution(installationDir, null, null);
    }

    /**
     * Creates a reference to a remote Gradle distribution. The appropriate distribution is downloaded and installed into the user's Gradle home directory.
     *
     * @param distributionUri the remote Gradle distribution location to use
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useDistribution(java.net.URI)
     */
    public static GradleDistribution forRemoteDistribution(URI distributionUri) {
        Preconditions.checkNotNull(distributionUri);
        return new GradleDistribution(null, distributionUri, null);
    }

    /**
     * Creates a reference to a specific version of Gradle. The appropriate distribution is downloaded and installed into the user's Gradle home directory.
     *
     * @param version the Gradle version to use
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useGradleVersion(String)
     */
    public static GradleDistribution forVersion(String version) {
        Preconditions.checkNotNull(version);
        return new GradleDistribution(null, null, version);
    }

    /**
     * Creates a reference to a project-specific version of Gradle.
     *
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useBuildDistribution()
     */
    public static GradleDistribution fromBuild() {
        return new GradleDistribution(null, null, null);
    }

}
