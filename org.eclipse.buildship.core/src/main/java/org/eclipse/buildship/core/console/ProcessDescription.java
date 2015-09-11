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

package org.eclipse.buildship.core.console;

import com.google.common.base.Optional;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;

import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;

/**
 * Describes process associated with a Gradle launch.
 */
public interface ProcessDescription {

    // TODO (donat) remove getLaunch()

    /**
     * @return a human-readable name of the process
     */
    String getName();

    /**
     * @return the {@code Job} instances in which the {@code ILaunch} instance is run
     */
    Job getJob();

    /**
     * @return the {@code ILaunch} instance of this process
     */
    Optional<ILaunch> getLaunch();

    /**
     * @return the {@code GradleRunConfigurationAttributes} applied to execute the request
     */
    GradleRunConfigurationAttributes getConfigurationAttributes();

    /**
     * Executes the process once again with the same attributes
     */
    void rerun();

}
