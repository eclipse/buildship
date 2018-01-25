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
import java.net.URI;
import java.net.URISyntaxException;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import org.eclipse.osgi.util.NLS;

import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.util.binding.Validator;

/**
 * Factory class for {@link com.gradleware.tooling.toolingutils.binding.Validator} instances that
 * validate a {@link org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper} instance.
 */
public final class GradleDistributionValidator {

    private GradleDistributionValidator() {
    }

    /**
     * Creates a new {@code Validator} instance.
     *
     * @return the new instance
     */
    public static Validator<GradleDistributionWrapper> gradleDistributionValidator() {
        return new Validator<GradleDistributionWrapper>() {

            @Override
            public Optional<String> validate(GradleDistributionWrapper gradleDistribution) {
                GradleDistributionWrapper.DistributionType type = gradleDistribution.getType();
                String configuration = gradleDistribution.getConfiguration();

                if (GradleDistributionWrapper.DistributionType.LOCAL_INSTALLATION == type) {
                    if (Strings.isNullOrEmpty(configuration)) {
                        return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleDistribution_Label_LocalInstallationDirectory));
                    } else if (!new File(configuration).exists()) {
                        return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_DoesNotExist, CoreMessages.GradleDistribution_Label_LocalInstallationDirectory));
                    } else {
                        return Optional.absent();
                    }
                } else if (GradleDistributionWrapper.DistributionType.REMOTE_DISTRIBUTION == type) {
                    if (Strings.isNullOrEmpty(configuration)) {
                        return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, CoreMessages.GradleDistribution_Label_RemoteDistributionUri));
                    } else if (!isValidURI(configuration)) {
                        return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_IsNotValid, CoreMessages.GradleDistribution_Label_RemoteDistributionUri));
                    } else {
                        return Optional.absent();
                    }
                } else if (GradleDistributionWrapper.DistributionType.VERSION == type) {
                    if (Strings.isNullOrEmpty(configuration)) {
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
        };
    }

}
