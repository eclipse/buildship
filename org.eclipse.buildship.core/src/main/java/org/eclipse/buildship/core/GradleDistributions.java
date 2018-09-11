/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

import java.io.File;
import java.net.URI;

import org.eclipse.buildship.core.internal.DefaultFixedVersionGradleDistribution;
import org.eclipse.buildship.core.internal.DefaultLocalGradleDistribution;
import org.eclipse.buildship.core.internal.DefaultRemoteGradleDistribution;
import org.eclipse.buildship.core.internal.DefaultWrapperGradleDistribution;

/**
 * Contains factory methods for all supported Gradle distribution types.
 *
 * @author Donat Csikos
 * @since 3.0
 * @noimplement this interface is not intended to be implemented by clients
 */
public abstract class GradleDistributions {

    private GradleDistributions() {
    }

    /**
     * Creates a reference to a local Gradle installation.
     *
     * @param installationDir the local Gradle installation directory to use
     * @return a new distribution instance
     */
    public static LocalGradleDistribution forLocalInstallation(File installationDir) {
        return new DefaultLocalGradleDistribution(installationDir);
    }

    /**
     * Creates a reference to a remote Gradle distribution.
     *
     * @param distributionUri the remote Gradle distribution location to use
     * @return a new distribution instance
     */
    public static RemoteGradleDistribution forRemoteDistribution(URI distributionUri) {
        return new DefaultRemoteGradleDistribution(distributionUri.toString());
    }

    /**
     * Creates a reference to a specific version of Gradle.
     *
     * @param version the Gradle version to use
     * @return a new distribution instance
     */
    public static FixedVersionGradleDistribution forVersion(String version) {
        return new DefaultFixedVersionGradleDistribution(version);
    }

    /**
     * Creates a reference to a project-specific version of Gradle.
     *
     * @return a new distribution instance
     * @see org.gradle.tooling.GradleConnector#useBuildDistribution()
     */
    public static WrapperGradleDistribution fromBuild() {
        return new DefaultWrapperGradleDistribution();
    }
}
