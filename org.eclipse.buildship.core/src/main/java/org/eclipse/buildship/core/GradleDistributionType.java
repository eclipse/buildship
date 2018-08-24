/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

/**
 * Enumerates types of available Gradle distributions.
 *
 * @author Donat Csikos
 * @since 3.0
 */
public enum GradleDistributionType {
    /**
     * An invalid Gradle distribution. {@link GradleDistributionInfo} instances can have this type
     * but {@link GradleDistribution} cannot.
     */
    INVALID,

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