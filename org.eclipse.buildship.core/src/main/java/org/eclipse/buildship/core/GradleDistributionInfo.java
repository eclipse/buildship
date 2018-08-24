/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

import java.util.Optional;

import org.eclipse.buildship.core.internal.DefaultGradleDistributionInfo;

/**
 * Represents a valid or invalid {@link GradleDistribution}.
 *
 * @author Donat Csikos
 * @since 3.0
 */
public abstract class GradleDistributionInfo {

    /**
     * Returns the distribution type.
     *
     * @return the distribution type
     */
    public abstract GradleDistributionType getType();

    /**
     * Returns a type-dependent state for this object.
     * <p>
     * For {@link GradleDistributionType#VERSION} the version is returned. For
     * {@link GradleDistributionType#LOCAL_INSTALLATION} the installation location path is returned.
     * for {@link GradleDistributionType#REMOTE_DISTRIBUTION} the remote URI is returned. For
     * {@link GradleDistributionType#WRAPPER} and for {@link GradleDistributionType#INVALID} an
     * empty string is returned.
     *
     * @return the configuration string
     */
    public abstract String getConfiguration();

    /**
     * Validates this distribution and returns a human-readable error message upon failure. If the
     * validation passes {@link Optional#empty()} is returned.
     *
     * @return the validation result
     */
    public abstract Optional<String> validate();

    /**
     * Converts the object to a {@link GradleDistribution}. If the current object is not valid then a runtime exception is thrown
     *
     * @return The created {@link GradleDistribution} instance.
     * @throws RuntimeException if the current distribution is not valid
     */
    public abstract GradleDistribution toGradleDistribution();

    /**
     * Converts this object to a string. The string then can be parsed with the {@link #deserializeFromString(String)} method.
     *
     * @return the string representation of this object
     */
    public abstract String serializeToString();

    /**
     * Creates a new instance from its string representation.
     * <p>
     * If the deserialization fails then an invalid distribution info is returned.
     *
     * @param distributionString the string to parse
     * @return a new distribution info instance
     */
    public static GradleDistributionInfo deserializeFromString(String distributionString) {
        return DefaultGradleDistributionInfo.deserializeFromString(distributionString);
    }

    /**
     * Creates a new instance.
     *
     * @param type the distribution info type
     * @param configuration the configuration string
     *
     * @return the new instance
     */
    public static GradleDistributionInfo from(GradleDistributionType type, String configuration) {
        return DefaultGradleDistributionInfo.from(type, configuration);
    }
}
