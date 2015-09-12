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

import com.google.common.base.Preconditions;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Convenience implementation of the ProcessDescription interface.
 */
public abstract class BaseProcessDescription implements ProcessDescription {

    private final String name;
    private final Job job;
    private final GradleRunConfigurationAttributes configurationAttributes;

    protected BaseProcessDescription(String name, Job job, GradleRunConfigurationAttributes configurationAttributes) {
        this.name = Preconditions.checkNotNull(name);
        this.job = Preconditions.checkNotNull(job);
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
    public GradleRunConfigurationAttributes getConfigurationAttributes() {
        return this.configurationAttributes;
    }

}
