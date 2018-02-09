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

import com.google.common.base.Preconditions;

import org.eclipse.osgi.util.NLS;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.i18n.CoreMessages;

/**
 * Formats a {@link GradleDistribution} to a human-readable display string.
 */
public final class GradleDistributionFormatter {

    /**
     * Converts the given {@link GradleDistribution} to a human-readable {@link String}.
     *
     * @param gradleDistribution the Gradle distribution to stringify
     * @return the resulting string
     */
    public static String toString(GradleDistribution gradleDistribution) {
        Preconditions.checkNotNull(gradleDistribution);

        GradleDistributionWrapper gradleDistributionWrapper = GradleDistributionWrapper.from(gradleDistribution);
        return toString(gradleDistributionWrapper);
    }

    /**
     * Converts the given {@link GradleDistributionWrapper} to a human-readable {@link String}.
     *
     * @param gradleDistributionWrapper the Gradle distribution to stringify
     * @return the resulting string
     */
    public static String toString(GradleDistributionWrapper gradleDistributionWrapper) {
        Preconditions.checkNotNull(gradleDistributionWrapper);

        switch (gradleDistributionWrapper.getType()) {
            case WRAPPER:
                return CoreMessages.GradleDistribution_Value_UseGradleWrapper;
            case LOCAL_INSTALLATION:
                return NLS.bind(CoreMessages.GradleDistribution_Value_UseLocalInstallation_0, gradleDistributionWrapper.getConfiguration());
            case REMOTE_DISTRIBUTION:
                return NLS.bind(CoreMessages.GradleDistribution_Value_UseRemoteDistribution_0, gradleDistributionWrapper.getConfiguration());
            case VERSION:
                return NLS.bind(CoreMessages.GradleDistribution_Value_UseGradleVersion_0, gradleDistributionWrapper.getConfiguration());
            default:
                throw new GradlePluginsRuntimeException("Unrecognized Gradle distribution type: " + gradleDistributionWrapper.getType()); //$NON-NLS-1$
        }
    }

}
