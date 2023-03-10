/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.console;

import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.internal.configuration.BaseRunConfiguration;

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
     * @return the run configuration
     */
    BaseRunConfiguration getRunConfig();

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
