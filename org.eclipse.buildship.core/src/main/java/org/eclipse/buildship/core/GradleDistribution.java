/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.gradle.tooling.GradleConnector;

import com.google.common.base.Preconditions;

/**
 * Represents a Gradle distribution.
 * <p>
 * Currently four different Gradle distribution types are supported.
 * <ul>
 * <li>{@link WrapperGradleDistribution}</li>
 * <li>{@link LocalGradleDistribution}</li>
 * <li>{@link RemoteGradleDistribution}</li>
 * <li>{@link FixedVersionGradleDistribution}</li>
 * </ul>
 *
 * @author Donat Csikos
 * @since 3.0
 * @noimplement this interface is not intended to be implemented by clients
 */
public abstract class GradleDistribution {

    private static final WrapperGradleDistribution WRAPPER_DISTRIBUTION = new WrapperGradleDistribution();

    /**
     * Creates a reference to a local Gradle installation.
     *
     * @param installationDir the local Gradle installation directory to use
     * @return a new distribution instance
     */
    public static LocalGradleDistribution forLocalInstallation(File installationDir) {
        return new LocalGradleDistribution(installationDir);
    }

    /**
     * Creates a reference to a remote Gradle distribution.
     *
     * @param distributionUri the remote Gradle distribution location to use
     * @return a new distribution instance
     */
    public static RemoteGradleDistribution forRemoteDistribution(URI distributionUri) {
        return new RemoteGradleDistribution(distributionUri);
    }

    /**
     * Creates a reference to a specific version of Gradle.
     *
     * @param version the Gradle version to use
     * @return a new distribution instance
     */
    public static FixedVersionGradleDistribution forVersion(String version) {
        return new FixedVersionGradleDistribution(version);
    }

    /**
     * Creates a reference to a project-specific version of Gradle.
     *
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useBuildDistribution()
     */
    public static WrapperGradleDistribution fromBuild() {
        return WRAPPER_DISTRIBUTION;
    }

    /**
     * Deserializes a distribution.
     * <p>
     * This method recognizes strings returned by {@link #toString()}.
     *
     * @param distributionString the string representation of a distribution
     * @return the parsed distribution
     * @throws RuntimeException if the deserialization fails
     */
    public static GradleDistribution fromString(String distributionString) {
        Preconditions.checkNotNull(distributionString);

        String localInstallationPrefix = "GRADLE_DISTRIBUTION(LOCAL_INSTALLATION(";
        if (distributionString.startsWith(localInstallationPrefix) && distributionString.endsWith("))")) {
            String configuration = distributionString.substring(localInstallationPrefix.length(), distributionString.length() - 2);
            return forLocalInstallation(new File(configuration));
        }

        String remoteDistributionPrefix = "GRADLE_DISTRIBUTION(REMOTE_DISTRIBUTION(";
        if (distributionString.startsWith(remoteDistributionPrefix) && distributionString.endsWith("))")) {
            String configuration = distributionString.substring(remoteDistributionPrefix.length(), distributionString.length() - 2);
            return forRemoteDistribution(createUrl(configuration));
        }

        String versionPrefix = "GRADLE_DISTRIBUTION(VERSION(";
        if (distributionString.startsWith(versionPrefix) && distributionString.endsWith("))")) {
            String configuration = distributionString.substring(versionPrefix.length(), distributionString.length() - 2);
            return forVersion(configuration);
        }

        String wrapperString = "GRADLE_DISTRIBUTION(WRAPPER)";
        if (distributionString.equals(wrapperString)) {
            return WRAPPER_DISTRIBUTION;
        }

        throw new IllegalArgumentException("Unrecognized Gradle distribution type: " + distributionString);
    }

    private static URI createUrl(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    GradleDistribution() {
    }

    /**
     * Configures the specified connector with this distribution.
     *
     * @param connector the connector to configure
     */
    public abstract void apply(GradleConnector connector);

    /**
     * Returns the human-readable representation of this distribution that can be displayed on the
     * UI.
     */
    public abstract String getDisplayName();
}
