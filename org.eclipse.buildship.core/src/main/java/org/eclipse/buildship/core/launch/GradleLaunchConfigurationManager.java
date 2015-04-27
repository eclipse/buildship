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

package org.eclipse.buildship.core.launch;

import com.google.common.base.Optional;

import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Manages the interactions with the Gradle {@link ILaunchConfiguration} instances.
 */
public interface GradleLaunchConfigurationManager {

    /**
     * Returns an existing {@link ILaunchConfiguration} instance corresponding to the specified
     * {@link GradleRunConfigurationAttributes}.
     *
     * @param configurationAttributes the run configuration attributes to look up
     * @return an existing Gradle run configuration or {@link Optional#absent()} if no such
     *         ILaunchConfiguration exists
     */
    Optional<ILaunchConfiguration> getRunConfiguration(GradleRunConfigurationAttributes configurationAttributes);

    /**
     * Returns either a new Gradle {@link ILaunchConfiguration} instance or an existing one,
     * depending on whether there is already a Gradle run configuration for the given set of
     * attributes or not.
     *
     * @param configurationAttributes the run configuration attributes
     * @return the new or reused Gradle run configuration
     */
    ILaunchConfiguration getOrCreateRunConfiguration(GradleRunConfigurationAttributes configurationAttributes);

}
