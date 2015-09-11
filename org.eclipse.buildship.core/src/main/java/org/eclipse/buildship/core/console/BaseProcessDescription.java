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
 * Convenience implementation of the ProcessDescription interface.
 */
public abstract class BaseProcessDescription implements ProcessDescription {

    private final String name;
    private final Job job;
    private final Optional<ILaunch> launch;
    private final GradleRunConfigurationAttributes configurationAttributes;

    public BaseProcessDescription(String name, Job job, ILaunch launch, GradleRunConfigurationAttributes configurationAttributes) {
        this.name = Preconditions.checkNotNull(name);
        this.job = Preconditions.checkNotNull(job);
        this.launch = Optional.fromNullable(launch);
        this.configurationAttributes = Preconditions.checkNotNull(configurationAttributes);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Job getJob() {
        return this.job;
    }

    @Override
    public Optional<ILaunch> getLaunch() {
        return this.launch;
    }

    @Override
    public GradleRunConfigurationAttributes getConfigurationAttributes() {
        return this.configurationAttributes;
    }

}
