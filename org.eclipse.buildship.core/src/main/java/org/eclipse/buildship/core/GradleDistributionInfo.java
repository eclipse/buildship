/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

import com.google.common.base.Optional;

import org.eclipse.buildship.core.internal.DefaultGradleDistributionInfo;
import org.eclipse.buildship.core.util.binding.Validator;

/**
 * Describes a valid or invalid {@link GradleDistribution}.
 *
 * @author Donat Csikos
 */
public abstract class GradleDistributionInfo {

    public abstract GradleDistributionType getType();

    public abstract String getConfiguration();

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
    public abstract Optional<String> validate();

    public abstract GradleDistribution toGradleDistribution();

    /**
     * Converts the current object to a {@link GradleDistribution} or returns
     * {@link GradleDistribution#fromBuild()} if invalid.
     *
     * @return the created Gradle distribution
     */
    public abstract GradleDistribution toGradleDistributionOrDefault();

    public abstract String serializeToString();

    public static GradleDistributionInfo deserializeFromString(String distributionString) {
       return DefaultGradleDistributionInfo.deserializeFromString(distributionString);
    }

    public static Validator<GradleDistributionInfo> validator() {
        return DefaultGradleDistributionInfo.validator();
    }

    public static GradleDistributionInfo from(GradleDistributionType type, String configuration) {
        return GradleDistributionInfo.from(type, configuration);
    }
}
