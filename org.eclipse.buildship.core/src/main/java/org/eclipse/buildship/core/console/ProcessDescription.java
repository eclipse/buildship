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
import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;

import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;

/**
 * Describes a process.
 * <p>
 * Each process description has a name and optionally an {@link ILaunch} instance and a {@link Job}
 * instance which are performing the actual execution.
 */
public final class ProcessDescription {

    private final String name;
    private final Job job;
    private final Optional<ILaunch> launch;
    private final GradleRunConfigurationAttributes configurationAttributes;

    private ProcessDescription(String name, Job job, Optional<ILaunch> launch, GradleRunConfigurationAttributes configurationAttributes) {
        this.name = name;
        this.job = job;
        this.launch = launch;
        this.configurationAttributes = configurationAttributes;
    }

    public String getName() {
        return this.name;
    }

    public Job getJob() {
        return this.job;
    }

    public Optional<ILaunch> getLaunch() {
        return this.launch;
    }

    public GradleRunConfigurationAttributes getConfigurationAttributes() {
        return this.configurationAttributes;
    }

    /**
     * Creates a new instance.
     *
     * @param name                    a human-readable name of the process
     * @param job                     the {@code Job} instances in which the {@code ILaunch} instance is run
     * @param launch                  the {@code ILaunch} instance of this process
     * @param configurationAttributes the {@code GradleRunConfigurationAttributes} applied to execute the request
     * @return the new instance
     */
    public static ProcessDescription with(String name, Job job, ILaunch launch, GradleRunConfigurationAttributes configurationAttributes) {
        return new ProcessDescription(Preconditions.checkNotNull(name), Preconditions.checkNotNull(job), Optional.fromNullable(launch), Preconditions.checkNotNull(configurationAttributes));
    }

}
