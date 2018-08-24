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
import java.util.Optional;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import org.eclipse.osgi.util.NLS;

import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.GradleDistributionInfo;
import org.eclipse.buildship.core.GradleDistributionType;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.i18n.CoreMessages;

/**
 * Default implementation for {@link GradleDistributionInfo}.
 */
public final class DefaultGradleDistributionInfo extends GradleDistributionInfo {

    private final GradleDistributionType type;
    private final String configuration;

    private DefaultGradleDistributionInfo(GradleDistributionType type, String configuration) {
        this.type = type != null ? type : GradleDistributionType.INVALID;
        this.configuration = Strings.nullToEmpty(configuration);
    }

    @Override
    public GradleDistributionType getType() {
        return this.type;
    }

    @Override
    public String getConfiguration() {
        return this.configuration;
    }

    @Override
    public Optional<String> validate() {
        if (GradleDistributionType.INVALID == this.type) {
            return Optional.of(CoreMessages.ErrorMessage_Invalid_GradleDistribution);
        } else if (GradleDistributionType.LOCAL_INSTALLATION == this.type) {
            if (Strings.isNullOrEmpty(this.configuration)) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleDistribution_Label_LocalInstallationDirectory));
            } else if (!new File(this.configuration).exists()) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_DoesNotExist, CoreMessages.GradleDistribution_Label_LocalInstallationDirectory));
            } else {
                return Optional.empty();
            }
        } else if (GradleDistributionType.REMOTE_DISTRIBUTION == this.type) {
            if (Strings.isNullOrEmpty(this.configuration)) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleDistribution_Label_RemoteDistributionUri));
            } else if (!isValidURI(this.configuration)) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_IsNotValid, CoreMessages.GradleDistribution_Label_RemoteDistributionUri));
            } else {
                return Optional.empty();
            }
        } else if (GradleDistributionType.VERSION == this.type) {
            if (Strings.isNullOrEmpty(this.configuration)) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleDistribution_Label_SpecificGradleVersion));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    private boolean isValidURI(String configuration) {
        try {
            new URI(configuration);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    @Override
    public GradleDistribution toGradleDistribution() {
        return DefaultGradleDistribution.fromDistributionInfo(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        DefaultGradleDistributionInfo that = (DefaultGradleDistributionInfo) other;
        return Objects.equal(this.type, that.type) && Objects.equal(this.configuration, that.configuration);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.type, this.configuration);
    }

    @Override
    public String toString() {
        switch (this.type) {
            case WRAPPER:
                return CoreMessages.GradleDistribution_Value_UseGradleWrapper;
            case LOCAL_INSTALLATION:
                return NLS.bind(CoreMessages.GradleDistribution_Value_UseLocalInstallation_0, this.configuration);
            case REMOTE_DISTRIBUTION:
                return NLS.bind(CoreMessages.GradleDistribution_Value_UseRemoteDistribution_0, this.configuration);
            case VERSION:
                return NLS.bind(CoreMessages.GradleDistribution_Value_UseGradleVersion_0, this.configuration);
            case INVALID:
                return "Unknown Gradle distribution " + this.configuration; // TODO (donat) NLS
            default:
                throw new GradlePluginsRuntimeException("Unrecognized Gradle distribution type: " + this.type);
        }
    }

    @Override
    public String serializeToString() {
        switch (this.type) {
            case INVALID:
                return String.format("INVALID_GRADLE_DISTRIBUTION(%s))", this.configuration);
            case LOCAL_INSTALLATION:
                return String.format("GRADLE_DISTRIBUTION(LOCAL_INSTALLATION(%s))", this.configuration);
            case REMOTE_DISTRIBUTION:
                return String.format("GRADLE_DISTRIBUTION(REMOTE_DISTRIBUTION(%s))", this.configuration);
            case VERSION:
                return String.format("GRADLE_DISTRIBUTION(VERSION(%s))", this.configuration);
            case WRAPPER:
                return String.valueOf("GRADLE_DISTRIBUTION(WRAPPER)");
            default:
                throw new GradlePluginsRuntimeException("Invalid distribution type: " + this.type);
        }
    }

    public static GradleDistributionInfo deserializeFromString(String distributionString) {
        if (distributionString == null) {
            return from(GradleDistributionType.INVALID, "");
        }

        String localInstallationPrefix = "GRADLE_DISTRIBUTION(LOCAL_INSTALLATION(";
        if (distributionString.startsWith(localInstallationPrefix) && distributionString.endsWith("))")) {
            String configuration = distributionString.substring(localInstallationPrefix.length(), distributionString.length() - 2);

            return from(GradleDistributionType.LOCAL_INSTALLATION, configuration);
        }

        String remoteDistributionPrefix = "GRADLE_DISTRIBUTION(REMOTE_DISTRIBUTION(";
        if (distributionString.startsWith(remoteDistributionPrefix) && distributionString.endsWith("))")) {
            String configuration = distributionString.substring(remoteDistributionPrefix.length(), distributionString.length() - 2);
            return from(GradleDistributionType.REMOTE_DISTRIBUTION, configuration);
        }

        String versionPrefix = "GRADLE_DISTRIBUTION(VERSION(";
        if (distributionString.startsWith(versionPrefix) && distributionString.endsWith("))")) {
            String configuration = distributionString.substring(versionPrefix.length(), distributionString.length() - 2);
            return from(GradleDistributionType.VERSION, configuration);
        }

        String wrapperString = "GRADLE_DISTRIBUTION(WRAPPER)";
        if (distributionString.equals(wrapperString)) {
            return from(GradleDistributionType.WRAPPER, null);
        }

        String invalidDistributionPrefix = "INVALID_GRADLE_DISTRIBUTION(";
        if (distributionString.startsWith(invalidDistributionPrefix) && distributionString.endsWith("))")) {
            String configuration = distributionString.substring(invalidDistributionPrefix.length(), distributionString.length() - 2);
            return from(GradleDistributionType.INVALID, configuration);
        }

        return from(GradleDistributionType.INVALID, distributionString);
    }

    public static GradleDistributionInfo from(GradleDistributionType type, String configuration) {
        return new DefaultGradleDistributionInfo(type, configuration);
    }
}
