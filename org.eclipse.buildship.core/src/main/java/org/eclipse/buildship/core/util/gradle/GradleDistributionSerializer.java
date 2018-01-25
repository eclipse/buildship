/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.util.gradle;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.common.base.Preconditions;

/**
 * Serializes / deserializes a {@link GradleDistribution} to / from a {@link String}.
 */
public final class GradleDistributionSerializer {

    public static final GradleDistributionSerializer INSTANCE = new GradleDistributionSerializer();

    private GradleDistributionSerializer() {
    }

    /**
     * Serializes the given Gradle distribution to its String representation.
     *
     * @param distribution the distribution to serialize
     * @return the serialized distribution
     */
    public String serializeToString(GradleDistribution distribution) {
        Preconditions.checkNotNull(distribution);

        try {
            Field localInstallationDirField = GradleDistribution.class.getDeclaredField("localInstallationDir");
            localInstallationDirField.setAccessible(true);
            File localInstallationDir = (File) localInstallationDirField.get(distribution);
            if (localInstallationDir != null) {
                return String.format("GRADLE_DISTRIBUTION(LOCAL_INSTALLATION(%s))", localInstallationDir.getAbsolutePath());
            }

            Field remoteDistributionUriField = GradleDistribution.class.getDeclaredField("remoteDistributionUri");
            remoteDistributionUriField.setAccessible(true);
            URI remoteDistributionUri = (URI) remoteDistributionUriField.get(distribution);
            if (remoteDistributionUri != null) {
                return String.format("GRADLE_DISTRIBUTION(REMOTE_DISTRIBUTION(%s))", remoteDistributionUri.toString());
            }

            Field versionField = GradleDistribution.class.getDeclaredField("version");
            versionField.setAccessible(true);
            String version = (String) versionField.get(distribution);
            if (version != null) {
                return String.format("GRADLE_DISTRIBUTION(VERSION(%s))", version);
            }

            return String.valueOf("GRADLE_DISTRIBUTION(WRAPPER)");
        } catch (Exception e) {
            String message = String.format("Cannot serialize Gradle distribution '%s.'", distribution);
            throw new RuntimeException(message, e);
        }
    }

    /**
     * Deserializes the Gradle distribution from the the given String representation.
     *
     * @param distributionString the serialized distribution
     * @return the deserialized distribution
     */
    public GradleDistribution deserializeFromString(String distributionString) {
        Preconditions.checkNotNull(distributionString);

        String localInstallationPrefix = "GRADLE_DISTRIBUTION(LOCAL_INSTALLATION(";
        if (distributionString.startsWith(localInstallationPrefix) && distributionString.endsWith("))")) {
            String localInstallationDir = distributionString.substring(localInstallationPrefix.length(), distributionString.length() - 2);
            return GradleDistribution.forLocalInstallation(new File(localInstallationDir));
        }

        String remoteDistributionPrefix = "GRADLE_DISTRIBUTION(REMOTE_DISTRIBUTION(";
        if (distributionString.startsWith(remoteDistributionPrefix) && distributionString.endsWith("))")) {
            String remoteDistributionUri = distributionString.substring(remoteDistributionPrefix.length(), distributionString.length() - 2);
            return GradleDistribution.forRemoteDistribution(createURI(remoteDistributionUri));
        }

        String versionPrefix = "GRADLE_DISTRIBUTION(VERSION(";
        if (distributionString.startsWith(versionPrefix) && distributionString.endsWith("))")) {
            String version = distributionString.substring(versionPrefix.length(), distributionString.length() - 2);
            return GradleDistribution.forVersion(version);
        }

        String wrapperString = "GRADLE_DISTRIBUTION(WRAPPER)";
        if (distributionString.equals(wrapperString)) {
            return GradleDistribution.fromBuild();
        }

        String message = String.format("Cannot deserialize Gradle distribution string '%s.'", distributionString);
        throw new RuntimeException(message);
    }

    private URI createURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
