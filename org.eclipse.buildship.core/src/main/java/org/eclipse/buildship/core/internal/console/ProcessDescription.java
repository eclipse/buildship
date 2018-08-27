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

package org.eclipse.buildship.core.internal.console;

import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.internal.configuration.RunConfiguration;

/**
 * Describes the process that runs a Gradle build.
 */
public interface ProcessDescription {

    /**
     * Returns the human-readable name of the process.
     *
     * @return the human-readable name of the process
     */
    String getName();

    /**
     * Returns the job in which the Gradle build runs.
     *
     * @return the {@code Job} instance of the process
     */
    Job getJob();

    /**
     * Returns the set of attributes that are applied to execute the Gradle build.
     *
     * @return the {@code GradleRunConfigurationAttributes} instance of the process
     */
    RunConfiguration getRunConfig();

    /**
     * Returns whether the process can be rerun.
     *
     * @return {@code true} if the process can be rerun
     */
    boolean isRerunnable();

    /**
     * Reruns the process. A new {@code ProcessDescription} instance will be created as part of it.
     */
    void rerun();

}
