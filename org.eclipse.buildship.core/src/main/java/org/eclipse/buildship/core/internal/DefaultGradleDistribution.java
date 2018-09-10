/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.gradle.tooling.GradleConnector;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.eclipse.buildship.core.*;

/**
 * Represents a Gradle distribution that can be located locally or remotely, be a fixed version, or
 * be project-specific.
 *
 * @author Etienne Studer
 */
public class DefaultGradleDistribution implements GradleDistribution {

    /**
     * The available Gradle distributions types.
     */
    public enum Type {

        /**
         * Distribution handled via the wrapper script.
         */
        WRAPPER,

        /**
         * Distribution loaded from the the disk.
         */
        LOCAL_INSTALLATION,

        /**
         * Distribution downloaded from a remote URI.
         */
        REMOTE_DISTRIBUTION,

        /**
         * A specific Gradle version.
         */
        VERSION
    }

    private final GradleDistributionInfo distributionInfo;

    private DefaultGradleDistribution(Type type, String configuration) {
        this(new GradleDistributionInfo(type, configuration));
    }

    private DefaultGradleDistribution(GradleDistributionInfo distributionInfo) {
        Optional<String> validationError = distributionInfo.validate();
        Preconditions.checkArgument(!validationError.isPresent(), validationError.or(""));
        this.distributionInfo = distributionInfo;
    }

    public GradleDistributionInfo getDistributionInfo() {
        return this.distributionInfo;
    }

    public Type getType() {
        return this.distributionInfo.getType().get();
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
        switch (this.distributionInfo.getType().get()) {
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

        DefaultGradleDistribution that = (DefaultGradleDistribution) other;
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

    public static DefaultGradleDistribution forLocalInstallation(String installationDir) {
        return new DefaultGradleDistribution(Type.LOCAL_INSTALLATION, installationDir);
    }

    public static DefaultGradleDistribution forLocalInstallation(File installationDir) {
        return new DefaultGradleDistribution(Type.LOCAL_INSTALLATION, installationDir.getAbsolutePath());
    }

    public static DefaultGradleDistribution forRemoteDistribution(String distributionUri) {
        return new DefaultGradleDistribution(Type.REMOTE_DISTRIBUTION, distributionUri);
    }

    public static DefaultGradleDistribution forRemoteDistribution(URI distributionUri) {
        return new DefaultGradleDistribution(Type.REMOTE_DISTRIBUTION, distributionUri.toString());
    }

    public static DefaultGradleDistribution forVersion(String version) {
        return new DefaultGradleDistribution(Type.VERSION, version);
    }

    public static DefaultGradleDistribution fromBuild() {
        return new DefaultGradleDistribution(Type.WRAPPER, null);
    }

    public static DefaultGradleDistribution fromDistributionInfo(GradleDistributionInfo distributionInfo) {
        return new DefaultGradleDistribution(distributionInfo);
    }
}
