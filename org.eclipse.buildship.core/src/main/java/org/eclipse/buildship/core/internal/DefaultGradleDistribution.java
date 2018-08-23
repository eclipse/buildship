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

import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.GradleDistributionInfo;
import org.eclipse.buildship.core.GradleDistributionType;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;

/**
 * Default implementation for {@link GradleDistribution}.
 */
public final class DefaultGradleDistribution extends GradleDistribution {

    private final GradleDistributionInfo distributionInfo;

    private DefaultGradleDistribution(GradleDistributionType type, String configuration) {
        this(GradleDistributionInfo.from(type, configuration));
    }

    private DefaultGradleDistribution(GradleDistributionInfo distributionInfo) {
        Optional<String> validationError = distributionInfo.validate();
        Preconditions.checkArgument(!validationError.isPresent(), validationError.or(""));
        this.distributionInfo = distributionInfo;
    }

    @Override
    public GradleDistributionInfo getDistributionInfo() {
        return this.distributionInfo;
    }

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

    public static GradleDistribution forLocalInstallation(String installationDir) {
        return new DefaultGradleDistribution(GradleDistributionType.LOCAL_INSTALLATION, installationDir);
    }

    public static GradleDistribution forLocalInstallation(File installationDir) {
        return new DefaultGradleDistribution(GradleDistributionType.LOCAL_INSTALLATION, installationDir.getAbsolutePath());
    }

    public static GradleDistribution forRemoteDistribution(String distributionUri) {
        return new DefaultGradleDistribution(GradleDistributionType.REMOTE_DISTRIBUTION, distributionUri);
    }

    public static GradleDistribution forRemoteDistribution(URI distributionUri) {
        return new DefaultGradleDistribution(GradleDistributionType.REMOTE_DISTRIBUTION, distributionUri.toString());
    }

    public static GradleDistribution forVersion(String version) {
        return new DefaultGradleDistribution(GradleDistributionType.VERSION, version);
    }

    public static GradleDistribution fromBuild() {
        return new DefaultGradleDistribution(GradleDistributionType.WRAPPER, null);
    }

    public static GradleDistribution fromDistributionInfo(GradleDistributionInfo distributionInfo) {
        return new DefaultGradleDistribution(distributionInfo);
    }

}
