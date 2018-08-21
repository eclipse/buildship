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

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

import org.eclipse.osgi.util.NLS;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.i18n.CoreMessages;
import org.eclipse.buildship.core.internal.util.binding.Validator;

/**
 * Describes a valid or invalid {@link GradleDistribution}.
 *
 * @author Donat Csikos
 */
public final class GradleDistributionInfo {

    private final GradleDistributionType type;
    private final String configuration;

    public GradleDistributionInfo(GradleDistributionType type, String configuration) {
        this.type = type != null ? type : GradleDistributionType.INVALID;
        this.configuration = Strings.nullToEmpty(configuration);
    }

    public GradleDistributionType getType() {
        return this.type;
    }

    public String getConfiguration() {
        return this.configuration;
    }

    /**
     * Returns whether instance describes a valid Gradle distribution.
     *
     * @return true if can be converted to a {@link GradleDistribution} object
     */
    public boolean isValid() {
        return !validate().isPresent();
    }

    /**
     * Returns an error message if the current instance represents an invalid
     * {@link GradleDistribution}.
     *
     * @return a human-readable error message describing the problem
     */
    public Optional<String> validate() {
        if (GradleDistributionType.INVALID == this.type) {
            return Optional.of("Invalid distribution type"); // TODO (donat) externalize string
        } else if (GradleDistributionType.LOCAL_INSTALLATION == this.type) {
            if (Strings.isNullOrEmpty(this.configuration)) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleDistribution_Label_LocalInstallationDirectory));
            } else if (!new File(this.configuration).exists()) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_DoesNotExist, CoreMessages.GradleDistribution_Label_LocalInstallationDirectory));
            } else {
                return Optional.absent();
            }
        } else if (GradleDistributionType.REMOTE_DISTRIBUTION == this.type) {
            if (Strings.isNullOrEmpty(this.configuration)) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleDistribution_Label_RemoteDistributionUri));
            } else if (!isValidURI(this.configuration)) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_IsNotValid, CoreMessages.GradleDistribution_Label_RemoteDistributionUri));
            } else {
                return Optional.absent();
            }
        } else if (GradleDistributionType.VERSION == this.type) {
            if (Strings.isNullOrEmpty(this.configuration)) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleDistribution_Label_SpecificGradleVersion));
            } else {
                return Optional.absent();
            }
        } else {
            return Optional.absent();
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

    public GradleDistribution toGradleDistribution() {
        return GradleDistribution.fromDistributionInfo(this);
    }

    /**
     * Converts the current object to a {@link GradleDistribution} or returns
     * {@link GradleDistribution#fromBuild()} if invalid.
     *
     * @return the created Gradle distribution
     */
    public GradleDistribution toGradleDistributionOrDefault() {
        return isValid() ? toGradleDistribution() : GradleDistribution.fromBuild();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        GradleDistributionInfo that = (GradleDistributionInfo) other;
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
            return new GradleDistributionInfo(GradleDistributionType.INVALID, "");
        }

        String localInstallationPrefix = "GRADLE_DISTRIBUTION(LOCAL_INSTALLATION(";
        if (distributionString.startsWith(localInstallationPrefix) && distributionString.endsWith("))")) {
            String configuration = distributionString.substring(localInstallationPrefix.length(), distributionString.length() - 2);

            return new GradleDistributionInfo(GradleDistributionType.LOCAL_INSTALLATION, configuration);
        }

        String remoteDistributionPrefix = "GRADLE_DISTRIBUTION(REMOTE_DISTRIBUTION(";
        if (distributionString.startsWith(remoteDistributionPrefix) && distributionString.endsWith("))")) {
            String configuration = distributionString.substring(remoteDistributionPrefix.length(), distributionString.length() - 2);
            return new GradleDistributionInfo(GradleDistributionType.REMOTE_DISTRIBUTION, configuration);
        }

        String versionPrefix = "GRADLE_DISTRIBUTION(VERSION(";
        if (distributionString.startsWith(versionPrefix) && distributionString.endsWith("))")) {
            String configuration = distributionString.substring(versionPrefix.length(), distributionString.length() - 2);
            return new GradleDistributionInfo(GradleDistributionType.VERSION, configuration);
        }

        String wrapperString = "GRADLE_DISTRIBUTION(WRAPPER)";
        if (distributionString.equals(wrapperString)) {
            return new GradleDistributionInfo(GradleDistributionType.WRAPPER, null);
        }

        String invalidDistributionPrefix = "INVALID_GRADLE_DISTRIBUTION(";
        if (distributionString.startsWith(invalidDistributionPrefix) && distributionString.endsWith("))")) {
            String configuration = distributionString.substring(invalidDistributionPrefix.length(), distributionString.length() - 2);
            return new GradleDistributionInfo(GradleDistributionType.INVALID, configuration);
        }

        return new GradleDistributionInfo(GradleDistributionType.INVALID, distributionString);
    }

    public static Validator<GradleDistributionInfo> validator() {
        return new Validator<GradleDistributionInfo>() {

            @Override
            public Optional<String> validate(GradleDistributionInfo distributionInfo) {
                return distributionInfo.validate();
            }
        };
    }
}
