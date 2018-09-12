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

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

import org.eclipse.osgi.util.NLS;

import org.eclipse.buildship.core.FixedVersionGradleDistribution;
import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.GradleDistributions;
import org.eclipse.buildship.core.LocalGradleDistribution;
import org.eclipse.buildship.core.RemoteGradleDistribution;
import org.eclipse.buildship.core.WrapperGradleDistribution;
import org.eclipse.buildship.core.internal.i18n.CoreMessages;
import org.eclipse.buildship.core.internal.util.binding.Validator;

/**
 * Describes a valid or invalid {@link BaseGradleDistribution}.
 *
 * @author Donat Csikos
 */
public final class GradleDistributionInfo {

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

    private static final int INVALID_TYPE_ID = GradleDistributionInfo.Type.values().length;

    private final int typeId;
    private final String configuration;

    public GradleDistributionInfo(GradleDistributionInfo.Type type, String configuration) {
        this.typeId = type != null ? type.ordinal() : INVALID_TYPE_ID;
        this.configuration = Strings.nullToEmpty(configuration);
    }

    public java.util.Optional<Type> getType() {
        if (this.typeId == INVALID_TYPE_ID) {
            return java.util.Optional.empty();
        } else {
            return java.util.Optional.of(GradleDistributionInfo.Type.values()[this.typeId]);
        }
    }

    public String getConfiguration() {
        return this.configuration;
    }

    /**
     * Returns whether instance describes a valid Gradle distribution.
     *
     * @return true if can be converted to a {@link BaseGradleDistribution} object
     */
    public boolean isValid() {
        return !validate().isPresent();
    }

