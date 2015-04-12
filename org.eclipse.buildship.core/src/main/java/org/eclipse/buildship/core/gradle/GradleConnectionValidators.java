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

package org.eclipse.buildship.core.gradle;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import com.gradleware.tooling.toolingutils.binding.Validator;

import org.eclipse.buildship.core.i18n.CoreMessages;

/**
 * Factory class for {@link Validator} instances that validate Gradle connections attributes.
 */
public final class GradleConnectionValidators {

    private GradleConnectionValidators() {
    }

    public static Validator<File> requiredDirectoryValidator(final String prefix) {
        return new Validator<File>() {

            @Override
            public Optional<String> validate(File file) {
                if (file == null) {
                    return Optional.of(String.format(CoreMessages.ErrorMessage_0_MustBeSpecified, prefix));
                } else if (!file.exists()) {
                    return Optional.of(String.format(CoreMessages.ErrorMessage_0_DoesNotExist, prefix));
                } else if (!file.isDirectory()) {
                    return Optional.of(String.format(CoreMessages.ErrorMessage_0_MustBeDirectory, prefix));
                } else {
                    return Optional.absent();
                }
            }
        };
    }

    public static Validator<File> optionalDirectoryValidator(final String prefix) {
        return new Validator<File>() {

            @Override
            public Optional<String> validate(File file) {
                if (file == null) {
                    return Optional.absent();
                } else if (!file.exists()) {
                    return Optional.of(String.format(CoreMessages.ErrorMessage_0_DoesNotExist, prefix));
                } else if (!file.isDirectory()) {
                    return Optional.of(String.format(CoreMessages.ErrorMessage_0_MustBeDirectory, prefix));
                } else {
                    return Optional.absent();
                }
            }
        };
    }

    public static Validator<GradleDistributionWrapper> gradleDistributionValidator() {
        return new Validator<GradleDistributionWrapper>() {

            @Override
            public Optional<String> validate(GradleDistributionWrapper gradleDistribution) {
                GradleDistributionWrapper.DistributionType type = gradleDistribution.getType();
                String configuration = gradleDistribution.getConfiguration();

                if (GradleDistributionWrapper.DistributionType.LOCAL_INSTALLATION == type) {
                    if (Strings.isNullOrEmpty(configuration)) {
                        return Optional.of(String.format(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleOptions_Label_LocalInstallationDirectory));
                    } else if (!new File(configuration).exists()) {
                        return Optional.of(String.format(CoreMessages.ErrorMessage_0_DoesNotExist, CoreMessages.GradleOptions_Label_LocalInstallationDirectory));
                    } else {
                        return Optional.absent();
                    }
                } else if (GradleDistributionWrapper.DistributionType.REMOTE_DISTRIBUTION == type) {
                    if (Strings.isNullOrEmpty(configuration)) {
                        return Optional.of(String.format(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleOptions_Label_RemoteDistributionUri));
                    } else if (!isValidURI(configuration)) {
                        return Optional.of(String.format(CoreMessages.ErrorMessage_0_IsNotValid, CoreMessages.GradleOptions_Label_RemoteDistributionUri));
                    } else {
                        return Optional.absent();
                    }
                } else if (GradleDistributionWrapper.DistributionType.VERSION == type) {
                    if (Strings.isNullOrEmpty(configuration)) {
                        return Optional.of(String.format(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleOptions_Label_SpecificGradleVersion));
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
        };
    }

    public static <T> Validator<T> nullValidator() {
        return new Validator<T>() {

            @Override
            public Optional<String> validate(T value) {
                return Optional.absent();
            }

        };
    }

}
