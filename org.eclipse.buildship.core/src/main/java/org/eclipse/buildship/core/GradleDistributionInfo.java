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
     * Returns an error message if the current instance represents an invalid
     * {@link GradleDistribution}.
     *
     * @return a human-readable error message describing the problem
     */
    public abstract Optional<String> validate();

    public abstract GradleDistribution toGradleDistribution();

    public abstract String serializeToString();

    public static GradleDistributionInfo deserializeFromString(String distributionString) {
       return DefaultGradleDistributionInfo.deserializeFromString(distributionString);
    }

    public static Validator<GradleDistributionInfo> validator() {
        return DefaultGradleDistributionInfo.validator();
    }

    public static GradleDistributionInfo from(GradleDistributionType type, String configuration) {
        return DefaultGradleDistributionInfo.from(type, configuration);
    }
}