    /**
     * Returns an error message if the current instance represents an invalid
     * {@link BaseGradleDistribution}.
     *
     * @return a human-readable error message describing the problem
     */
    public Optional<String> validate() {
        java.util.Optional<GradleDistributionInfo.Type> typeOrNull = getType();
        if (!typeOrNull.isPresent()) {
            return Optional.of("Invalid distribution type"); // TODO (donat) externalize string
        }

        GradleDistributionInfo.Type type = typeOrNull.get();
        if (type == GradleDistributionInfo.Type.LOCAL_INSTALLATION) {
            if (Strings.isNullOrEmpty(this.configuration)) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleDistribution_Label_LocalInstallationDirectory));
            } else {
                return validateLocalInstallationLocation(new File(this.configuration));
            }
        } else if (type == GradleDistributionInfo.Type.REMOTE_DISTRIBUTION) {
            if (Strings.isNullOrEmpty(this.configuration)) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleDistribution_Label_RemoteDistributionUri));
            } else if (!isValidURI(this.configuration)) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_IsNotValid, CoreMessages.GradleDistribution_Label_RemoteDistributionUri));
            } else {
                return Optional.absent();
            }
        } else if (type == GradleDistributionInfo.Type.VERSION) {
            if (Strings.isNullOrEmpty(this.configuration)) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleDistribution_Label_SpecificGradleVersion));
            } else {
                return Optional.absent();
            }
        } else {
            return Optional.absent();
        }
    }

    public static Optional<String> validateLocalInstallationLocation(File location) {
        if (location == null) {
            return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleDistribution_Label_LocalInstallationDirectory));
        } else if (!location.exists()) {
            return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_DoesNotExist, CoreMessages.GradleDistribution_Label_LocalInstallationDirectory));
        } else if (!location.isDirectory()) {
            return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeDirectory, CoreMessages.GradleDistribution_Label_LocalInstallationDirectory));
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
       Optional<String> errorMessage = validate();
       if (errorMessage.isPresent()) {
           throw new GradlePluginsRuntimeException(errorMessage.get());
       }

        switch (getType().get()) {
            case WRAPPER:
                return GradleDistributions.fromBuild();
            case LOCAL_INSTALLATION:
                return GradleDistributions.forLocalInstallation(new File(this.configuration));
            case REMOTE_DISTRIBUTION:
                return GradleDistributions.forRemoteDistribution(createUrl(this.configuration));
            case VERSION:
                return GradleDistributions.forVersion(this.configuration);
        }

        throw new GradlePluginsRuntimeException("Invalid distribution type: " + getType().get());
    }

    private static URI createUrl(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    /**
     * Converts the current object to a {@link BaseGradleDistribution} or returns
     * {@link BaseGradleDistribution#fromBuild()} if invalid.
     *
     * @return the created Gradle distribution
     */
    public GradleDistribution toGradleDistributionOrDefault() {
        return isValid() ? toGradleDistribution() : GradleDistributions.fromBuild();
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
        return Objects.equal(this.typeId, that.typeId) && Objects.equal(this.configuration, that.configuration);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.typeId, this.configuration);
    }

    @Override
    public String toString() {
        java.util.Optional<GradleDistributionInfo.Type> typeOrNull = getType();

        if (!typeOrNull.isPresent()) {
            return "Unknown Gradle distribution " + this.configuration; // TODO (donat) NLS
        }

        GradleDistributionInfo.Type type = typeOrNull.get();
        switch (type) {
            case WRAPPER:
                return CoreMessages.GradleDistribution_Value_UseGradleWrapper;
            case LOCAL_INSTALLATION:
                return NLS.bind(CoreMessages.GradleDistribution_Value_UseLocalInstallation_0, this.configuration);
            case REMOTE_DISTRIBUTION:
                return NLS.bind(CoreMessages.GradleDistribution_Value_UseRemoteDistribution_0, this.configuration);
            case VERSION:
                return NLS.bind(CoreMessages.GradleDistribution_Value_UseGradleVersion_0, this.configuration);
            default:
                throw new GradlePluginsRuntimeException("Unrecognized Gradle distribution type: " + type);
        }
    }

    public String serializeToString() {
        java.util.Optional<GradleDistributionInfo.Type> typeOrNull = getType();

        if (!typeOrNull.isPresent()) {
            return String.format("INVALID_GRADLE_DISTRIBUTION(%s))", this.configuration);
        }

        GradleDistributionInfo.Type type = typeOrNull.get();
        switch (type) {
            case LOCAL_INSTALLATION:
                return String.format("GRADLE_DISTRIBUTION(LOCAL_INSTALLATION(%s))", this.configuration);
            case REMOTE_DISTRIBUTION:
                return String.format("GRADLE_DISTRIBUTION(REMOTE_DISTRIBUTION(%s))", this.configuration);
            case VERSION:
                return String.format("GRADLE_DISTRIBUTION(VERSION(%s))", this.configuration);
            case WRAPPER:
                return String.valueOf("GRADLE_DISTRIBUTION(WRAPPER)");
            default:
                throw new GradlePluginsRuntimeException("Invalid distribution type: " + type);
        }
    }

    public static GradleDistributionInfo from(GradleDistribution gradleDistribution) {
        if (gradleDistribution instanceof LocalGradleDistribution) {
            return new GradleDistributionInfo(Type.LOCAL_INSTALLATION, ((LocalGradleDistribution)gradleDistribution).getLocation().getAbsolutePath());
        } else if (gradleDistribution instanceof RemoteGradleDistribution) {
            return new GradleDistributionInfo(Type.REMOTE_DISTRIBUTION, ((RemoteGradleDistribution)gradleDistribution).getUrl().toString());
        } else if (gradleDistribution instanceof FixedVersionGradleDistribution) {
            return new GradleDistributionInfo(Type.VERSION, ((FixedVersionGradleDistribution)gradleDistribution).getVersion());
        } else if (gradleDistribution instanceof WrapperGradleDistribution) {
            return new GradleDistributionInfo(Type.WRAPPER, null);
        } else {
            throw new GradlePluginsRuntimeException("Invalid distribution type: " + gradleDistribution);
        }
    }

    public static GradleDistributionInfo deserializeFromString(String distributionString) {
        if (distributionString == null) {
            return new GradleDistributionInfo(null, null);
        }

        String localInstallationPrefix = "GRADLE_DISTRIBUTION(LOCAL_INSTALLATION(";
        if (distributionString.startsWith(localInstallationPrefix) && distributionString.endsWith("))")) {
            String configuration = distributionString.substring(localInstallationPrefix.length(), distributionString.length() - 2);

            return new GradleDistributionInfo(GradleDistributionInfo.Type.LOCAL_INSTALLATION, configuration);
        }

        String remoteDistributionPrefix = "GRADLE_DISTRIBUTION(REMOTE_DISTRIBUTION(";
        if (distributionString.startsWith(remoteDistributionPrefix) && distributionString.endsWith("))")) {
            String configuration = distributionString.substring(remoteDistributionPrefix.length(), distributionString.length() - 2);
            return new GradleDistributionInfo(GradleDistributionInfo.Type.REMOTE_DISTRIBUTION, configuration);
        }

        String versionPrefix = "GRADLE_DISTRIBUTION(VERSION(";
        if (distributionString.startsWith(versionPrefix) && distributionString.endsWith("))")) {
            String configuration = distributionString.substring(versionPrefix.length(), distributionString.length() - 2);
            return new GradleDistributionInfo(GradleDistributionInfo.Type.VERSION, configuration);
        }

        String wrapperString = "GRADLE_DISTRIBUTION(WRAPPER)";
        if (distributionString.equals(wrapperString)) {
            return new GradleDistributionInfo(GradleDistributionInfo.Type.WRAPPER, null);
        }

        String invalidDistributionPrefix = "INVALID_GRADLE_DISTRIBUTION(";
        if (distributionString.startsWith(invalidDistributionPrefix) && distributionString.endsWith("))")) {
            String configuration = distributionString.substring(invalidDistributionPrefix.length(), distributionString.length() - 2);
            return new GradleDistributionInfo(null, configuration);
        }

        return new GradleDistributionInfo(null, distributionString);
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
