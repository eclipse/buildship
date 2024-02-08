/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.util.gradle;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

import org.eclipse.osgi.util.NLS;

import org.eclipse.buildship.core.FixedVersionGradleDistribution;
import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.LocalGradleDistribution;
import org.eclipse.buildship.core.RemoteGradleDistribution;
import org.eclipse.buildship.core.WrapperGradleDistribution;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.i18n.CoreMessages;
import org.eclipse.buildship.core.internal.util.binding.Validator;

/**
 * Describes a valid or invalid {@link BaseGradleDistribution}.
 *
 * @author Donat Csikos
 */
public final class GradleDistributionViewModel {

    public enum Type {
        WRAPPER,
        LOCAL_INSTALLATION,
        REMOTE_DISTRIBUTION,
        VERSION
    }

    private static final int INVALID_TYPE_ID = GradleDistributionViewModel.Type.values().length;

    private final int typeId;
    private final String configuration;

    public GradleDistributionViewModel(GradleDistributionViewModel.Type type, String configuration) {
        this.typeId = type != null ? type.ordinal() : INVALID_TYPE_ID;
        this.configuration = Strings.nullToEmpty(configuration);
    }

    public java.util.Optional<Type> getType() {
        if (this.typeId == INVALID_TYPE_ID) {
            return java.util.Optional.empty();
        } else {
            return java.util.Optional.of(GradleDistributionViewModel.Type.values()[this.typeId]);
        }
    }

    public String getConfiguration() {
        return this.configuration;
    }

    public boolean isValid() {
        return !validate().isPresent();
    }

    public Optional<String> validate() {
        java.util.Optional<GradleDistributionViewModel.Type> typeOrNull = getType();
        if (!typeOrNull.isPresent()) {
            return Optional.of("Invalid distribution type"); // TODO (donat) externalize string
        }

        GradleDistributionViewModel.Type type = typeOrNull.get();
        if (type == GradleDistributionViewModel.Type.LOCAL_INSTALLATION) {
            if (Strings.isNullOrEmpty(this.configuration)) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleDistribution_Label_LocalInstallationDirectory));
            } else {
                return validateLocalInstallationLocation(new File(this.configuration));
            }
        } else if (type == GradleDistributionViewModel.Type.REMOTE_DISTRIBUTION) {
            if (Strings.isNullOrEmpty(this.configuration)) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleDistribution_Label_RemoteDistributionUri));
            } else if (!isValidURI(this.configuration)) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_IsNotValid, CoreMessages.GradleDistribution_Label_RemoteDistributionUri));
            } else {
                return Optional.absent();
            }
        } else if (type == GradleDistributionViewModel.Type.VERSION) {
            if (Strings.isNullOrEmpty(this.configuration)) {
                return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleDistribution_Label_SpecificGradleVersion));
            } else {
                return Optional.absent();
            }
        } else {
            return Optional.absent();
        }
    }

    private static Optional<String> validateLocalInstallationLocation(File location) {
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
                return GradleDistribution.fromBuild();
            case LOCAL_INSTALLATION:
                return GradleDistribution.forLocalInstallation(new File(this.configuration));
            case REMOTE_DISTRIBUTION:
                return GradleDistribution.forRemoteDistribution(createUrl(this.configuration));
            case VERSION:
                return GradleDistribution.forVersion(this.configuration);
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        GradleDistributionViewModel that = (GradleDistributionViewModel) other;
        return Objects.equal(this.typeId, that.typeId) && Objects.equal(this.configuration, that.configuration);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.typeId, this.configuration);
    }

    @Override
    public String toString() {
        java.util.Optional<GradleDistributionViewModel.Type> typeOrNull = getType();

        if (!typeOrNull.isPresent()) {
            return "Unknown Gradle distribution " + this.configuration; // TODO (donat) NLS
        }

        GradleDistributionViewModel.Type type = typeOrNull.get();
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

    public static GradleDistributionViewModel from(GradleDistribution gradleDistribution) {
        if (gradleDistribution instanceof LocalGradleDistribution) {
            return new GradleDistributionViewModel(Type.LOCAL_INSTALLATION, ((LocalGradleDistribution)gradleDistribution).getLocation().getAbsolutePath());
        } else if (gradleDistribution instanceof RemoteGradleDistribution) {
            return new GradleDistributionViewModel(Type.REMOTE_DISTRIBUTION, ((RemoteGradleDistribution)gradleDistribution).getUrl().toString());
        } else if (gradleDistribution instanceof FixedVersionGradleDistribution) {
            return new GradleDistributionViewModel(Type.VERSION, ((FixedVersionGradleDistribution)gradleDistribution).getVersion());
        } else if (gradleDistribution instanceof WrapperGradleDistribution) {
            return new GradleDistributionViewModel(Type.WRAPPER, null);
        } else {
            throw new GradlePluginsRuntimeException("Invalid distribution type: " + gradleDistribution);
        }
    }

    public static Validator<GradleDistributionViewModel> validator() {
        return new Validator<GradleDistributionViewModel>() {

            @Override
            public Optional<String> validate(GradleDistributionViewModel distributionInfo) {
                return distributionInfo.validate();
            }
        };
    }
}
