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

import org.gradle.tooling.GradleConnector;

import com.google.common.base.Preconditions;

/**
 * Wraps the type of Gradle distribution and its configurations, e.g. using a fixed Gradle version
 * (type) in version 2.1 (configuration).
 */
public final class GradleDistributionWrapper {

    private final DistributionType type;
    private final String configuration;

    private GradleDistributionWrapper(DistributionType type, String configuration) {
        this.type = Preconditions.checkNotNull(type);
        this.configuration = configuration;
    }

    public DistributionType getType() {
        return this.type;
    }

    public String getConfiguration() {
        return this.configuration;
    }

    public GradleDistribution toGradleDistribution() {
        if (this.type == DistributionType.LOCAL_INSTALLATION) {
            return GradleDistribution.forLocalInstallation(new File(this.configuration));
        } else if (this.type == DistributionType.REMOTE_DISTRIBUTION) {
            return GradleDistribution.forRemoteDistribution(createURI(this.configuration));
        } else if (this.type == DistributionType.VERSION) {
            return GradleDistribution.forVersion(this.configuration);
        } else {
            return GradleDistribution.fromBuild();
        }
    }

    private URI createURI(String path) {
        try {
            return new URI(path);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void apply(GradleConnector connector) {
        if (this.type == DistributionType.LOCAL_INSTALLATION) {
            connector.useInstallation(new File(this.configuration));
        } else if (this.type == DistributionType.REMOTE_DISTRIBUTION) {
           connector.useDistribution(createURI(this.configuration));
        } else if (this.type == DistributionType.VERSION) {
            connector.useGradleVersion(this.configuration);
        } else {
            connector.useBuildDistribution();
        }
    }

    public static GradleDistributionWrapper from(DistributionType type, String configuration) {
        return new GradleDistributionWrapper(type, configuration);
    }

    public static GradleDistributionWrapper from(GradleDistribution distribution) {
        Preconditions.checkNotNull(distribution);

        try {
            Field localInstallationDirField = GradleDistribution.class.getDeclaredField("localInstallationDir");
            localInstallationDirField.setAccessible(true);
            File localInstallationDir = (File) localInstallationDirField.get(distribution);
            if (localInstallationDir != null) {
                return from(DistributionType.LOCAL_INSTALLATION, localInstallationDir.getAbsolutePath());
            }

            Field remoteDistributionUriField = GradleDistribution.class.getDeclaredField("remoteDistributionUri");
            remoteDistributionUriField.setAccessible(true);
            URI remoteDistributionUri = (URI) remoteDistributionUriField.get(distribution);
            if (remoteDistributionUri != null) {
                return from(DistributionType.REMOTE_DISTRIBUTION, remoteDistributionUri.toString());
            }

            Field versionField = GradleDistribution.class.getDeclaredField("version");
            versionField.setAccessible(true);
            String version = (String) versionField.get(distribution);
            if (version != null) {
                return from(DistributionType.VERSION, version);
            }

            return from(DistributionType.WRAPPER, null);
        } catch (Exception e) {
            String message = String.format("Cannot serialize Gradle distribution '%s.'", distribution);
            throw new RuntimeException(message, e);
        }
    }

    /**
     * Enumerates the different types of Gradle distributions.
     */
    public enum DistributionType {
        WRAPPER, LOCAL_INSTALLATION, REMOTE_DISTRIBUTION, VERSION
    }

}
