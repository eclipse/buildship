/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.launch;

import com.google.common.base.Optional;

import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Manages the interactions with the Gradle {@link ILaunchConfiguration} instances.
 */
public interface GradleLaunchConfigurationManager {

    /**
     * Returns the existing Gradle {@link ILaunchConfiguration} instance for the given set of
     * attributes, if such a configuration exists.
     *
     * @param configurationAttributes the run configuration attributes, must not be not null
     * @return the existing Gradle run configuration or {@link Optional#absent()} if no such
     *         configuration exists
     */
    Optional<ILaunchConfiguration> getRunConfiguration(GradleRunConfigurationAttributes configurationAttributes);

    /**
     * Returns either a new Gradle {@link ILaunchConfiguration} instance or an existing one,
     * depending on whether there is already a Gradle run configuration for the given set of
     * attributes or not. The result is saved to the disk.
     *
     * @param configurationAttributes the run configuration attributes, must not be not null
     * @return the new or existing Gradle run configuration
     */
    ILaunchConfiguration getOrCreateRunConfiguration(GradleRunConfigurationAttributes configurationAttributes);

    /**
     * Returns either a new Gradle {@link ILaunchConfiguration} instance or an existing one,
     * depending on whether there is already a Gradle test run configuration for the given set of
     * attributes or not. The result is saved to the disk.
     *
     * @param configurationAttributes the launch configuration attributes, must not be not null
     * @return the new or existing Gradle test launch configuration
     */
    ILaunchConfiguration getOrCreateTestRunConfiguration(GradleTestRunConfigurationAttributes configurationAttributes);

    /**
     * Launches the given target configuration.
     *
     * @param configuration the launch configuration to launch
     * @param mode the target mode
     */
    void launch(ILaunchConfiguration configuration, String mode);

}
