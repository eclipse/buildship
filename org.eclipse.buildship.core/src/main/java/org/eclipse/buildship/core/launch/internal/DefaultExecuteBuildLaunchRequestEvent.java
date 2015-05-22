/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - refactoring and integration
 */

package org.eclipse.buildship.core.launch.internal;

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingclient.BuildLaunchRequest;

import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.launch.ExecuteBuildLaunchRequestEvent;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;

/**
 * Default implementation of {@link ExecuteBuildLaunchRequestEvent}.
 */
public final class DefaultExecuteBuildLaunchRequestEvent implements ExecuteBuildLaunchRequestEvent {

    private final Job buildJob;
    private final BuildLaunchRequest buildLaunchRequest;
    private final GradleRunConfigurationAttributes runConfigurationAttributes;
    private final String processName;

    public DefaultExecuteBuildLaunchRequestEvent(Job buildJob, BuildLaunchRequest buildLaunchRequest, GradleRunConfigurationAttributes runConfigurationAttributes, String processName) {
        this.buildJob = buildJob;
        this.buildLaunchRequest = Preconditions.checkNotNull(buildLaunchRequest);
        this.runConfigurationAttributes = Preconditions.checkNotNull(runConfigurationAttributes);
        this.processName = Preconditions.checkNotNull(processName);
    }

    @Override
    public Job getBuildJob() {
        return this.buildJob;
    }

    @Override
    public BuildLaunchRequest getBuildLaunchRequest() {
        return this.buildLaunchRequest;
    }

    @Override
    public GradleRunConfigurationAttributes getRunConfigurationAttributes() {
        return this.runConfigurationAttributes;
    }

    @Override
    public String getProcessName() {
        return this.processName;
    }

}
